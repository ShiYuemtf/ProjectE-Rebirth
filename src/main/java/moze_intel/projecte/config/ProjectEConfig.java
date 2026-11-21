package moze_intel.projecte.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import moze_intel.projecte.PECore;
import moze_intel.projecte.platform.config.ModConfigEvent;
import moze_intel.projecte.platform.misc.FMLPaths;

public class ProjectEConfig {

	public static final Path CONFIG_DIR;
	public static final ServerConfig server = new ServerConfig();
	public static final CommonConfig common = new CommonConfig();
	public static final ClientConfig client = new ClientConfig();

	private static final List<PEModConfig> CONFIGS = new ArrayList<>();

	static {
		CONFIG_DIR = FMLPaths.getOrCreateGameRelativePath(FMLPaths.CONFIGDIR.get().resolve(PECore.MODNAME));
	}

	public static void register() {
		registerConfig(server);
		registerConfig(common);
		registerConfig(client);
	}

	/**
	 * Creates a mod config so that Forge Config API Port tracks it and syncs server configs from server to client.
	 */
	public static void registerConfig(IPEConfig config) {
		PEModConfig peModConfig = new PEModConfig(PECore.MOD_CONTAINER, config);
		if (config.addToContainer()) {
			CONFIGS.add(peModConfig);
		}
	}

	/**
	 * Reads every registered config file from disk. Must run after {@link #register()}, because the specs only know their keys once all values are defined.
	 */
	public static void loadAll() {
		for (PEModConfig config : CONFIGS) {
			config.getConfig().load(CONFIG_DIR);
		}
	}

	public static void saveAll() {
		for (PEModConfig config : CONFIGS) {
			config.getConfig().save();
		}
	}

	/**
	 * Called from the Forge Config API Port loading/reloading/unloading callbacks that {@code PECore} bridges into {@link ModConfigEvent}s.
	 */
	public static void clearCache(ModConfigEvent event) {
		for (PEModConfig config : CONFIGS) {
			config.clearCache(event);
		}
	}
}
