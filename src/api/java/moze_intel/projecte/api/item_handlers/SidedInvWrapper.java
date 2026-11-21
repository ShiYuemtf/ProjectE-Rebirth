package moze_intel.projecte.api.item_handlers;

import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Exposes one face of a {@link WorldlyContainer} as an {@link IItemHandlerModifiable}, honoring
 * {@link WorldlyContainer#getSlotsForFace(Direction)} and the per-face insert/extract checks.
 */
public class SidedInvWrapper implements IItemHandlerModifiable {

	private final WorldlyContainer inv;
	@Nullable
	private final Direction side;
	private final int[] slots;

	public SidedInvWrapper(WorldlyContainer inv, @Nullable Direction side) {
		this.inv = inv;
		this.side = side;
		this.slots = inv.getSlotsForFace(side);
	}

	private boolean checkSlot(int localSlot) {
		return localSlot >= 0 && localSlot < slots.length;
	}

	@Override
	public int getSlots() {
		return slots.length;
	}

	@NotNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return checkSlot(slot) ? inv.getItem(slots[slot]) : ItemStack.EMPTY;
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		if (checkSlot(slot)) {
			inv.setItem(slots[slot], stack);
			inv.setChanged();
		}
	}

	@NotNull
	@Override
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		if (stack.isEmpty() || !isItemValid(slot, stack)) {
			return stack;
		}
		int index = slots[slot];
		ItemStack existing = inv.getItem(index);
		int limit = getSlotLimit(slot);
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
				inv.setItem(index, reachedLimit ? stack.copyWithCount(limit) : stack.copy());
			} else {
				existing.grow(reachedLimit ? limit : stack.getCount());
				inv.setItem(index, existing);
			}
			inv.setChanged();
		}
		return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
	}

	@NotNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount == 0 || !checkSlot(slot)) {
			return ItemStack.EMPTY;
		}
		int index = slots[slot];
		ItemStack existing = inv.getItem(index);
		if (existing.isEmpty() || !inv.canTakeItemThroughFace(index, existing, side)) {
			return ItemStack.EMPTY;
		}
		int toExtract = Math.min(amount, existing.getMaxStackSize());
		if (existing.getCount() <= toExtract) {
			if (!simulate) {
				inv.setItem(index, ItemStack.EMPTY);
				inv.setChanged();
			}
			return existing;
		}
		if (!simulate) {
			inv.setItem(index, existing.copyWithCount(existing.getCount() - toExtract));
			inv.setChanged();
		}
		return existing.copyWithCount(toExtract);
	}

	@Override
	public int getSlotLimit(int slot) {
		return inv.getMaxStackSize();
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return checkSlot(slot) && inv.canPlaceItemThroughFace(slots[slot], stack, side);
	}
}
