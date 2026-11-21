package moze_intel.projecte.platform.network;

/**
 * Fabric side stand-in for Forge's {@code PacketDistributor}: only the "send to this player" and "send to the server" targets are used by ProjectE.
 */
public class PacketDistributor {

	public static final PlayerTarget PLAYER = new PlayerTarget();
	public static final NoArgTarget ALL = new NoArgTarget();
	public static final NoArgTarget SERVER = new NoArgTarget();

	@FunctionalInterface
	public interface PacketTarget {

		void send(SimpleChannel channel, Object packet);
	}

	public static class PlayerTarget {

		public PacketTarget with(java.util.function.Supplier<net.minecraft.server.level.ServerPlayer> player) {
			return (channel, packet) -> channel.sendToPlayer(player.get(), packet);
		}
	}

	public static class NoArgTarget {

		public PacketTarget noArg() {
			return (channel, packet) -> channel.sendToServer(packet);
		}
	}

	private PacketDistributor() {
	}
}
