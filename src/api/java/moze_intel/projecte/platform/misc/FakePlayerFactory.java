package moze_intel.projecte.platform.misc;

import com.mojang.authlib.GameProfile;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * Fabric side stand-in for Forge's {@code FakePlayerFactory}: one fake player per level, keyed by dimension.
 */
public class FakePlayerFactory {

	private static final Map<Level, FakePlayer> FAKE_PLAYERS = new ConcurrentHashMap<>();

	public static FakePlayer get(ServerLevel level, GameProfile profile) {
		return FAKE_PLAYERS.computeIfAbsent(level, key -> new FakePlayer(level, profile));
	}

	public static void unloadLevel(ServerLevel level) {
		FAKE_PLAYERS.remove(level);
	}

	private FakePlayerFactory() {
	}
}
