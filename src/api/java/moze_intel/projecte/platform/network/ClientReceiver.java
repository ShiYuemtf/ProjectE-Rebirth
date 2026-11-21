package moze_intel.projecte.platform.network;

import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Client only half of {@link SimpleChannel}. Kept in its own class so the common initialiser never loads a client networking type on a dedicated server.
 */
final class ClientReceiver {

	static void register(ResourceLocation channel, Consumer<FriendlyByteBuf> handler) {
		ClientPlayNetworking.registerGlobalReceiver(channel, (client, listener, buf, responseSender) -> {
			//Same reasoning as the server half: the handler already runs on the client thread, and deferring would reorder it against vanilla packets
			handler.accept(new FriendlyByteBuf(buf.copy()));
		});
	}

	private ClientReceiver() {
	}
}
