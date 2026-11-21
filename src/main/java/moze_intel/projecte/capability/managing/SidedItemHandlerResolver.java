package moze_intel.projecte.capability.managing;

import net.minecraft.core.Direction;
import moze_intel.projecte.platform.capability.Capability;
import moze_intel.projecte.platform.capability.CompatCapabilities;
import moze_intel.projecte.platform.util.LazyOptional;
import moze_intel.projecte.api.item_handlers.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SidedItemHandlerResolver implements ICapabilityResolver<IItemHandler> {

	protected abstract ICapabilityResolver<IItemHandler> getResolver(@Nullable Direction side);

	@NotNull
	@Override
	public Capability<IItemHandler> getMatchingCapability() {
		return CompatCapabilities.ITEM_HANDLER;
	}

	@NotNull
	@Override
	public <T> LazyOptional<T> getCapabilityUnchecked(@NotNull Capability<T> capability, @Nullable Direction side) {
		return getResolver(side).getCapabilityUnchecked(capability, side);
	}

	@Override
	public void invalidate(@NotNull Capability<?> capability, @Nullable Direction side) {
		getResolver(side).invalidate(capability, side);
	}
}