package moze_intel.projecte.gameObjs.block_entities;

import java.util.Optional;
import moze_intel.projecte.api.item_handlers.IItemHandler;
import moze_intel.projecte.api.item_handlers.IItemHandlerModifiable;
import moze_intel.projecte.capability.managing.ICapabilityResolver;
import moze_intel.projecte.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import moze_intel.projecte.platform.capability.Capability;
import moze_intel.projecte.platform.capability.CompatCapabilities;
import moze_intel.projecte.platform.util.LazyOptional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

/**
 * Shared base for every ProjectE block entity that exposes an {@link IItemHandler}.
 * <p>
 * Also implements {@link WorldlyContainer}: Forge patched {@code IItemHandler} support into {@code HopperBlockEntity}, Fabric does not, so without a vanilla
 * container view hoppers and droppers cannot see any of these machines at all. The vanilla methods below just delegate to whatever the item handler resolver
 * answers for the requested side, which keeps the per-face slot rules of the collectors and relays intact.
 */
public abstract class CapabilityEmcBlockEntity extends EmcBlockEntity implements WorldlyContainer {

	@Nullable
	protected ICapabilityResolver<IItemHandler> itemHandlerResolver;

	public CapabilityEmcBlockEntity(BlockEntityTypeRegistryObject<? extends CapabilityEmcBlockEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public CapabilityEmcBlockEntity(BlockEntityTypeRegistryObject<? extends CapabilityEmcBlockEntity> type, BlockPos pos, BlockState state,
			@Range(from = 1, to = Long.MAX_VALUE)long maxAmount) {
		super(type, pos, state, maxAmount);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		if (itemHandlerResolver != null) {
			//Should never be null but just in case
			itemHandlerResolver.invalidateAll();
		}
	}

	@NotNull
	@Override
	public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
		if (cap == CompatCapabilities.ITEM_HANDLER && itemHandlerResolver != null) {
			//Should never be null but just in case
			return itemHandlerResolver.getCapabilityUnchecked(cap, side);
		}
		//Must fall through to EmcBlockEntity/BaseEmcBlockEntity, which answers EMC_STORAGE_CAPABILITY with itself.
		//Returning empty here broke every block-to-block emc transfer (collector -> condenser, relay pushing onwards).
		return super.getCapability(cap, side);
	}

	@Nullable
	private IItemHandler handler(@Nullable Direction side) {
		if (itemHandlerResolver == null) {
			return null;
		}
		Optional<IItemHandler> resolved = itemHandlerResolver.getCapabilityUnchecked(CompatCapabilities.ITEM_HANDLER, side).resolve();
		return resolved.orElse(null);
	}

	// --- WorldlyContainer / Container -----------------------------------------------------

	@NotNull
	@Override
	public int[] getSlotsForFace(@NotNull Direction side) {
		IItemHandler handler = handler(side);
		if (handler == null) {
			return new int[0];
		}
		int[] slots = new int[handler.getSlots()];
		for (int i = 0; i < slots.length; i++) {
			slots[i] = i;
		}
		return slots;
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, @NotNull ItemStack stack, @Nullable Direction side) {
		IItemHandler handler = handler(side);
		return handler != null && slot >= 0 && slot < handler.getSlots() && handler.insertItem(slot, stack, true).getCount() < stack.getCount();
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, @NotNull ItemStack stack, @NotNull Direction side) {
		IItemHandler handler = handler(side);
		return handler != null && slot >= 0 && slot < handler.getSlots() && !handler.extractItem(slot, 1, true).isEmpty();
	}

	@Override
	public int getContainerSize() {
		IItemHandler handler = handler(null);
		return handler == null ? 0 : handler.getSlots();
	}

	@Override
	public boolean isEmpty() {
		IItemHandler handler = handler(null);
		if (handler == null) {
			return true;
		}
		for (int i = 0; i < handler.getSlots(); i++) {
			if (!handler.getStackInSlot(i).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@NotNull
	@Override
	public ItemStack getItem(int slot) {
		IItemHandler handler = handler(null);
		return handler == null || slot < 0 || slot >= handler.getSlots() ? ItemStack.EMPTY : handler.getStackInSlot(slot);
	}

	@NotNull
	@Override
	public ItemStack removeItem(int slot, int amount) {
		IItemHandler handler = handler(null);
		return handler == null || slot < 0 || slot >= handler.getSlots() ? ItemStack.EMPTY : handler.extractItem(slot, amount, false);
	}

	@NotNull
	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		IItemHandler handler = handler(null);
		if (handler == null || slot < 0 || slot >= handler.getSlots()) {
			return ItemStack.EMPTY;
		}
		ItemStack existing = handler.getStackInSlot(slot);
		int count = existing.getCount();
		ItemStack extracted = handler.extractItem(slot, count, false);
		return extracted.isEmpty() ? ItemStack.EMPTY : extracted;
	}

	@Override
	public void setItem(int slot, @NotNull ItemStack stack) {
		IItemHandler handler = handler(null);
		if (handler instanceof IItemHandlerModifiable modifiable && slot >= 0 && slot < modifiable.getSlots()) {
			modifiable.setStackInSlot(slot, stack);
		}
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	public void clearContent() {
		IItemHandler handler = handler(null);
		if (handler instanceof IItemHandlerModifiable modifiable) {
			for (int i = 0; i < modifiable.getSlots(); i++) {
				modifiable.setStackInSlot(i, ItemStack.EMPTY);
			}
		}
	}

	@Override
	public void setChanged() {
		super.setChanged();
	}

	@Override
	public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
		IItemHandler handler = handler(null);
		return handler != null && slot >= 0 && slot < handler.getSlots() && handler.isItemValid(slot, stack);
	}

	@Override
	public int getMaxStackSize() {
		IItemHandler handler = handler(null);
		if (handler == null) {
			return 64;
		}
		int max = 0;
		for (int i = 0; i < handler.getSlots(); i++) {
			max = Math.max(max, handler.getSlotLimit(i));
		}
		return max == 0 ? 64 : max;
	}
}
