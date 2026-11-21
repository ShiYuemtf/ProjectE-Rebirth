package moze_intel.projecte.capability.managing;

import net.minecraft.core.Direction;
import moze_intel.projecte.platform.capability.Capability;
import moze_intel.projecte.platform.capability.CompatCapabilities;
import moze_intel.projecte.platform.util.LazyOptional;
import moze_intel.projecte.platform.util.NonNullLazy;
import moze_intel.projecte.platform.util.NonNullSupplier;
import moze_intel.projecte.api.item_handlers.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Simplified/modified version of Mekanism's BasicCapabilityResolver
 */
public abstract class BasicCapabilityResolver<CAPABILITY> implements ICapabilityResolver<CAPABILITY> {

	public static ICapabilityResolver<IItemHandler> getBasicItemHandlerResolver(NonNullSupplier<IItemHandler> supplier) {
		return new BasicCapabilityResolver<>(supplier) {
			@NotNull
			@Override
			public Capability<IItemHandler> getMatchingCapability() {
				return CompatCapabilities.ITEM_HANDLER;
			}
		};
	}

	public static ICapabilityResolver<IItemHandler> getBasicItemHandlerResolver(IItemHandler handler) {
		return new BasicCapabilityResolver<>(handler) {
			@NotNull
			@Override
			public Capability<IItemHandler> getMatchingCapability() {
				return CompatCapabilities.ITEM_HANDLER;
			}
		};
	}

	private final NonNullSupplier<CAPABILITY> supplier;
	private LazyOptional<CAPABILITY> cachedCapability;

	protected BasicCapabilityResolver(CAPABILITY constant) {
		this.supplier = () -> constant;
	}

	protected BasicCapabilityResolver(NonNullSupplier<CAPABILITY> supplier) {
		this.supplier = supplier instanceof NonNullLazy ? supplier : NonNullLazy.of(supplier);
	}

	@NotNull
	@Override
	public <T> LazyOptional<T> getCapabilityUnchecked(@NotNull Capability<T> capability, @Nullable Direction side) {
		if (cachedCapability == null || !cachedCapability.isPresent()) {
			//If the capability has not been retrieved yet or it is not valid then recreate it
			cachedCapability = LazyOptional.of(supplier);
		}
		return cachedCapability.cast();
	}

	@Override
	public void invalidate(@NotNull Capability<?> capability, @Nullable Direction side) {
		//We only have one capability so just invalidate everything
		invalidateAll();
	}

	@Override
	public void invalidateAll() {
		if (cachedCapability != null && cachedCapability.isPresent()) {
			cachedCapability.invalidate();
			cachedCapability = null;
		}
	}
}