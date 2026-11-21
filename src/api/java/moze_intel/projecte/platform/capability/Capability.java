package moze_intel.projecte.platform.capability;

import java.util.Optional;
import moze_intel.projecte.platform.util.LazyOptional;

/**
 * Fabric side stand-in for Forge's {@code Capability}. Forge capabilities are existence by type on Fabric: the instance is a typed key, and objects that support
 * the capability simply implement the interface (see {@link CapabilityHelper} for the lookup path).
 */
public class Capability<T> {

	private final String name;
	private final Class<T> type;

	Capability(String name, Class<T> type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public Class<T> getType() {
		return type;
	}

	public boolean isInstance(Object instance) {
		return type.isInstance(instance);
	}

	public Optional<T> maybeCast(Object instance) {
		return type.isInstance(instance) ? Optional.of(type.cast(instance)) : Optional.empty();
	}

	public LazyOptional<T> orEmpty(LazyOptional<?> other) {
		return other.map(this.type::cast);
	}

	@Override
	public String toString() {
		return "Capability{" + name + "}";
	}
}
