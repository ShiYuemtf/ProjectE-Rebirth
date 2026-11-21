package moze_intel.projecte.platform.misc;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Fabric side stand-in for Forge's {@code ModList}. Fabric has no mod list registry beyond the loader, so {@link #isLoaded(String)} forwards to it.
 */
public class ModList {

	private static final ModList INSTANCE = new ModList();

	public static ModList get() {
		return INSTANCE;
	}

	private ModList() {
	}

	public boolean isLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}
}
