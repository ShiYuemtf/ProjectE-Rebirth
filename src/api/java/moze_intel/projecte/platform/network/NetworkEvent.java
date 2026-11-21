package moze_intel.projecte.platform.network;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;

/**
 * Fabric side stand-in for Forge's {@code NetworkEvent.Context}: the per-packet information ProjectE's handlers need, plus the main-thread hop Forge performed
 * through {@code enqueueWork}.
 */
public class NetworkEvent {

	public static class Context {

		private final NetworkDirection direction;
		@Nullable
		private final ServerPlayer sender;

		public Context(NetworkDirection direction, @Nullable ServerPlayer sender) {
			this.direction = direction;
			this.sender = sender;
		}

		public NetworkDirection getDirection() {
			return direction;
		}

		@Nullable
		public ServerPlayer getSender() {
			return sender;
		}

		public void setPacketHandled(boolean handled) {
			// No acknowledgement protocol on this channel; kept for source compatibility with the ported handlers.
		}

		/**
		 * Runs the task on the thread that owns the current side, the way Forge's {@code enqueueWork} did.
		 */
		public void enqueueWork(Runnable task) {
			if (sender == null) {
				task.run();
			} else {
				sender.server.execute(task);
			}
		}
	}
}
