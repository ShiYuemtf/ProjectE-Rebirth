package moze_intel.projecte.platform.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Client only half of {@link SimpleChannel}: hands a serialised payload to the server.
 */
final class ClientSender {

	static boolean canSend(ResourceLocation channel) {
		return net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.canSend(channel);
	}

	static void send(ResourceLocation channel, FriendlyByteBuf payload) {
		if (!ClientPlayNetworking.canSend(channel)) {
			//The server has no receiver for this channel (plain server, or the channel was not negotiated). Dropping the packet is
			//better than killing the client: the action simply does nothing server side.
			return;
		}
		ClientPlayNetworking.send(channel, payload);
	}

	private ClientSender() {
	}
}
