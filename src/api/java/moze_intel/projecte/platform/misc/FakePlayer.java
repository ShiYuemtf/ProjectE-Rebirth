package moze_intel.projecte.platform.misc;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Fabric side stand-in for Forge's {@code FakePlayer}: a server side player used by ProjectE for actions that need a player context (Archangel's Smite, world
 * interactions from pedestals).
 */
public class FakePlayer extends ServerPlayer {

	public FakePlayer(ServerLevel level, GameProfile profile) {
		super(level.getServer(), level, profile);
	}
}
