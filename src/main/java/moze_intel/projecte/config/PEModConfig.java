package moze_intel.projecte.config;

import moze_intel.projecte.PECore;
import moze_intel.projecte.platform.config.ModConfigEvent;
import moze_intel.projecte.platform.misc.ModContainer;
import moze_intel.projecte.platform.config.ModConfig;

/**
 * Wraps one of our config specs so that the cached values can be dropped whenever Forge Config API Port (re)loads the file. Forge used to reroute the server config
 * out of the world folder; Forge Config API Port already keeps every config under {@code config/}, so the custom file handler is gone.
 */
public class PEModConfig {

	private final IPEConfig peConfig;
	private final ModConfig modConfig;

	public PEModConfig(ModContainer container, IPEConfig config) {
		this.peConfig = config;
		this.modConfig = container.addConfig(config.getConfigType(), config.getConfigSpec(), PECore.MODNAME + "-" + config.getFileName() + ".toml");
	}

	public ModConfig getConfig() {
		return modConfig;
	}

	public void clearCache(ModConfigEvent event) {
		peConfig.clearCache(event instanceof ModConfigEvent.Unloading);
	}
}
