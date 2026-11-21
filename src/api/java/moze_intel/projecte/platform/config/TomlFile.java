package moze_intel.projecte.platform.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Just enough TOML to read and write ProjectE's config files: {@code [section.sub]} headers, {@code key = value} pairs, {@code #} comments, and flat arrays.
 * <p>
 * Keys are flattened to {@code section.sub.key} so lookups never have to walk a tree. Numbers come back as {@link Long} or {@link Double}, booleans as
 * {@link Boolean}, strings as {@link String}, arrays as {@link List}.
 */
public class TomlFile {

	private final Map<String, Object> values = new LinkedHashMap<>();
	private final Map<String, List<String>> comments = new LinkedHashMap<>();
	private final Map<String, String> keyComments = new LinkedHashMap<>();
	private final Path path;

	private TomlFile(Path path) {
		this.path = path;
	}

	public static Builder builder(Path path) {
		return new Builder(path);
	}

	public Path getPath() {
		return path;
	}

	public Object get(String key) {
		return values.get(key);
	}

	public void set(String key, Object value) {
		values.put(key, value);
	}

	public void setComment(String key, String comment) {
		keyComments.put(key, comment);
	}

	/**
	 * Flushes to disk using the comments registered through {@link #setComment}. Used by the EMC mapping dump, which is not backed by a config spec.
	 */
	public void save() {
		Map<String, List<String>> merged = new LinkedHashMap<>(comments);
		keyComments.forEach((key, comment) -> merged.put(key, java.util.Collections.singletonList(comment)));
		save(merged);
	}

	public void close() {
		//Files are written eagerly in save(), nothing to release
	}

	public Map<String, Object> getValues() {
		return values;
	}

	public void load() {
		values.clear();
		if (!Files.exists(path)) {
			return;
		}
		String section = "";
		try {
			for (String rawLine : Files.readAllLines(path, StandardCharsets.UTF_8)) {
				String line = rawLine.trim();
				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}
				if (line.startsWith("[") && line.endsWith("]")) {
					section = line.substring(1, line.length() - 1).trim();
					continue;
				}
				int eq = line.indexOf('=');
				if (eq < 0) {
					continue;
				}
				String key = line.substring(0, eq).trim();
				//Strip any inline comment that is not inside a quoted string
				String rawValue = stripInlineComment(line.substring(eq + 1).trim());
				String full = section.isEmpty() ? key : section + "." + key;
				values.put(full, parseValue(rawValue));
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to read ProjectE config " + path, e);
		}
	}

	private static String stripInlineComment(String value) {
		boolean inString = false;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c == '"') {
				inString = !inString;
			} else if (c == '#' && !inString) {
				return value.substring(0, i).trim();
			}
		}
		return value;
	}

	private static Object parseValue(String value) {
		if (value.isEmpty()) {
			return "";
		}
		if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
			return value.substring(1, value.length() - 1);
		}
		if (value.startsWith("[") && value.endsWith("]")) {
			List<Object> list = new ArrayList<>();
			String inner = value.substring(1, value.length() - 1).trim();
			if (!inner.isEmpty()) {
				for (String element : splitArray(inner)) {
					list.add(parseValue(element.trim()));
				}
			}
			return list;
		}
		if ("true".equals(value)) {
			return Boolean.TRUE;
		}
		if ("false".equals(value)) {
			return Boolean.FALSE;
		}
		try {
			if (value.contains(".") || value.contains("e") || value.contains("E")) {
				return Double.parseDouble(value);
			}
			return Long.parseLong(value);
		} catch (NumberFormatException ignored) {
			return value;
		}
	}

	private static List<String> splitArray(String inner) {
		List<String> parts = new ArrayList<>();
		boolean inString = false;
		StringBuilder current = new StringBuilder();
		for (int i = 0; i < inner.length(); i++) {
			char c = inner.charAt(i);
			if (c == '"') {
				inString = !inString;
				current.append(c);
			} else if (c == ',' && !inString) {
				parts.add(current.toString());
				current.setLength(0);
			} else {
				current.append(c);
			}
		}
		if (current.length() > 0) {
			parts.add(current.toString());
		}
		return parts;
	}

	/**
	 * Converts a parsed TOML value to the type the config definition expects, tolerating the int/long and int/double mismatch TOML parsers always produce.
	 */
	@SuppressWarnings("unchecked")
	public static Object coerce(Object raw, Object defaultValue) {
		if (raw == null || defaultValue == null) {
			return raw;
		}
		if (defaultValue instanceof Boolean && raw instanceof Boolean) {
			return raw;
		}
		if (defaultValue instanceof Integer && raw instanceof Number number) {
			return number.intValue();
		}
		if (defaultValue instanceof Long && raw instanceof Number number) {
			return number.longValue();
		}
		if (defaultValue instanceof Double && raw instanceof Number number) {
			return number.doubleValue();
		}
		if (defaultValue instanceof Float && raw instanceof Number number) {
			return number.floatValue();
		}
		if (defaultValue instanceof String && raw instanceof String) {
			return raw;
		}
		if (defaultValue instanceof Enum<?> enumDefault && raw instanceof String text) {
			for (Enum<?> constant : enumDefault.getDeclaringClass().getEnumConstants()) {
				if (constant.name().equalsIgnoreCase(text)) {
					return constant;
				}
			}
			return defaultValue;
		}
		if (defaultValue instanceof List<?> defaultList && raw instanceof List<?> rawList) {
			List<Object> converted = new ArrayList<>();
			Object sample = defaultList.isEmpty() ? null : defaultList.get(0);
			for (Object element : rawList) {
				converted.add(sample == null ? element : coerce(element, sample));
			}
			return converted;
		}
		return defaultValue;
	}

	public void save(Map<String, List<String>> specComments) {
		//Group flat keys back into sections so the output stays readable and hand-editable
		Map<String, List<String>> sections = new LinkedHashMap<>();
		for (String key : values.keySet()) {
			int dot = key.lastIndexOf('.');
			String section = dot < 0 ? "" : key.substring(0, dot);
			sections.computeIfAbsent(section, s -> new ArrayList<>()).add(key);
		}
		StringBuilder out = new StringBuilder();
		out.append("# ProjectE configuration\n");
		for (Map.Entry<String, List<String>> entry : sections.entrySet()) {
			out.append('\n');
			if (!entry.getKey().isEmpty()) {
				out.append('[').append(entry.getKey()).append("]\n");
			}
			for (String key : entry.getValue()) {
				List<String> comments = specComments.get(key);
				if (comments != null) {
					for (String comment : comments) {
						for (String line : comment.split("\n")) {
							out.append("\t# ").append(line).append('\n');
						}
					}
				}
				String shortKey = entry.getKey().isEmpty() ? key : key.substring(entry.getKey().length() + 1);
				out.append('\t').append(shortKey).append(" = ").append(format(values.get(key))).append('\n');
			}
		}
		try {
			Path parent = path.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			Files.write(path, out.toString().getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new RuntimeException("Failed to write ProjectE config " + path, e);
		}
	}

	private static String format(Object value) {
		if (value instanceof String text) {
			return '"' + text.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
		}
		if (value instanceof Enum<?> constant) {
			return '"' + constant.name() + '"';
		}
		if (value instanceof List<?> list) {
			StringBuilder builder = new StringBuilder("[");
			for (int i = 0; i < list.size(); i++) {
				if (i > 0) {
					builder.append(", ");
				}
				builder.append(format(list.get(i)));
			}
			return builder.append(']').toString();
		}
		return String.valueOf(value);
	}

	public static class Builder {

		private final Path path;

		private Builder(Path path) {
			this.path = path;
		}

		public TomlFile build() {
			return new TomlFile(path);
		}
	}
}
