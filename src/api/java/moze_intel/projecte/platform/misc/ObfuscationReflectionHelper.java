package moze_intel.projecte.platform.misc;

import java.lang.reflect.Field;
import javax.annotation.Nullable;

/**
 * Fabric side stand-in for Forge's {@code ObfuscationReflectionHelper}. On Fabric the runtime is already in named (Mojang) mappings, so the field name is used as
 * given; the {@code SRG} prefix convention is kept out of the way by rejecting {@code f_}/{@code m_} names up front so a bad call fails loudly at startup.
 */
public class ObfuscationReflectionHelper {

	@Nullable
	public static Field findField(Class<?> clazz, String name) {
		if (name.startsWith("f_")) {
			throw new IllegalArgumentException("SRG field name " + name + " is not usable on Fabric; use the Mojang mapped name");
		}
		Class<?> current = clazz;
		while (current != null) {
			try {
				Field field = current.getDeclaredField(name);
				field.setAccessible(true);
				return field;
			} catch (NoSuchFieldException ignored) {
				current = current.getSuperclass();
			}
		}
		return null;
	}

	public static <T, E> void setPrivateValue(Class<? super T> clazz, T instance, E value, String name) {
		Field field = findField(clazz, name);
		if (field == null) {
			throw new UnableToFindFieldException("Unable to find field " + name + " on " + clazz.getName());
		}
		try {
			field.set(instance, value);
		} catch (IllegalAccessException e) {
			throw new UnableToAccessFieldException("Unable to access field " + name + " on " + clazz.getName(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T, E> E getPrivateValue(Class<? super T> clazz, T instance, String name) {
		Field field = findField(clazz, name);
		if (field == null) {
			throw new UnableToFindFieldException("Unable to find field " + name + " on " + clazz.getName());
		}
		try {
			return (E) field.get(instance);
		} catch (IllegalAccessException e) {
			throw new UnableToAccessFieldException("Unable to access field " + name + " on " + clazz.getName(), e);
		}
	}

	private ObfuscationReflectionHelper() {
	}

	public static class UnableToFindFieldException extends RuntimeException {

		public UnableToFindFieldException(String message) {
			super(message);
		}
	}

	public static class UnableToAccessFieldException extends RuntimeException {

		public UnableToAccessFieldException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
