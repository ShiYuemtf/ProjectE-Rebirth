package moze_intel.projecte.events;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.capabilities.IAlchBagProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.item_handlers.IItemHandler;
import moze_intel.projecte.api.item_handlers.ItemHandlerHelper;
import moze_intel.projecte.capability.CapabilityHelper;
import moze_intel.projecte.gameObjs.items.AlchemicalBag;
import moze_intel.projecte.gameObjs.items.armor.PEArmor;
import moze_intel.projecte.handlers.InternalAbilities;
import moze_intel.projecte.impl.TransmutationOffline;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.text.PELang;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PlayerEvents implements ModInitializer {

	private static boolean registered;

	/**
	 * Server side registrations. Also reached through {@link #onInitialize()} so that {@code fabric.mod.json} can list this class as a main entrypoint; whichever
	 * runs first wins because the guard makes it idempotent.
	 */
	@Override
	public void onInitialize() {
		registerServer();
	}

	public static void registerServer() {
		if (registered) {
			return;
		}
		registered = true;
		ServerTickEvents.END_SERVER_TICK.register(PlayerEvents::tickLoginSync);
		//On death or return from end, copy the capability data
		ServerPlayerEvents.COPY_FROM.register(PlayerEvents::cloneEvent);
		//On death or return from end, sync to the client
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> syncData(newPlayer));
		//Sync to the client for "normal" interdimensional teleports (nether portal, etc.)
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
			syncData(player);
			CapabilityHelper.getCapability(player, InternalAbilities.CAPABILITY).ifPresent(InternalAbilities::onDimensionChange);
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> playerConnect(handler.player, server));
		//Capabilities are cached per UUID, so drop them on the way out instead of leaking stale state into the next login
		//Do NOT drop the cached capabilities here: the server saves the player again while shutting down, and a cleared
		//cache made that second save write an empty knowledge/EMC block over the real one. Stale entries are pruned on join instead.
		//Nothing is dropped here. Vanilla saves the player a second time while the server shuts down, and by then the
		//connection is already marked disconnected, so pruning here made that final save write an empty capability block.
		//Entries are dropped when the server stops (after the final save) and lazily when another player joins.
		//This hook fires when the entity first is about to take damage, if it is denied it is as if they never got hit/damaged
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) ->
				!(entity instanceof ServerPlayer player && source.is(DamageTypeTags.IS_FIRE) && TickEvents.shouldPlayerResistFire(player)));
	}

	private static void cloneEvent(ServerPlayer original, ServerPlayer newPlayer, boolean alive) {
		//The capability cache is keyed by uuid, so without dropping it the new player instance keeps using providers that still point at the old inventory and
		//at the dead entity. Copy everything that supports NBT across, then let the new instance rebuild from that data.
		CompoundTag data = CapabilityHelper.serializePlayerCapabilities(original);
		CapabilityHelper.invalidatePlayerCapabilities(newPlayer);
		CapabilityHelper.deserializePlayerCapabilities(newPlayer, data);
		//A dead instance never reconnects, so pruneOfflinePlayers() would never collect it, and the cache keys are strong references.
		CapabilityHelper.invalidatePlayerCapabilities(original);
	}

	private static void syncData(ServerPlayer player) {
		CapabilityHelper.getCapability(player, PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent(c -> c.sync(player));
		CapabilityHelper.getCapability(player, PECapabilities.ALCH_BAG_CAPABILITY).ifPresent(c -> c.sync(null, player));
	}

	/**
	 * Packet offsets (in player ticks) at which the login snapshot is re-sent. Sending it more than once is harmless because the
	 * client simply re-applies the same knowledge/EMC snapshot, and it removes the race where the client was not ready yet.
	 */
	private static final int[] LOGIN_SYNC_AT = {20, 60};
	private static final Map<UUID, Integer> LOGIN_SYNC_AGE = new HashMap<>();

	private static void tickLoginSync(MinecraftServer server) {
		if (LOGIN_SYNC_AGE.isEmpty()) {
			return;
		}
		Iterator<Map.Entry<UUID, Integer>> iterator = LOGIN_SYNC_AGE.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, Integer> entry = iterator.next();
			ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
			if (player == null || player.hasDisconnected()) {
				iterator.remove();
				continue;
			}
			int age = entry.getValue() + 1;
			for (int offset : LOGIN_SYNC_AT) {
				if (age == offset) {
					PacketHandler.sendFragmentedEmcPacket(player);
					syncData(player);
					break;
				}
			}
			if (age >= LOGIN_SYNC_AT[LOGIN_SYNC_AT.length - 1]) {
				iterator.remove();
			} else {
				entry.setValue(age);
			}
		}
	}

	private static void playerConnect(ServerPlayer player, MinecraftServer server) {
		//The player is loading fresh online data, drop any cached offline data (fake players never open a connection so they never get here)
		TransmutationOffline.clear(player.getUUID());
		PECore.debugLog("Clearing offline data cache in preparation to load online data");

		PacketHandler.sendFragmentedEmcPacket(player);

		CapabilityHelper.getCapability(player, PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent(knowledge -> {
			knowledge.sync(player);
			PlayerHelper.updateScore(player, PlayerHelper.SCOREBOARD_EMC, knowledge.getEmc());
		});

		CapabilityHelper.getCapability(player, PECapabilities.ALCH_BAG_CAPABILITY).ifPresent(c -> c.sync(null, player));

		PECore.debugLog("Sent knowledge and bag data to {}", player.getName());
		//The client is often still loading its world when JOIN fires and drops anything it cannot handle yet, so the
		//whole login snapshot is repeated once the client has certainly created its player.
		LOGIN_SYNC_AGE.put(player.getUUID(), 0);

		if (PECore.uuids.contains(player.getUUID().toString())) {
			Component joinMessage = PELang.HIGH_ALCHEMIST.translateColored(ChatFormatting.BLUE, ChatFormatting.GOLD, player.getDisplayName());
			server.getPlayerList().broadcastSystemMessage(joinMessage, false);
		}
	}

	/**
	 * Alchemical bag suction, called from {@code moze_intel.projecte.mixin.ItemEntityMixin} at the head of {@code ItemEntity#playerTouch}.
	 *
	 * @return true if a bag took the item and vanilla pickup handling should be skipped.
	 */
	public static boolean onItemPickup(ItemEntity itemEntity, Player player) {
		Level level = player.level();
		if (level.isClientSide || itemEntity.hasPickUpDelay()) {
			return false;
		}
		ItemStack bag = AlchemicalBag.getFirstBagWithSuctionItem(player, player.getInventory().items);
		if (bag.isEmpty()) {
			return false;
		}
		Optional<IAlchBagProvider> cap = CapabilityHelper.getCapability(player, PECapabilities.ALCH_BAG_CAPABILITY).resolve();
		if (cap.isEmpty()) {
			return false;
		}
		IItemHandler handler = cap.get().getBag(((AlchemicalBag) bag.getItem()).color);
		ItemStack remainder = ItemHandlerHelper.insertItemStacked(handler, itemEntity.getItem(), false);
		if (remainder.isEmpty()) {
			itemEntity.discard();
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
			((ServerPlayer) player).connection.send(new ClientboundTakeItemEntityPacket(itemEntity.getId(), player.getId(), 1));
		} else {
			itemEntity.setItem(remainder);
		}
		return true;
	}

	/**
	 * Applies the damage reduction of worn ProjectEF armor. Called from {@code moze_intel.projecte.mixin.LivingEntityMixin}, replacing LivingHurtEvent.
	 */
	public static float getReducedDamage(LivingEntity entityLiving, DamageSource source, float damage) {
		if (damage <= 0) {
			return damage;
		}
		float totalPercentReduced = getTotalPercentReduced(entityLiving, source, damage);
		return totalPercentReduced >= 1 ? 0 : damage - damage * totalPercentReduced;
	}

	/**
	 * @return true when the worn armor makes the hit do nothing at all, which Forge expressed by cancelling the damage event.
	 */
	public static boolean blocksAllDamage(LivingEntity entityLiving, DamageSource source, float damage) {
		return damage > 0 && getTotalPercentReduced(entityLiving, source, damage) >= 1;
	}

	private static float getTotalPercentReduced(LivingEntity entityLiving, DamageSource source, float damage) {
		return getReductionForSlot(entityLiving, source, EquipmentSlot.HEAD, damage) +
				getReductionForSlot(entityLiving, source, EquipmentSlot.CHEST, damage) +
				getReductionForSlot(entityLiving, source, EquipmentSlot.LEGS, damage) +
				getReductionForSlot(entityLiving, source, EquipmentSlot.FEET, damage);
	}

	private static float getReductionForSlot(LivingEntity entityLiving, DamageSource source, EquipmentSlot slot, float damage) {
		ItemStack armorStack = entityLiving.getItemBySlot(slot);
		if (armorStack.getItem() instanceof PEArmor armorItem) {
			ArmorItem.Type type = armorItem.getType();
			if (type.getSlot() != slot) {
				//If the armor slot does not match the slot this piece of armor is for then it shouldn't be providing any reduction
				return 0;
			}
			//We return the max of this piece's base reduction (in relation to the full set), and the
			// max damage an item can absorb for a given source
			return Math.max(armorItem.getFullSetBaseReduction(), armorItem.getMaxDamageAbsorb(type, source) / damage) * armorItem.getPieceEffectiveness(type);
		}
		return 0;
	}
}
