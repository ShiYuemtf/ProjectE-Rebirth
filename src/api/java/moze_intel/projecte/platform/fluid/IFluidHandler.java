package moze_intel.projecte.platform.fluid;

import javax.annotation.Nullable;

/**
 * Fabric side stand-in for Forge's {@code IFluidHandler}. Ported call sites use it for the amulets and the dispenser behaviours that pour water or lava into
 * other mods' tanks.
 */
public interface IFluidHandler {

	int getTanks();

	FluidStack getFluidInTank(int tank);

	int getTankCapacity(int tank);

	boolean isFluidValid(int tank, FluidStack stack);

	int fill(FluidStack resource, FluidAction action);

	FluidStack drain(FluidStack resource, FluidAction action);

	FluidStack drain(int maxDrain, FluidAction action);

	enum FluidAction {
		EXECUTE,
		SIMULATE;

		public boolean execute() {
			return this == EXECUTE;
		}

		public boolean simulate() {
			return this == SIMULATE;
		}
	}

	@Nullable
	default FluidStack drainIgnoringAmountLimit(FluidStack resource, FluidAction action) {
		return drain(resource, action);
	}
}
