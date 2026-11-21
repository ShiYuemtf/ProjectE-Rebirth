package moze_intel.projecte.platform.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

/**
 * Fabric side stand-in for Forge's {@code IFluidBlock}. Only {@link #getFluid()} is used by the ported code, for matching a level block against a fluid.
 */
public interface IFluidBlock {

	Fluid getFluid();

	/**
	 * Removes the fluid source at the given position. Mirrors Forge's {@code IFluidBlock#drain}.
	 */
	default boolean drain(Level level, BlockPos pos, IFluidHandler.FluidAction action) {
		if (action.execute()) {
			level.removeBlock(pos, false);
		}
		return true;
	}
}
