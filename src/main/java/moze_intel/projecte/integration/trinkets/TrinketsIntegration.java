package moze_intel.projecte.integration.trinkets;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import moze_intel.projecte.api.item_handlers.IItemHandler;
import moze_intel.projecte.gameObjs.registries.PEItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

/**
 * Trinkets specific bridge.
 * <p>
 * This class is only class loaded once the trinkets mod is confirmed present (every entry point is guarded by {@code IntegrationHelper.isLoaded}), so ProjectE
 * keeps loading fine without trinkets installed.
 */
public final class TrinketsIntegration {

	private static final ProjectETrinket PROJECTE_TRINKET = new ProjectETrinket();

	private TrinketsIntegration() {
	}

	/**
	 * Registers every ProjectE accessory as a wearable trinket. This replaces the Forge build's {@code addItemCapability(CURIOS_MODID, ...)} calls: Curios let
	 * an item advertise a capability, Trinkets instead needs the item registered up front.
	 */
	public static void registerProjectETrinkets() {
		//Amulets and the tools that were curios on Forge
		register(PEItems.EVERTIDE_AMULET.get());
		register(PEItems.VOLCANITE_AMULET.get());
		register(PEItems.REPAIR_TALISMAN.get());
		register(PEItems.GEM_OF_ETERNAL_DENSITY.get());
		//All six Klein Star tiers
		register(PEItems.KLEIN_STAR_EIN.get());
		register(PEItems.KLEIN_STAR_ZWEI.get());
		register(PEItems.KLEIN_STAR_DREI.get());
		register(PEItems.KLEIN_STAR_VIER.get());
		register(PEItems.KLEIN_STAR_SPHERE.get());
		register(PEItems.KLEIN_STAR_OMEGA.get());
		//Rings
		register(PEItems.ARCANA_RING.get());
		register(PEItems.BLACK_HOLE_BAND.get());
		register(PEItems.BODY_STONE.get());
		register(PEItems.IGNITION_RING.get());
		register(PEItems.LIFE_STONE.get());
		register(PEItems.SOUL_STONE.get());
		register(PEItems.SWIFTWOLF_RENDING_GALE.get());
		register(PEItems.VOID_RING.get());
		register(PEItems.ZERO_RING.get());
	}

	private static void register(Item item) {
		TrinketsApi.registerTrinket(item, PROJECTE_TRINKET);
	}

	/**
	 * Flattens every trinket slot group into one handler, so callers do not have to know which slot a ring ended up in.
	 */
	@Nullable
	public static IItemHandler getInventory(LivingEntity living) {
		Optional<TrinketComponent> optional = TrinketsApi.getTrinketComponent(living);
		if (optional.isEmpty()) {
			return null;
		}
		TrinketComponent component = optional.get();
		List<TrinketInventory> inventories = new ArrayList<>();
		for (Map<String, TrinketInventory> group : component.getInventory().values()) {
			inventories.addAll(group.values());
		}
		return inventories.isEmpty() ? null : new TrinketItemHandler(inventories);
	}

	@Nullable
	public static IItemHandler getInventory(Player player) {
		return getInventory((LivingEntity) player);
	}
}
