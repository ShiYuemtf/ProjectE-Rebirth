package moze_intel.projecte.platform.util;

import java.util.function.Predicate;

/**
 * Fabric side stand-in for Forge's {@code NonNullPredicate}.
 */
@FunctionalInterface
public interface NonNullPredicate<T> extends Predicate<T> {
}
