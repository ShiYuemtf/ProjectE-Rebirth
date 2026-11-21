package moze_intel.projecte.platform.network;

import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Fabric side stand-in for Forge's {@code NetworkRegistry.ChannelBuilder}. The protocol version predicate has nothing to check against on Fabric (channels are
 * matched by name through the loader), so it is accepted and ignored.
 */
public class NetworkRegistry {

	public static ChannelBuilder named(ResourceLocation channel) {
		return new ChannelBuilder(channel);
	}

	public static class ChannelBuilder {

		private final ResourceLocation channel;

		private ChannelBuilder(ResourceLocation channel) {
			this.channel = channel;
		}

		public ChannelBuilder clientAcceptedVersions(java.util.function.Predicate<String> predicate) {
			return this;
		}

		public ChannelBuilder serverAcceptedVersions(java.util.function.Predicate<String> predicate) {
			return this;
		}

		public ChannelBuilder networkProtocolVersion(java.util.function.Supplier<String> version) {
			return this;
		}

		public SimpleChannel simpleChannel() {
			return new SimpleChannel(channel);
		}
	}

	private NetworkRegistry() {
	}

	static Optional<ServerPlayer> emptyTarget() {
		return Optional.empty();
	}
}
