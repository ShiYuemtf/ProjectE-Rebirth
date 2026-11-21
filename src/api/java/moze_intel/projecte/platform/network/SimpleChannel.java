package moze_intel.projecte.platform.network;

import io.netty.buffer.Unpooled;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Fabric side stand-in for Forge's {@code SimpleChannel}. One named {@code play} channel carries every ProjectE packet, each prefixed with its registration index;
 * Forge's protocol version negotiation is dropped because Fabric matches channels by name through the loader.
 */
public class SimpleChannel {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("projecte-network");
	private static final java.util.concurrent.atomic.AtomicBoolean DROPPED_WARNED = new java.util.concurrent.atomic.AtomicBoolean();

	private final ResourceLocation channel;
	private final Map<Class<?>, Entry> byType = new ConcurrentHashMap<>();
	private final Map<Integer, Entry> byIndex = new ConcurrentHashMap<>();

	public SimpleChannel(ResourceLocation channel) {
		this.channel = channel;
	}

	public <T> void registerMessage(int index, Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder,
			BiConsumer<T, Supplier<NetworkEvent.Context>> handler, Optional<NetworkDirection> direction) {
		NetworkDirection networkDirection = direction.orElse(NetworkDirection.PLAY_TO_CLIENT);
		Entry entry = new Entry(index, type, castEncoder(encoder), castDecoder(decoder), castHandler(handler), networkDirection);
		byType.put(type, entry);
		byIndex.put(index, entry);
	}

	@SuppressWarnings("unchecked")
	private static BiConsumer<Object, FriendlyByteBuf> castEncoder(BiConsumer<?, FriendlyByteBuf> encoder) {
		return (BiConsumer<Object, FriendlyByteBuf>) encoder;
	}

	@SuppressWarnings("unchecked")
	private static Function<FriendlyByteBuf, Object> castDecoder(Function<FriendlyByteBuf, ?> decoder) {
		return (Function<FriendlyByteBuf, Object>) decoder;
	}

	@SuppressWarnings("unchecked")
	private static BiConsumer<Object, Supplier<NetworkEvent.Context>> castHandler(BiConsumer<?, Supplier<NetworkEvent.Context>> handler) {
		return (BiConsumer<Object, Supplier<NetworkEvent.Context>>) handler;
	}

	/**
	 * Hooks the server side receiver. Called from the common initialiser, so it must stay free of client only types.
	 */
	public void registerServerReceiver() {
		ServerPlayNetworking.registerGlobalReceiver(channel, (server, player, handler, buf, responseSender) -> {
			//Fabric already runs channel handlers on the server thread, so dispatching here keeps the packet in the same tick as the vanilla packet it
			//was sent alongside. Deferring through server.execute() pushed it to the next tick, which broke ordering against the container click packet.
			dispatch(new FriendlyByteBuf(buf.copy()), player, NetworkDirection.PLAY_TO_SERVER);
		});
	}

	/**
	 * Hooks the client side receiver. Must only be called from the client initialiser; the client networking class lives in a separate holder for that reason.
	 */
	public void registerClientReceiver() {
		ClientReceiver.register(channel, buf -> dispatch(buf, null, NetworkDirection.PLAY_TO_CLIENT));
	}

	public <T> void send(PacketDistributor.PacketTarget target, T packet) {
		target.send(this, packet);
	}

	public <T> void sendToServer(T packet) {
		Entry entry = entryFor(packet);
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		buf.writeVarInt(entry.index());
		entry.encoder().accept(packet, buf);
		ClientSender.send(channel, buf);
	}

	void sendToPlayer(ServerPlayer player, Object packet) {
		Entry entry = entryFor(packet);
		if (!ServerPlayNetworking.canSend(player, channel)) {
			//The client cannot receive on this channel yet (it never sent minecraft:register, or the mod is absent). Silently
			// dropping every packet shows up in game as an empty transmutation table and zero EMC, so say it once.
			if (DROPPED_WARNED.compareAndSet(false, true)) {
				LOGGER.warn("Client {} cannot receive on channel {}; ProjectE sync packets are being dropped. "
						+ "The transmutation table will stay locked until the client registers the channel.", player.getGameProfile().getName(), channel);
			}
			return;
		}
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		buf.writeVarInt(entry.index());
		entry.encoder().accept(packet, buf);
		ServerPlayNetworking.send(player, channel, buf);
	}

	private Entry entryFor(Object packet) {
		Entry entry = byType.get(packet.getClass());
		if (entry == null) {
			throw new IllegalArgumentException("Unregistered packet type " + packet.getClass().getName());
		}
		return entry;
	}

	private void dispatch(FriendlyByteBuf buf, @Nullable ServerPlayer sender, NetworkDirection direction) {
		int index = buf.readVarInt();
		Entry entry = byIndex.get(index);
		if (entry == null || entry.direction() != direction) {
			throw new IllegalStateException("Unknown packet index " + index + " for " + channel + " (" + direction + ")");
		}
		Object packet = entry.decoder().apply(buf);
		NetworkEvent.Context context = new NetworkEvent.Context(direction, sender);
		entry.handler().accept(packet, () -> context);
	}

	private record Entry(int index, Class<?> type, BiConsumer<Object, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, Object> decoder,
						 BiConsumer<Object, Supplier<NetworkEvent.Context>> handler, NetworkDirection direction) {
	}
}
