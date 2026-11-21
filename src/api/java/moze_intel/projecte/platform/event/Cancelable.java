package moze_intel.projecte.platform.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a type as cancelable, mirroring Forge's {@code @Cancelable}. Only consulted by {@link Event#setCanceled(boolean)} so that trying to cancel a non
 * cancelable event fails loudly instead of silently doing nothing.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Cancelable {
}
