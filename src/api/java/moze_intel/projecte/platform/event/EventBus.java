package moze_intel.projecte.platform.event;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Fabric side replacement for Forge's event bus. Scope is deliberately narrow: it only carries ProjectE's own API events (see
 * {@code moze_intel.projecte.api.event.*}) and the few internal ones, so no mod-load-time event phases are implemented.
 * <p>
 * Type resolution: {@link #register(Object)} reads {@code @SubscribeEvent} parameter types by reflection. Registration through {@link #addListener(Consumer)}
 * cannot see the lambda's type parameter because of erasure, so those listeners are tried against every event and skipped when the lambda's own cast rejects
 * them.
 */
public class EventBus implements IEventBus {

	private final Map<Class<?>, List<Listener>> byType = new ConcurrentHashMap<>();
	private final List<Listener> untyped = new CopyOnWriteArrayList<>();

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Event> boolean post(T event) {
		List<Listener> exact = byType.get(event.getClass());
		if (exact != null) {
			for (Listener listener : exact) {
				listener.consumer.accept(event);
			}
		}
		for (Map.Entry<Class<?>, List<Listener>> entry : byType.entrySet()) {
			if (entry.getKey() == event.getClass() || !entry.getKey().isInstance(event)) {
				continue;
			}
			for (Listener listener : entry.getValue()) {
				listener.consumer.accept(event);
			}
		}
		for (Listener listener : untyped) {
			try {
				listener.consumer.accept(event);
			} catch (ClassCastException ignored) {
				// The lambda declared a different event type; erasure hid it at registration time.
			}
		}
		return event.isCanceled();
	}

	@Override
	public void register(Object target) {
		Class<?> type = target.getClass();
		for (Method method : type.getMethods()) {
			SubscribeEvent annotation = method.getAnnotation(SubscribeEvent.class);
			if (annotation == null || method.getParameterCount() != 1) {
				continue;
			}
			Class<?> eventType = method.getParameterTypes()[0];
			Consumer<Object> consumer = event -> invoke(method, target, event);
			add(eventType, new Listener(annotation.priority(), consumer));
		}
	}

	private static void invoke(Method method, Object target, Object event) {
		try {
			if (Modifier.isStatic(method.getModifiers())) {
				method.invoke(null, event);
			} else {
				method.invoke(target, event);
			}
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed to dispatch " + event.getClass().getName() + " to " + method, e);
		}
	}

	@Override
	public <T extends Event> void addListener(Consumer<T> listener) {
		addListener(EventPriority.NORMAL, listener);
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <T extends Event> void addListener(EventPriority priority, Consumer<T> listener) {
		//Erasure hides the lambda's event type, so it is registered against every event and skipped when its own cast rejects the event (see post()).
		untyped.add(new Listener(priority, (Consumer) listener));
	}

	private void add(Class<?> eventType, Listener listener) {
		List<Listener> listeners = byType.computeIfAbsent(eventType, key -> new CopyOnWriteArrayList<>());
		listeners.add(listener);
		listeners.sort((first, second) -> Integer.compare(first.priority().ordinal(), second.priority().ordinal()));
	}

	private record Listener(EventPriority priority, Consumer<Object> consumer) {
	}
}
