package moze_intel.projecte.platform.capability;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Fabric side stand-in for Forge's {@code CapabilityToken}. Anonymous subclasses carry the capability type in their generic superclass signature, which is how
 * Forge resolves the type; {@link CapabilityManager} reads the same information here.
 */
public abstract class CapabilityToken<T> {

	@SuppressWarnings("unchecked")
	Class<T> resolveType() {
		Class<?> current = getClass();
		// Walk up anonymous subclasses until we find the class whose generic superclass is a plain CapabilityToken
		while (current != null && current != CapabilityToken.class) {
			Type genericSuperclass = current.getGenericSuperclass();
			if (genericSuperclass instanceof ParameterizedType parameterized) {
				Type[] arguments = parameterized.getActualTypeArguments();
				if (arguments.length == 1 && arguments[0] instanceof Class<?> argument && argument != Object.class) {
					return (Class<T>) argument;
				}
				if (arguments.length == 1 && arguments[0] instanceof ParameterizedType nested) {
					Type raw = nested.getRawType();
					if (raw instanceof Class<?> rawClass && rawClass != Object.class) {
						return (Class<T>) rawClass;
					}
				}
			}
			current = current.getSuperclass();
		}
		throw new IllegalStateException("Unable to resolve capability type from " + getClass()
				+ "; use CapabilityManager.get(Class) instead of an anonymous CapabilityToken");
	}
}
