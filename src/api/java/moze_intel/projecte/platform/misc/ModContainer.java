package moze_intel.projecte.platform.misc;

import moze_intel.projecte.platform.config.ForgeConfigSpec;
import moze_intel.projecte.platform.config.ModConfig;

/**
 * Fabric side stand-in for Forge's {@code ModContainer}. Fabric has no mod container object, so this only carries the mod id and the config registration entry
 * point that {@code ProjectEConfig} used from Forge.
 */
public class ModContainer {

	private final String modId;

	public ModContainer(String modId) {
		this.modId = modId;
	}

	public String getModId() {
		return modId;
	}

	/**
	 * Creates the config object for this mod; ProjectEConfig is what loads and saves it.
	 */
	public ModConfig addConfig(ModConfig.Type type, ForgeConfigSpec spec, String fileName) {
		return new ModConfig(type, spec, modId, fileName);
	}
}
