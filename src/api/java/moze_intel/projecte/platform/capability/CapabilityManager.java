package moze_intel.projecte.platform.capability;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CapabilityManager {

	private static final Map<Class<?>, Capability<?>> CAPABILITIES = new ConcurrentHashMap<>();

	private CapabilityManager() {
	}

	@SuppressWarnings("unchecked")
	public static <T> Capability<T> get(CapabilityToken<T> token) {
		return get(token.resolveType());
	}

	@SuppressWarnings("unchecked")
	public static <T> Capability<T> get(Class<T> type) {
		return (Capability<T>) CAPABILITIES.computeIfAbsent(type, key -> new Capability<>(key.getName(), (Class<T>) key));
	}
}
