package moze_intel.projecte.integration;

import moze_intel.projecte.api.item_handlers.IItemHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric side replacement for Forge's IMC based integration helper. IMC has no equivalent here, so this only reports which optional mods are present; the
 * integrations themselves (JEI, Trinkets) are reimplemented against their Fabric APIs instead of exchanging IMC messages.
 */
public class IntegrationHelper {

	public static final String TRINKETS_MODID = "trinkets";

	public static void sendIMCMessages() {
		//No IMC on Fabric: integrations register directly from their own entrypoints.
	}

	public static boolean isLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	/**
	 * Registers every ProjectE accessory with Trinkets. Guarded so a missing trinkets mod never touches the bridge class - the reference inside the method body
	 * is only resolved when the body actually runs.
	 */
	public static void registerTrinkets() {
		if (isLoaded(TRINKETS_MODID)) {
			TrinketsBridge.registerProjectETrinkets();
		}
	}

	/**
	 * @return the player's worn accessories flattened into one handler, or null when trinkets is absent (callers treat null as "nothing worn").
	 */
	@Nullable
	public static IItemHandler getWornTrinkets(Player player) {
		if (isLoaded(TRINKETS_MODID)) {
			return TrinketsBridge.getInventory(player);
		}
		return null;
	}

	/**
	 * Keeps the trinkets API off the class loading path of anything that does not need it: this class is only resolved when one of the two guarded methods above
	 * actually reaches it.
	 */
	private static final class TrinketsBridge {

		static void registerProjectETrinkets() {
			moze_intel.projecte.integration.trinkets.TrinketsIntegration.registerProjectETrinkets();
		}

		@Nullable
		static IItemHandler getInventory(Player player) {
			return moze_intel.projecte.integration.trinkets.TrinketsIntegration.getInventory(player);
		}
	}
}
