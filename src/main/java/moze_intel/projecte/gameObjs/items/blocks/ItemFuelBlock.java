package moze_intel.projecte.gameObjs.items.blocks;

import moze_intel.projecte.gameObjs.EnumFuelType;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class ItemFuelBlock extends BlockItem {

	@NotNull
	private final EnumFuelType type;

	public ItemFuelBlock(Block block, Properties props, @NotNull EnumFuelType type) {
		super(block, props);
		this.type = type;
		//1.20.1 has no Item#getBurnTime hook; the vanilla furnace fuel table is fed through the Fabric content registry instead
		int burnTime = type.getBurnTime();
		if (burnTime > 0) {
			FuelRegistry.INSTANCE.add(this, burnTime * 9);
		}
	}
}
