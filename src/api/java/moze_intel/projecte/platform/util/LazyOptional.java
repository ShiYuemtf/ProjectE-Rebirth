package moze_intel.projecte.platform.util;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric side replacement for Forge's {@code LazyOptional}. Keeps the same contract: a lazily resolved value that can be invalidated, with listeners fired on
 * invalidation. The implementation is a thin wrapper around an optional supplier.
 */
public final class LazyOptional<T> {

	private static final LazyOptional<?> EMPTY = new LazyOptional<>(null);

	@Nullable
	private Supplier<T> supplier;
	@Nullable
	private T value;
	private final List<Consumer<LazyOptional<T>>> listeners = new ArrayList<>(1);
	private boolean valid = true;

	private LazyOptional(@Nullable Supplier<T> supplier) {
		this.supplier = supplier;
		this.value = null;
	}

	public static <T> LazyOptional<T> empty() {
		@SuppressWarnings("unchecked")
		LazyOptional<T> empty = (LazyOptional<T>) EMPTY;
		return empty;
	}

	public static <T> LazyOptional<T> of(Supplier<T> supplier) {
		if (supplier == null) {
			throw new NullPointerException("Supplier must not be null");
		}
		return new LazyOptional<>(supplier);
	}

	public static <T> LazyOptional<T> of(@Nullable final T value) {
		return new LazyOptional<>(value == null ? null : () -> value);
	}

	/**
	 * Resolves the value, or {@link Optional#empty()} if this optional is empty or has been invalidated.
	 */
	@NotNull
	public <R> Optional<R> resolve() {
		if (!isPresent()) {
			return Optional.empty();
		}
		@SuppressWarnings("unchecked")
		R cast = (R) this.value;
		return Optional.ofNullable(cast);
	}

	@SuppressWarnings("unchecked")
	@NotNull
	public T orElse(T other) {
		return isPresent() ? (T) this.value : other;
	}

	public void ifPresent(Consumer<? super T> consumer) {
		if (isPresent()) {
			consumer.accept((T) this.value);
		}
	}

	public void ifPresentOrElse(Consumer<? super T> consumer, Runnable notPresent) {
		if (isPresent()) {
			consumer.accept((T) this.value);
		} else {
			notPresent.run();
		}
	}

	public boolean isPresent() {
		if (!valid) {
			return false;
		}
		if (value == null && supplier != null) {
			value = supplier.get();
			supplier = null;
		}
		return value != null;
	}

	@NotNull
	public LazyOptional<T> filter(Predicate<? super T> predicate) {
		if (!isPresent()) {
			return empty();
		}
		return predicate.test((T) value) ? this : empty();
	}

	@NotNull
	public <R> LazyOptional<R> map(Function<? super T, ? extends R> mapper) {
		return isPresent() ? LazyOptional.of(() -> mapper.apply((T) value)) : empty();
	}

	/**
	 * Reinterprets this optional as holding another type, matching Forge's signature. The cast is unchecked by design: callers only do it after matching the
	 * capability instance itself.
	 */
	@SuppressWarnings("unchecked")
	public <R> LazyOptional<R> cast() {
		return (LazyOptional<R>) this;
	}

	public T orElseGet(Supplier<? extends T> other) {
		return isPresent() ? (T) value : other.get();
	}

	public T orElseThrow() {
		if (isPresent()) {
			return (T) value;
		}
		throw new NoSuchElementException("No value present");
	}

	public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
		if (isPresent()) {
			return (T) value;
		}
		throw exceptionSupplier.get();
	}

	/**
	 * Invalidates this optional and notifies every listener. Further calls to {@link #isPresent()} report false.
	 */
	public void invalidate() {
		if (valid) {
			valid = false;
			supplier = null;
			value = null;
			if (!listeners.isEmpty()) {
				List<Consumer<LazyOptional<T>>> copy = new ArrayList<>(listeners);
				listeners.clear();
				for (Consumer<LazyOptional<T>> listener : copy) {
					listener.accept(empty());
				}
			}
		}
	}

	public void addListener(Consumer<LazyOptional<T>> listener) {
		if (valid) {
			listeners.add(listener);
		} else {
			listener.accept(empty());
		}
	}

	public void removeListener(Consumer<LazyOptional<T>> listener) {
		listeners.remove(listener);
	}
}
