package moze_intel.projecte.platform.misc;

import javax.annotation.Nullable;
import net.minecraft.server.MinecraftServer;

/**
 * Fabric side stand-in for Forge's {@code ServerLifecycleHooks}. Fabric has no such holder, so the server is tracked by ProjectE's own lifecycle callbacks
 * ({@code ServerLifecycleEvents.SERVER_STARTING} / {@code SERVER_STOPPED}).
 */
public class ServerLifecycleHooks {

	@Nullable
	private static volatile MinecraftServer currentServer;

	public static void setCurrentServer(@Nullable MinecraftServer server) {
		currentServer = server;
	}

	@Nullable
	public static MinecraftServer getCurrentServer() {
		return currentServer;
	}
}
