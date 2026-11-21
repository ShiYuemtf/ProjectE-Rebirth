package moze_intel.projecte.platform.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fabric side stand-in for Forge's {@code IPlantable}. Vanilla 1.20.1 blocks do not implement it, so ported {@code instanceof IPlantable} checks are dead ends;
 * call sites that actually need the plant state resolve it from the {@link net.minecraft.world.item.BlockItem} instead.
 */
public interface IPlantable {

	BlockState getPlant(BlockGetter level, BlockPos pos);
}
