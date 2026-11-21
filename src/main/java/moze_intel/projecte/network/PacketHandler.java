package moze_intel.projecte.network;

import io.netty.buffer.Unpooled;
import java.util.Optional;
import java.util.function.Function;
import moze_intel.projecte.PECore;
import moze_intel.projecte.emc.EMCMappingHandler;
import moze_intel.projecte.emc.FuelMapper;
import moze_intel.projecte.network.packets.IPEPacket;
import moze_intel.projecte.network.packets.to_client.CooldownResetPKT;
import moze_intel.projecte.network.packets.to_client.SyncBagDataPKT;
import moze_intel.projecte.network.packets.to_client.SyncEmcPKT;
import moze_intel.projecte.network.packets.to_client.SyncEmcPKT.EmcPKTInfo;
import moze_intel.projecte.network.packets.to_client.SyncFuelMapperPKT;
import moze_intel.projecte.network.packets.to_client.UpdateCondenserLockPKT;
import moze_intel.projecte.network.packets.to_client.UpdateWindowIntPKT;
import moze_intel.projecte.network.packets.to_client.UpdateWindowLongPKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeClearPKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncChangePKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncEmcPKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncInputsAndLocksPKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncPKT;
import moze_intel.projecte.network.packets.to_client.knowledge.UpdateTransmutationTargetsPkt;
import moze_intel.projecte.network.packets.to_server.KeyPressPKT;
import moze_intel.projecte.network.packets.to_server.LeftClickArchangelPKT;
import moze_intel.projecte.network.packets.to_server.SearchUpdatePKT;
import moze_intel.projecte.network.packets.to_server.UpdateGemModePKT;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import moze_intel.projecte.platform.misc.FakePlayer;
import moze_intel.projecte.platform.network.NetworkDirection;
import moze_intel.projecte.platform.network.NetworkRegistry;
import moze_intel.projecte.platform.network.PacketDistributor;
import moze_intel.projecte.platform.network.SimpleChannel;
import moze_intel.projecte.platform.misc.ServerLifecycleHooks;

public final class PacketHandler {

	private static final String PROTOCOL_VERSION = Integer.toString(4);
	private static final SimpleChannel HANDLER = NetworkRegistry
			.named(PECore.rl("main_channel"))
			.clientAcceptedVersions(PROTOCOL_VERSION::equals)
			.serverAcceptedVersions(PROTOCOL_VERSION::equals)
			.networkProtocolVersion(() -> PROTOCOL_VERSION)
			.simpleChannel();
	private static int index;

	public static void register() {
		//Client to server messages
		registerClientToServer(KeyPressPKT.class, KeyPressPKT::decode);
		registerClientToServer(LeftClickArchangelPKT.class, LeftClickArchangelPKT::decode);
		registerClientToServer(SearchUpdatePKT.class, SearchUpdatePKT::decode);
		registerClientToServer(UpdateGemModePKT.class, UpdateGemModePKT::decode);

		//Server to client messages
		registerServerToClient(CooldownResetPKT.class, CooldownResetPKT::decode);
		registerServerToClient(KnowledgeClearPKT.class, KnowledgeClearPKT::decode);
		registerServerToClient(KnowledgeSyncPKT.class, KnowledgeSyncPKT::decode);
		registerServerToClient(KnowledgeSyncEmcPKT.class, KnowledgeSyncEmcPKT::decode);
		registerServerToClient(KnowledgeSyncInputsAndLocksPKT.class, KnowledgeSyncInputsAndLocksPKT::decode);
		registerServerToClient(KnowledgeSyncChangePKT.class, KnowledgeSyncChangePKT::decode);
		registerServerToClient(SyncBagDataPKT.class, SyncBagDataPKT::decode);
		registerServerToClient(SyncEmcPKT.class, SyncEmcPKT::decode);
		registerServerToClient(SyncFuelMapperPKT.class, SyncFuelMapperPKT::decode);
		registerServerToClient(UpdateCondenserLockPKT.class, UpdateCondenserLockPKT::decode);
		registerServerToClient(UpdateTransmutationTargetsPkt.class, UpdateTransmutationTargetsPkt::decode);
		registerServerToClient(UpdateWindowIntPKT.class, UpdateWindowIntPKT::decode);
		registerServerToClient(UpdateWindowLongPKT.class, UpdateWindowLongPKT::decode);

		//Fabric has no per-direction channel objects: the server half has to be hooked here so clients see the channel as sendable,
		//and the client half is hooked from ClientRegistration (it lives in a client-only class).
		HANDLER.registerServerReceiver();
		PECore.LOGGER.info("Registered {} ProjectE packets on {} (server receiver hooked)", index, PECore.rl("main_channel"));
	}

	/**
	 * Hooks the client half of the channel. Only call from the client initialiser.
	 */
	public static void registerClientReceiver() {
		HANDLER.registerClientReceiver();
		PECore.LOGGER.info("Hooked ProjectE client receiver on {}", PECore.rl("main_channel"));
	}

	private static <MSG extends IPEPacket> void registerClientToServer(Class<MSG> type, Function<FriendlyByteBuf, MSG> decoder) {
		registerMessage(type, decoder, NetworkDirection.PLAY_TO_SERVER);
	}

	private static <MSG extends IPEPacket> void registerServerToClient(Class<MSG> type, Function<FriendlyByteBuf, MSG> decoder) {
		registerMessage(type, decoder, NetworkDirection.PLAY_TO_CLIENT);
	}

	private static <MSG extends IPEPacket> void registerMessage(Class<MSG> type, Function<FriendlyByteBuf, MSG> decoder, NetworkDirection networkDirection) {
		HANDLER.registerMessage(index++, type, IPEPacket::encode, decoder, IPEPacket::handle, Optional.of(networkDirection));
	}

	public static <MSG extends IPEPacket> void sendNonLocal(MSG msg, ServerPlayer player) {
		//On Forge the client rebuilt its own EMC table from reload listeners, so the singleplayer owner was skipped.
		//Our client never maps EMC by itself, so the table has to reach every player including the local one,
		//otherwise the transmutation table cannot price anything and looks empty.
		sendTo(msg, player);
	}

	private static void sendFragmentedEmcPacket(ServerPlayer player, SyncEmcPKT pkt, SyncFuelMapperPKT fuelPkt) {
		sendTo(pkt, player);
		sendTo(fuelPkt, player);
	}

	public static void sendFragmentedEmcPacket(ServerPlayer player) {
		sendFragmentedEmcPacket(player, new SyncEmcPKT(serializeEmcData()), FuelMapper.getSyncPacket());
	}

	public static void sendFragmentedEmcPacketToAll() {
		if (ServerLifecycleHooks.getCurrentServer() != null) {
			SyncEmcPKT pkt = new SyncEmcPKT(serializeEmcData());
			SyncFuelMapperPKT fuelPkt = FuelMapper.getSyncPacket();
			for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
				sendFragmentedEmcPacket(player, pkt, fuelPkt);
			}
		}
	}

	private static EmcPKTInfo[] serializeEmcData() {
		EmcPKTInfo[] data = EMCMappingHandler.createPacketData();
		//Simulate encoding the EMC packet to get an accurate size
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		int index = buf.writerIndex();
		new SyncEmcPKT(data).encode(buf);
		PECore.debugLog("EMC data size: {} bytes", buf.writerIndex() - index);
		buf.release();
		return data;
	}

	/**
	 * Sends a packet to the server.<br> Must be called Client side.
	 */
	public static <MSG extends IPEPacket> void sendToServer(MSG msg) {
		HANDLER.sendToServer(msg);
	}

	/**
	 * Send a packet to a specific player.<br> Must be called Server side.
	 */
	public static <MSG extends IPEPacket> void sendTo(MSG msg, ServerPlayer player) {
		if (!(player instanceof FakePlayer)) {
			HANDLER.send(PacketDistributor.PLAYER.with(() -> player), msg);
		}
	}
}