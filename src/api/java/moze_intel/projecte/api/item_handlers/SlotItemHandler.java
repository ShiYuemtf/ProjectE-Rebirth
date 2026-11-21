package moze_intel.projecte.api.item_handlers;

import javax.annotation.Nonnull;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A {@link Slot} backed by an {@link IItemHandler} instead of a {@link Container}. Port of Forge's {@code SlotItemHandler}; the vanilla slot delegates to a
 * container, so every accessor here is routed to the handler and {@link #setChanged()} reports back to it.
 */
public class SlotItemHandler extends Slot {

	private final IItemHandler itemHandler;
	protected final int index;

	public SlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		//0 sized container: Slot's constructor only stores the reference, all access goes through the handler
		super(new net.minecraft.world.SimpleContainer(0), index, xPosition, yPosition);
		this.itemHandler = itemHandler;
		this.index = index;
	}

	@Nonnull
	@Override
	public ItemStack getItem() {
		return itemHandler.getStackInSlot(index);
	}

	@Override
	public void set(@Nonnull ItemStack stack) {
		if (itemHandler instanceof IItemHandlerModifiable modifiable) {
			modifiable.setStackInSlot(index, stack);
		} else {
			itemHandler.extractItem(index, Integer.MAX_VALUE, false);
			if (!stack.isEmpty()) {
				itemHandler.insertItem(index, stack, false);
			}
		}
		this.setChanged();
	}

	public void initialize(@Nonnull ItemStack stack) {
		//shortcut: 1.20.1 Fabric has no distinct initialize pass, so this collapses to set
		this.set(stack);
	}

	@Override
	public boolean mayPlace(@Nonnull ItemStack stack) {
		return itemHandler.isItemValid(index, stack);
	}

	@Override
	public boolean mayPickup(Player player) {
		return !itemHandler.extractItem(index, 1, true).isEmpty();
	}

	@Override
	public void onTake(Player player, @Nonnull ItemStack stack) {
		itemHandler.extractItem(index, stack.getCount(), false);
		super.onTake(player, stack);
	}

	@Nonnull
	@Override
	public ItemStack remove(int amount) {
		return itemHandler.extractItem(index, amount, false);
	}

	@Override
	public int getMaxStackSize() {
		return itemHandler.getSlotLimit(index);
	}

	@Override
	public int getMaxStackSize(@Nonnull ItemStack stack) {
		return Math.min(getMaxStackSize(), stack.getMaxStackSize());
	}

	public boolean isSameInventory(Slot other) {
		return other instanceof SlotItemHandler slotItemHandler && slotItemHandler.itemHandler == this.itemHandler;
	}

	@Override
	public int getContainerSlot() {
		return index;
	}

	@Override
	public void setChanged() {
		itemHandler.markSlotChanged(index);
	}

	/**
	 * Index of this slot inside the backing handler.
	 */
	public int getSlotIndex() {
		return index;
	}
}
