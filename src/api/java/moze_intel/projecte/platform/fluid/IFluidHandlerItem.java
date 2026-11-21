package moze_intel.projecte.platform.fluid;

import net.minecraft.world.item.ItemStack;

/**
 * Fabric side stand-in for Forge's {@code IFluidHandlerItem}: a fluid handler owned by an item stack.
 */
public interface IFluidHandlerItem extends IFluidHandler {

	ItemStack getContainer();
}
