package moze_intel.projecte.events;

import java.util.EnumSet;
import java.util.Set;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.item_handlers.IItemHandler;
import moze_intel.projecte.capability.CapabilityHelper;
import moze_intel.projecte.platform.capability.CompatCapabilities;
import moze_intel.projecte.gameObjs.container.AlchBagContainer;
import moze_intel.projecte.gameObjs.items.AlchemicalBag;
import moze_intel.projecte.gameObjs.items.IFireProtector;
import moze_intel.projecte.handlers.CommonInternalAbilities;
import moze_intel.projecte.handlers.InternalAbilities;
import moze_intel.projecte.handlers.InternalTimers;
import moze_intel.projecte.utils.PlayerHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

/**
 * Player ticking. The server half is registered here (also through {@link #onInitialize()} when listed as a main entrypoint), the client half by
 * {@code ClientEvents#register()} so that client only classes never load on a dedicated server.
 */
public class TickEvents implements ModInitializer {

	private static boolean serverRegistered;

	@Override
	public void onInitialize() {
		registerServer();
	}

	public static void registerServer() {
		if (serverRegistered) {
			return;
		}
		serverRegistered = true;
		//Forge fired one PlayerTickEvent per player per tick; Fabric only ticks worlds, so the player loop is explicit
		ServerTickEvents.END_WORLD_TICK.register(level -> level.players().forEach(TickEvents::tickPlayer));
	}

	public static void tickPlayer(Player player) {
		CapabilityHelper.getCapability(player, PECapabilities.ALCH_BAG_CAPABILITY).ifPresent(provider -> {
			Set<DyeColor> colorsChanged = EnumSet.noneOf(DyeColor.class);
			for (DyeColor color : getBagColorsPresent(player)) {
				IItemHandler inv = provider.getBag(color);
				for (int i = 0; i < inv.getSlots(); i++) {
					ItemStack current = inv.getStackInSlot(i);
					if (!current.isEmpty()) {
						CapabilityHelper.getCapability(current, PECapabilities.ALCH_BAG_ITEM_CAPABILITY).ifPresent(alchBagItem -> {
							if (alchBagItem.updateInAlchBag(inv, player, current)) {
								colorsChanged.add(color);
							}
						});
					}
				}
			}

			if (player instanceof ServerPlayer serverPlayer) {
				//Only sync for when it ticks on the server
				for (DyeColor e : colorsChanged) {
					if (serverPlayer.containerMenu instanceof AlchBagContainer container) {
						ItemStack heldItem = serverPlayer.getItemInHand(container.hand);
						if (heldItem.getItem() instanceof AlchemicalBag bag && bag.color == e) {
							// Do not sync if this color is open, the container system does it for us
							// and we'll stay out of its way.
							continue;
						}
					}
					provider.sync(e, serverPlayer);
				}
			}
		});

		CapabilityHelper.getCapability(player, CommonInternalAbilities.CAPABILITY).ifPresent(CommonInternalAbilities::tick);
		//The cast is the side check: these handlers only ever exist for a ServerPlayer
		if (player instanceof ServerPlayer serverPlayer) {
			CapabilityHelper.getCapability(player, InternalAbilities.CAPABILITY).ifPresent(InternalAbilities::tick);
			CapabilityHelper.getCapability(player, InternalTimers.CAPABILITY).ifPresent(InternalTimers::tick);
			if (player.isOnFire() && shouldPlayerResistFire(serverPlayer)) {
				player.clearFire();
			}
		}
	}

	public static boolean shouldPlayerResistFire(ServerPlayer player) {
		for (ItemStack stack : player.getArmorSlots()) {
			if (!stack.isEmpty() && stack.getItem() instanceof IFireProtector protector && protector.canProtectAgainstFire(stack, player)) {
				return true;
			}
		}
		for (int i = 0; i < Inventory.getSelectionSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (!stack.isEmpty() && stack.getItem() instanceof IFireProtector protector && protector.canProtectAgainstFire(stack, player)) {
				return true;
			}
		}
		IItemHandler curios = PlayerHelper.getCurios(player);
		if (curios != null) {
			for (int i = 0; i < curios.getSlots(); i++) {
				ItemStack stack = curios.getStackInSlot(i);
				if (!stack.isEmpty() && stack.getItem() instanceof IFireProtector protector && protector.canProtectAgainstFire(stack, player)) {
					return true;
				}
			}
		}
		return false;
	}

	private static Set<DyeColor> getBagColorsPresent(Player player) {
		Set<DyeColor> bagsPresent = EnumSet.noneOf(DyeColor.class);
		CapabilityHelper.getCapability(player, CompatCapabilities.ITEM_HANDLER).ifPresent(inv -> {
			for (int i = 0; i < inv.getSlots(); i++) {
				ItemStack stack = inv.getStackInSlot(i);
				if (!stack.isEmpty() && stack.getItem() instanceof AlchemicalBag bag) {
					bagsPresent.add(bag.color);
				}
			}
		});
		return bagsPresent;
	}
}
