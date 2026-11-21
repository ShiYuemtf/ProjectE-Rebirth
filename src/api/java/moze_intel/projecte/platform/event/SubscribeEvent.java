package moze_intel.projecte.platform.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a single argument method as an event handler, mirroring Forge's {@code @SubscribeEvent}. The declared parameter type decides which events are delivered.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubscribeEvent {

	EventPriority priority() default EventPriority.NORMAL;
}
