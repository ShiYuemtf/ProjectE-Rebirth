package moze_intel.projecte.integration.trinkets;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Shared {@link Trinket} behavior registered for every ProjectE accessory.
 * <p>
 * Its job is forwarding the trinket tick to {@link net.minecraft.world.item.Item#inventoryTick}, which is what drives the accessories' passive abilities. On
 * Forge the same thing happened because Curios called the item's capabilities; Trinkets keeps worn stacks in its own inventory, so unless the tick is forwarded
 * here a worn ring would sit completely inert.
 */
public class ProjectETrinket implements Trinket {

	@Override
	public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
		Level level = entity.level();
		if (level.isClientSide) {
			stack.getItem().inventoryTick(stack, level, entity, Inventory.SLOT_OFFHAND, false);
			return;
		}
		//Worn stacks are live references, and mutating one (draining EMC, toggling active) does not sync to the client the way a vanilla inventory slot would.
		//Flag the inventory when the tick changed something so trinkets pushes the update.
		ItemStack before = stack.copy();
		stack.getItem().inventoryTick(stack, level, entity, Inventory.SLOT_OFFHAND, false);
		if (!ItemStack.matches(before, stack)) {
			slot.inventory().markUpdate();
		}
	}

	/**
	 * Every ProjectE accessory works from anywhere: the passive abilities only ever check "is this stack equipped", and the active ones are driven by the
	 * hotbar/keybind code. Restricting slot groups would only take away options, so allow them all.
	 */
	@Override
	public boolean canEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
		return true;
	}
}
