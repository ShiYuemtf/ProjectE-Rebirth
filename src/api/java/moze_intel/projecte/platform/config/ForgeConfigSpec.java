package moze_intel.projecte.platform.config;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Minimal stand-in for Forge's config spec, so ProjectE does not need Forge Config API Port at runtime.
 * <p>
 * Only the surface ProjectE actually uses is implemented: nested {@code push}/{@code pop} sections, comments, and the primitive/enum/list definitions. Values are
 * resolved lazily from the bound {@link TomlFile} and cached until the config is reloaded.
 */
public class ForgeConfigSpec {

	private final Map<String, ConfigValue<?>> values;
	private final Map<String, List<String>> comments;
	private TomlFile file;
	private boolean loaded;

	ForgeConfigSpec(Map<String, ConfigValue<?>> values, Map<String, List<String>> comments) {
		this.values = values;
		this.comments = comments;
	}

	void bind(TomlFile file) {
		this.file = file;
		for (java.util.Map.Entry<String, ConfigValue<?>> entry : values.entrySet()) {
			ConfigValue<?> value = entry.getValue();
			value.spec = this;
			value.path = entry.getKey();
			value.clearCache();
			if (file.get(entry.getKey()) == null) {
				//Materialise the default so the generated toml is actually editable instead of a bare header
				file.set(entry.getKey(), value.getDefault());
			}
		}
		this.loaded = true;
	}

	void unbind() {
		this.loaded = false;
		for (ConfigValue<?> value : values.values()) {
			value.clearCache();
		}
	}

	public boolean isLoaded() {
		return loaded;
	}

	public Map<String, ConfigValue<?>> getValues() {
		return values;
	}

	public Map<String, List<String>> getComments() {
		return comments;
	}

	@SuppressWarnings("unchecked")
	<T> T read(String path, T defaultValue) {
		if (file == null) {
			return defaultValue;
		}
		Object raw = file.get(path);
		if (raw == null) {
			return defaultValue;
		}
		return (T) TomlFile.coerce(raw, defaultValue);
	}

	void write(String path, Object value) {
		if (file != null) {
			file.set(path, value);
		}
	}

	public static class Builder {

		private final Deque<String> path = new ArrayDeque<>();
		private final List<String> pendingComment = new ArrayList<>();
		private final Map<String, ConfigValue<?>> values = new LinkedHashMap<>();
		private final Map<String, List<String>> comments = new LinkedHashMap<>();

		public Builder push(String section) {
			path.addLast(section);
			return this;
		}

		public Builder pop() {
			if (!path.isEmpty()) {
				path.removeLast();
			}
			return this;
		}

		public Builder pop(int count) {
			for (int i = 0; i < count; i++) {
				pop();
			}
			return this;
		}

		public Builder comment(String... comment) {
			pendingComment.addAll(Arrays.asList(comment));
			return this;
		}

		private String fullPath(String name) {
			if (path.isEmpty()) {
				return name;
			}
			return String.join(".", path) + "." + name;
		}

		private <T> ConfigValue<T> register(String name, T defaultValue, ConfigValue<T> value) {
			String full = fullPath(name);
			values.put(full, value);
			if (!pendingComment.isEmpty()) {
				comments.put(full, new ArrayList<>(pendingComment));
				pendingComment.clear();
			}
			return value;
		}

		public ConfigValue<Boolean> define(String name, boolean defaultValue) {
			return register(name, defaultValue, new BooleanValue(defaultValue));
		}

		public ConfigValue<Integer> define(String name, int defaultValue) {
			return register(name, defaultValue, new IntValue(defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE));
		}

		public ConfigValue<Long> define(String name, long defaultValue) {
			return register(name, defaultValue, new LongValue(defaultValue, Long.MIN_VALUE, Long.MAX_VALUE));
		}

		public ConfigValue<Double> define(String name, double defaultValue) {
			return register(name, defaultValue, new DoubleValue(defaultValue, Double.MIN_VALUE, Double.MAX_VALUE));
		}

		public ConfigValue<String> define(String name, String defaultValue) {
			return register(name, defaultValue, new ConfigValue<>(defaultValue));
		}

		public IntValue defineInRange(String name, int defaultValue, int min, int max) {
			IntValue value = new IntValue(defaultValue, min, max);
			register(name, defaultValue, value);
			return value;
		}

		public LongValue defineInRange(String name, long defaultValue, long min, long max) {
			LongValue value = new LongValue(defaultValue, min, max);
			register(name, defaultValue, value);
			return value;
		}

		public DoubleValue defineInRange(String name, double defaultValue, double min, double max) {
			DoubleValue value = new DoubleValue(defaultValue, min, max);
			register(name, defaultValue, value);
			return value;
		}

		public <T extends Enum<T>> EnumValue<T> defineEnum(String name, T defaultValue) {
			EnumValue<T> value = new EnumValue<>(defaultValue);
			register(name, defaultValue, value);
			return value;
		}

		public <T> ConfigValue<List<? extends T>> defineList(String name, List<? extends T> defaultValue, Predicate<Object> elementValidator) {
			ConfigValue<List<? extends T>> value = new ConfigValue<>(defaultValue);
			register(name, defaultValue, value);
			return value;
		}

		public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(String name, Supplier<List<? extends T>> defaultValue,
				Predicate<Object> elementValidator) {
			ConfigValue<List<? extends T>> value = new ConfigValue<>(defaultValue.get());
			register(name, defaultValue.get(), value);
			return value;
		}

		public <T> ConfigValue<T> defineInList(String name, T defaultValue, java.util.Collection<? extends T> acceptableValues) {
			return register(name, defaultValue, new ConfigValue<>(defaultValue));
		}

		public ForgeConfigSpec build() {
			return new ForgeConfigSpec(values, comments);
		}
	}

	public static class ConfigValue<T> {

		private final T defaultValue;
		private T cached;
		private boolean resolved;
		ForgeConfigSpec spec;
		String path;

		ConfigValue(T defaultValue) {
			this.defaultValue = defaultValue;
		}

		@SuppressWarnings("unchecked")
		public T get() {
			if (!resolved) {
				cached = spec == null ? defaultValue : (T) spec.read(path, defaultValue);
				resolved = true;
			}
			return cached;
		}

		public void set(T value) {
			cached = value;
			resolved = true;
			if (spec != null) {
				spec.write(path, value);
			}
		}

		public T getDefault() {
			return defaultValue;
		}

		public boolean isLoaded() {
			return spec != null && spec.isLoaded();
		}

		void clearCache() {
			resolved = false;
		}

		public String getPath() {
			return path;
		}
	}

	public static class BooleanValue extends ConfigValue<Boolean> {

		BooleanValue(Boolean defaultValue) {
			super(defaultValue);
		}
	}

	public static class IntValue extends ConfigValue<Integer> {

		private final int min;
		private final int max;

		IntValue(Integer defaultValue, int min, int max) {
			super(defaultValue);
			this.min = min;
			this.max = max;
		}

		@Override
		public Integer get() {
			return Math.max(min, Math.min(max, super.get()));
		}
	}

	public static class LongValue extends ConfigValue<Long> {

		private final long min;
		private final long max;

		LongValue(Long defaultValue, long min, long max) {
			super(defaultValue);
			this.min = min;
			this.max = max;
		}

		@Override
		public Long get() {
			return Math.max(min, Math.min(max, super.get()));
		}
	}

	public static class DoubleValue extends ConfigValue<Double> {

		private final double min;
		private final double max;

		DoubleValue(Double defaultValue, double min, double max) {
			super(defaultValue);
			this.min = min;
			this.max = max;
		}

		@Override
		public Double get() {
			return Math.max(min, Math.min(max, super.get()));
		}
	}

	public static class EnumValue<T extends Enum<T>> extends ConfigValue<T> {

		EnumValue(T defaultValue) {
			super(defaultValue);
		}
	}
}
