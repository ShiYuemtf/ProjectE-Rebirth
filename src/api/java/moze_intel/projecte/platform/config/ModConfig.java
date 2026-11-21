package moze_intel.projecte.platform.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A single config file (client/common/server) bound to a {@link ForgeConfigSpec}. Replaces Forge Config API Port's ModConfig so the mod has no config-library
 * dependency at runtime.
 * <p>
 * Load order matters: {@link #load(Path)} binds the spec to the parsed file before any value is read, and {@link #save()} writes the current in-memory values back.
 */
public class ModConfig {

	public enum Type {
		CLIENT,
		COMMON,
		SERVER,
		STARTUP
	}

	private static final List<ModConfig> ALL = new ArrayList<>();
	private static final List<Consumer<ModConfig>> LOAD_LISTENERS = new ArrayList<>();
	private static final List<Consumer<ModConfig>> UNLOAD_LISTENERS = new ArrayList<>();

	private final Type type;
	private final ForgeConfigSpec spec;
	private final String modId;
	private final String fileName;
	private TomlFile file;

	public ModConfig(Type type, ForgeConfigSpec spec, String modId, String fileName) {
		this.type = type;
		this.spec = spec;
		this.modId = modId;
		this.fileName = fileName;
	}

	public Type getType() {
		return type;
	}

	public ForgeConfigSpec getSpec() {
		return spec;
	}

	public String getModId() {
		return modId;
	}

	public String getFileName() {
		return fileName;
	}

	public TomlFile getFile() {
		return file;
	}

	public void load(Path configDir) {
		Path target = configDir.resolve(fileName);
		file = TomlFile.builder(target).build();
		file.load();
		spec.bind(file);
		//Write back immediately: bind() fills in any key the file was missing
		file.save(spec.getComments());
		if (!Files.exists(target)) {
			//First run: materialise the defaults so the user has something to edit
			save();
		}
		ALL.add(this);
		LOAD_LISTENERS.forEach(listener -> listener.accept(this));
	}

	public void save() {
		if (file != null) {
			file.save(spec.getComments());
		}
	}

	public void unload() {
		UNLOAD_LISTENERS.forEach(listener -> listener.accept(this));
		spec.unbind();
		file = null;
		ALL.remove(this);
	}

	public static void onLoad(Consumer<ModConfig> listener) {
		LOAD_LISTENERS.add(listener);
	}

	public static void onUnload(Consumer<ModConfig> listener) {
		UNLOAD_LISTENERS.add(listener);
	}

	public static void saveAll() {
		for (ModConfig config : new ArrayList<>(ALL)) {
			config.save();
		}
	}

	public static void unloadAll() {
		for (ModConfig config : new ArrayList<>(ALL)) {
			config.unload();
		}
	}
}
