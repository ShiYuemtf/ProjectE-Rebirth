package moze_intel.projecte.capability;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import moze_intel.projecte.api.item_handlers.IItemHandler;
import moze_intel.projecte.api.item_handlers.PlayerMainInvWrapper;
import moze_intel.projecte.capability.managing.ICapabilityResolver;
import moze_intel.projecte.capability.managing.SerializableCapabilityResolver;
import moze_intel.projecte.platform.capability.Capability;
import moze_intel.projecte.platform.capability.CompatCapabilities;
import moze_intel.projecte.platform.capability.ICapabilityProvider;
import moze_intel.projecte.platform.capability.ICapabilityProviderItem;
import moze_intel.projecte.platform.util.LazyOptional;
import moze_intel.projecte.handlers.CommonInternalAbilities;
import moze_intel.projecte.handlers.InternalAbilities;
import moze_intel.projecte.handlers.InternalTimers;
import moze_intel.projecte.impl.capability.AlchBagImpl;
import moze_intel.projecte.impl.capability.KnowledgeImpl;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fabric replacement for Forge's {@code getCapability} on vanilla types. Forge stores capabilities in a provider field on {@code ItemStack}/{@code Entity}; Fabric has
 * no such field, so item capabilities are rebuilt from the item on each lookup and player capabilities live in {@link #PLAYER_CAPS}.
 *
 * @implNote Item capability providers are cheap value objects, but a rebuilt wrapper does not share cached state with a previous one. Anything that needs to
 * persist across lookups must live in the stack's NBT, which is what the ported capability implementations already do.
 * @implNote Player capabilities are keyed by the {@link Player} instance rather than by UUID. In a singleplayer session the client {@code LocalPlayer} and the
 * integrated server's {@code ServerPlayer} share a UUID and an entity id, so a UUID key would hand both sides the same providers: the client would read and write
 * the server's knowledge and bag state, and server-only providers such as {@link InternalAbilities} would be missing whenever the client happened to populate the
 * entry first.
 */
public class CapabilityHelper {

	/**
	 * NBT key the player capability block is stored under, both by the save mixin and by commands that read a
	 * player file straight off disk. Kept in one place so the two cannot drift apart again.
	 */
	public static final String NBT_KEY = "projecte_capabilities";

	private static final Map<Player, Map<Capability<?>, ICapabilityProvider>> PLAYER_CAPS = new IdentityHashMap<>();

	public static <T> LazyOptional<T> getCapability(@Nullable Object holder, Capability<T> capability) {
		return getCapability(holder, capability, null);
	}

	public static <T> LazyOptional<T> getCapability(@Nullable Object holder, Capability<T> capability, @Nullable Direction side) {
		if (holder == null) {
			return LazyOptional.empty();
		} else if (holder instanceof ItemStack stack) {
			return getItemCapability(stack, capability, side);
		} else if (holder instanceof Player player) {
			ICapabilityProvider provider = playerCapabilities(player).get(capability);
			return provider == null ? LazyOptional.empty() : provider.getCapability(capability, side);
		} else if (holder instanceof ICapabilityProvider provider) {
			return provider.getCapability(capability, side);
		}
		return LazyOptional.empty();
	}

	private static <T> LazyOptional<T> getItemCapability(ItemStack stack, Capability<T> capability, @Nullable Direction side) {
		//Keyed off ICapabilityProviderItem rather than ItemPE: ProjectE's tools extend AxeItem/SwordItem/... so an ItemPE test made every tool capability
		//invisible, which silently disabled charging, mode switching and projectile shooting on all of them while the Philosopher's Stone kept working.
		if (stack.isEmpty() || !(stack.getItem() instanceof ICapabilityProviderItem item)) {
			return LazyOptional.empty();
		}
		ICapabilityProvider provider = item.initCapabilities(stack, stack.getTag());
		return provider == null ? LazyOptional.empty() : provider.getCapability(capability, side);
	}

	private static Map<Capability<?>, ICapabilityProvider> playerCapabilities(Player player) {
		synchronized (PLAYER_CAPS) {
			Map<Capability<?>, ICapabilityProvider> capabilities = PLAYER_CAPS.get(player);
			if (capabilities == null) {
				//A new provider set is about to be created for this player, so it is safe to release the ones left behind by players that already left
				PLAYER_CAPS.entrySet().removeIf(entry -> entry.getKey() instanceof ServerPlayer other && other.hasDisconnected());
				capabilities = new HashMap<>();
				add(capabilities, playerInventoryResolver(player));
				add(capabilities, new AlchBagImpl.Provider());
				add(capabilities, new KnowledgeImpl.Provider(player));
				add(capabilities, new CommonInternalAbilities.Provider(player));
				if (player instanceof ServerPlayer serverPlayer) {
					add(capabilities, new InternalTimers.Provider());
					add(capabilities, new InternalAbilities.Provider(serverPlayer));
				}
				PLAYER_CAPS.put(player, capabilities);
			}
			return capabilities;
		}
	}

	/**
	 * Forge hands every player an {@code ITEM_HANDLER} capability over its main inventory; ProjectE relies on it when shift-clicking items out of the transmutation
	 * table. The wrapper is rebuilt on each lookup instead of being cached, because the capability entry outlives respawns while the inventory instance does not.
	 */
	private static ICapabilityResolver<IItemHandler> playerInventoryResolver(Player player) {
		return new ICapabilityResolver<>() {
			@NotNull
			@Override
			public Capability<IItemHandler> getMatchingCapability() {
				return CompatCapabilities.ITEM_HANDLER;
			}

			@NotNull
			@Override
			@SuppressWarnings("unchecked")
			public <T> LazyOptional<T> getCapabilityUnchecked(@NotNull Capability<T> capability, @Nullable Direction side) {
				return LazyOptional.of(() -> (T) new PlayerMainInvWrapper(player.getInventory()));
			}

			@Override
			public void invalidate(@NotNull Capability<?> capability, @Nullable Direction side) {
			}

			@Override
			public void invalidateAll() {
			}
		};
	}

	private static void add(Map<Capability<?>, ICapabilityProvider> capabilities, ICapabilityResolver<?> resolver) {
		capabilities.put(resolver.getMatchingCapability(), resolver);
	}

	/**
	 * Writes the player capabilities that support NBT so they can be stored on the player. Called from the player save/load mixin.
	 */
	public static CompoundTag serializePlayerCapabilities(Player player) {
		CompoundTag tag = new CompoundTag();
		for (Map.Entry<Capability<?>, ICapabilityProvider> entry : playerCapabilities(player).entrySet()) {
			if (entry.getValue() instanceof SerializableCapabilityResolver<?> serializable) {
				tag.put(entry.getKey().getName().toString(), serializable.serializeNBT());
			}
		}
		return tag;
	}

	public static void deserializePlayerCapabilities(Player player, CompoundTag tag) {
		for (Map.Entry<Capability<?>, ICapabilityProvider> entry : playerCapabilities(player).entrySet()) {
			String key = entry.getKey().getName().toString();
			if (entry.getValue() instanceof SerializableCapabilityResolver<?> serializable && tag.contains(key)) {
				serializable.deserializeNBT(tag.getCompound(key));
			}
		}
	}

	/**
	 * Drops cached capabilities of players that are no longer connected.
	 * <p>
	 * This replaces dropping the entry on disconnect: the server saves a player once when they leave and then again while shutting down, and the second save used
	 * to run after the cache was cleared. That handed it a fresh, empty provider set which then overwrote the real knowledge and EMC on disk, so every relog
	 * started from nothing until something re-synced it. Pruning here keeps the data available for the shutdown save while still releasing identity keys.
	 */
	public static void pruneOfflinePlayers() {
		synchronized (PLAYER_CAPS) {
			PLAYER_CAPS.entrySet().removeIf(entry -> entry.getKey() instanceof ServerPlayer serverPlayer && serverPlayer.hasDisconnected());
		}
	}

	/**
	 * Drops every cached player capability for one player.
	 */
	public static void invalidatePlayerCapabilities(Player player) {
		Map<Capability<?>, ICapabilityProvider> capabilities;
		synchronized (PLAYER_CAPS) {
			//Identity keys are strong references, so the entry has to be dropped explicitly or the whole player graph leaks
			capabilities = PLAYER_CAPS.remove(player);
		}
		if (capabilities != null) {
			for (ICapabilityProvider provider : capabilities.values()) {
				if (provider instanceof ICapabilityResolver<?> resolver) {
					resolver.invalidateAll();
				}
			}
		}
	}
}
