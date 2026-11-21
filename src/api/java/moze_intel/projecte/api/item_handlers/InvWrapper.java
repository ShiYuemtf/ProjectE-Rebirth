package moze_intel.projecte.api.item_handlers;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Exposes a vanilla {@link Container} (a chest, a block entity inventory, ...) as an {@link IItemHandlerModifiable}.
 */
public class InvWrapper implements IItemHandlerModifiable {

	private final Container inv;

	public InvWrapper(Container inv) {
		this.inv = inv;
	}

	@Override
	public int getSlots() {
		return inv.getContainerSize();
	}

	@NotNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return inv.getItem(slot);
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		inv.setItem(slot, stack);
		inv.setChanged();
	}

	@NotNull
	@Override
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		if (stack.isEmpty() || !isItemValid(slot, stack)) {
			return stack;
		}
		ItemStack existing = inv.getItem(slot);
		int limit = getStackLimit(slot, stack);
		if (!existing.isEmpty()) {
			if (!ItemStack.isSameItemSameTags(existing, stack)) {
				return stack;
			}
			limit -= existing.getCount();
		}
		if (limit <= 0) {
			return stack;
		}
		boolean reachedLimit = stack.getCount() > limit;
		if (!simulate) {
			if (existing.isEmpty()) {
				inv.setItem(slot, reachedLimit ? stack.copyWithCount(limit) : stack.copy());
			} else {
				existing.grow(reachedLimit ? limit : stack.getCount());
				inv.setItem(slot, existing);
			}
			inv.setChanged();
		}
		return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
	}

	@NotNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount == 0) {
			return ItemStack.EMPTY;
		}
		ItemStack existing = inv.getItem(slot);
		if (existing.isEmpty()) {
			return ItemStack.EMPTY;
		}
		int toExtract = Math.min(amount, existing.getMaxStackSize());
		if (existing.getCount() <= toExtract) {
			if (!simulate) {
				inv.setItem(slot, ItemStack.EMPTY);
				inv.setChanged();
			}
			return existing;
		}
		if (!simulate) {
			inv.setItem(slot, existing.copyWithCount(existing.getCount() - toExtract));
			inv.setChanged();
		}
		return existing.copyWithCount(toExtract);
	}

	@Override
	public int getSlotLimit(int slot) {
		return inv.getMaxStackSize();
	}

	protected int getStackLimit(int slot, @NotNull ItemStack stack) {
		return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return inv.canPlaceItem(slot, stack);
	}
}
