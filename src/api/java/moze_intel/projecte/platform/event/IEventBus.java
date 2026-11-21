package moze_intel.projecte.platform.event;

import java.util.function.Consumer;

/**
 * Fabric side stand-in for Forge's {@code IEventBus}.
 */
public interface IEventBus {

	<T extends Event> boolean post(T event);

	void register(Object target);

	<T extends Event> void addListener(Consumer<T> listener);

	<T extends Event> void addListener(EventPriority priority, Consumer<T> listener);
}
