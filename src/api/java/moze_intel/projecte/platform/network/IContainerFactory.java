package moze_intel.projecte.platform.network;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Fabric side stand-in for Forge's {@code IContainerFactory}. The menu type registered by {@code ContainerTypeDeferredRegister} is an
 * {@link ExtendedScreenHandlerType}, so the factory has to be an {@link ExtendedScreenHandlerType.ExtendedFactory}: that is what receives the opening data the
 * server wrote with {@code NetworkHooks.openScreen(player, provider, extraData)}.
 */
@FunctionalInterface
public interface IContainerFactory<T extends AbstractContainerMenu> extends ExtendedScreenHandlerType.ExtendedFactory<T> {

	@Override
	T create(int windowId, Inventory playerInventory, FriendlyByteBuf data);
}
