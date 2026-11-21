package moze_intel.projecte.platform.util;

import java.util.function.Supplier;

/**
 * Fabric side stand-in for Forge's {@code NonNullSupplier}: a supplier whose value is documented to never be null.
 */
@FunctionalInterface
public interface NonNullSupplier<T> extends Supplier<T> {

	static <T> NonNullSupplier<T> of(T value) {
		return () -> value;
	}
}
