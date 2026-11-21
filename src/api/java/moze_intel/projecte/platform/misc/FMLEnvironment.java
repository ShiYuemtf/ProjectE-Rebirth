package moze_intel.projecte.platform.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Fabric side stand-in for Forge's {@code FMLEnvironment}.
 */
public class FMLEnvironment {

	public static final Dist dist = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? Dist.CLIENT : Dist.DEDICATED_SERVER;
	public static final boolean production = !FabricLoader.getInstance().isDevelopmentEnvironment();

	public static boolean distIsClient() {
		return dist == Dist.CLIENT;
	}

	private FMLEnvironment() {
	}
}
