package moze_intel.projecte.platform.misc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fabric side stand-in for Forge's {@code FMLPaths}.
 */
public class FMLPaths {

	private static final Logger LOGGER = LoggerFactory.getLogger("projecte/paths");

	public static final PathHolder CONFIGDIR = new PathHolder(FabricLoader.getInstance().getConfigDir());
	public static final PathHolder GAMEDIR = new PathHolder(FabricLoader.getInstance().getGameDir());

	public static Path getOrCreateGameRelativePath(Path path) {
		return getOrCreateDirectory(path, path.toString());
	}

	public static Path getOrCreateDirectory(Path path, String name) {
		if (!Files.isDirectory(path)) {
			try {
				Files.createDirectories(path);
			} catch (IOException ex) {
				throw new RuntimeException("Failed to create directory " + name, ex);
			}
		}
		return path;
	}

	public record PathHolder(Path path) {

		public Path get() {
			return path;
		}
	}

	private FMLPaths() {
	}
}
