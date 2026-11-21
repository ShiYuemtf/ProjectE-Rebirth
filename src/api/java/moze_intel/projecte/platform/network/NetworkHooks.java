package moze_intel.projecte.platform.network;

import io.netty.buffer.Unpooled;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Fabric side stand-in for Forge's {@code NetworkHooks}. Vanilla 1.20.1 has no way to hand extra opening data to a menu, so the menu is opened through fabric's
 * {@link ExtendedScreenHandlerFactory}: the extra payload travels on fabric's screen handler channel and the client side menu type has to be an
 * {@code ExtendedScreenHandlerType} to receive it.
 */
public class NetworkHooks {

	public static void openScreen(ServerPlayer player, MenuProvider provider, BlockPos pos) {
		//Block entity menus rebuild themselves on the client from the position, so it has to travel as opening data. Just calling openMenu(provider) left the
		//client reading an empty buffer, which is why every block entity GUI failed to open.
		openScreen(player, provider, buf -> buf.writeBlockPos(pos));
	}

	public static void openScreen(ServerPlayer player, MenuProvider provider, Consumer<FriendlyByteBuf> extraData) {
		player.openMenu(new ExtendedScreenHandlerFactory() {
			@Override
			public void writeScreenOpeningData(ServerPlayer serverPlayer, FriendlyByteBuf buf) {
				extraData.accept(buf);
			}

			@Override
			public Component getDisplayName() {
				return provider.getDisplayName();
			}

			@Override
			public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player menuPlayer) {
				return provider.createMenu(containerId, inventory, menuPlayer);
			}
		});
	}

	public static void openScreen(ServerPlayer player, MenuProvider provider) {
		player.openMenu(provider);
	}

	/**
	 * Forge uses this to attach a custom spawn packet to entities; vanilla 1.20.1 always sends {@code ClientboundAddEntityPacket}, so no override is needed.
	 */

	public static FriendlyByteBuf buffer() {
		return new FriendlyByteBuf(Unpooled.buffer());
	}

	public static void sendMessage(Player player, Component message) {
		player.displayClientMessage(message, false);
	}

	private NetworkHooks() {
	}
}
