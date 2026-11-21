package moze_intel.projecte.platform.util;

import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * Fabric side stand-in for Forge's {@code Lazy}: memoises the first successful value production.
 */
public final class Lazy<T> implements Supplier<T> {

	private final Supplier<T> supplier;
	@Nullable
	private volatile T value;

	private Lazy(Supplier<T> supplier) {
		this.supplier = supplier;
	}

	public static <T> Lazy<T> of(Supplier<T> supplier) {
		return new Lazy<>(supplier);
	}

	public static <T> Lazy<T> of(T value) {
		return new Lazy<>(() -> value);
	}

	@Override
	public T get() {
		T resolved = value;
		if (resolved == null) {
			synchronized (this) {
				resolved = value;
				if (resolved == null) {
					resolved = supplier.get();
					value = resolved;
				}
			}
		}
		return resolved;
	}
}
