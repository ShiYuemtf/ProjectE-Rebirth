package moze_intel.projecte.gameObjs.items;

import moze_intel.projecte.gameObjs.EnumFuelType;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import org.jetbrains.annotations.NotNull;

public class AlchemicalFuel extends ItemPE {

	@NotNull
	private final EnumFuelType fuelType;

	public AlchemicalFuel(Properties props, @NotNull EnumFuelType type) {
		super(props);
		this.fuelType = type;
		//1.20.1 has no Item#getBurnTime hook, so Forge's IForgeItem#getBurnTime becomes an entry in the shared furnace fuel table instead
		//(vanilla furnaces and DMFurnaceBlockEntity both read AbstractFurnaceBlockEntity#getFuel)
		if (type.getBurnTime() > 0) {
			FuelRegistry.INSTANCE.add(this, type.getBurnTime());
		}
	}

	@NotNull
	public EnumFuelType getFuelType() {
		return fuelType;
	}
}
