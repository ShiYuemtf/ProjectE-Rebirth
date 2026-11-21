package moze_intel.projecte.platform.util;

import java.util.Objects;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric side replacement for Forge's {@code NonNullLazy}: a memoizing supplier whose value is created on first access.
 */
public abstract class NonNullLazy<T> implements NonNullSupplier<T> {

	public static <T> NonNullLazy<T> of(@NotNull Supplier<T> supplier) {
		Objects.requireNonNull(supplier);
		return new NonNullLazy<>() {
			@Nullable
			private volatile T value;

			@Override
			@NotNull
			public T get() {
				T result = value;
				if (result == null) {
					synchronized (this) {
						result = value;
						if (result == null) {
							value = result = Objects.requireNonNull(supplier.get());
						}
					}
				}
				return result;
			}
		};
	}

	public static <T> NonNullLazy<T> of(@NotNull T value) {
		Objects.requireNonNull(value);
		return new NonNullLazy<>() {
			@Override
			@NotNull
			public T get() {
				return value;
			}
		};
	}
}
