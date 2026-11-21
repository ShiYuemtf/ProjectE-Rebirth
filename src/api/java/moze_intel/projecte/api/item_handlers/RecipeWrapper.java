package moze_intel.projecte.api.item_handlers;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Presents an {@link IItemHandler} as a vanilla {@link Container} so recipes that expect a container (vanilla smelting, in ProjectE's case) can be
 * matched against handler backed inventories.
 */
public class RecipeWrapper implements Container {

	private final IItemHandler inv;

	public RecipeWrapper(IItemHandler inv) {
		this.inv = inv;
	}

	@Override
	public int getContainerSize() {
		return inv.getSlots();
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < inv.getSlots(); i++) {
			if (!inv.getStackInSlot(i).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@NotNull
	@Override
	public ItemStack getItem(int slot) {
		return inv.getStackInSlot(slot);
	}

	@NotNull
	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack extracted = inv.extractItem(slot, amount, false);
		return extracted == null ? ItemStack.EMPTY : extracted;
	}

	@NotNull
	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return removeItem(slot, inv.getStackInSlot(slot).getCount());
	}

	@Override
	public void setItem(int slot, @NotNull ItemStack stack) {
		if (inv instanceof IItemHandlerModifiable modifiable) {
			modifiable.setStackInSlot(slot, stack);
		} else {
			inv.extractItem(slot, inv.getStackInSlot(slot).getCount(), false);
			if (!stack.isEmpty()) {
				inv.insertItem(slot, stack, false);
			}
		}
	}

	@Override
	public int getMaxStackSize() {
		return 64;
	}

	@Override
	public void setChanged() {
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return true;
	}

	@Override
	public void clearContent() {
		for (int i = 0; i < inv.getSlots(); i++) {
			inv.extractItem(i, inv.getStackInSlot(i).getCount(), false);
		}
	}
}
