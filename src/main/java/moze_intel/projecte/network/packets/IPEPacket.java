package moze_intel.projecte.network.packets;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import moze_intel.projecte.platform.network.NetworkEvent;
import moze_intel.projecte.platform.network.NetworkEvent.Context;

public interface IPEPacket {

	void handle(NetworkEvent.Context context);

	void encode(FriendlyByteBuf buffer);

	static <PACKET extends IPEPacket> void handle(final PACKET message, Supplier<NetworkEvent.Context> ctx) {
		Context context = ctx.get();
		context.enqueueWork(() -> message.handle(context));
		context.setPacketHandled(true);
	}
}