package moze_intel.projecte.gameObjs.container.slots;

import net.minecraft.world.item.ItemStack;
import moze_intel.projecte.api.item_handlers.IItemHandler;
import moze_intel.projecte.api.item_handlers.SlotItemHandler;

public class InventoryContainerSlot extends SlotItemHandler implements IInsertableSlot {

    public InventoryContainerSlot(IItemHandler itemHandler, int index, int x, int y) {
        super(itemHandler, index, x, y);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Math.min(getMaxStackSize(), stack.getMaxStackSize());
    }
}