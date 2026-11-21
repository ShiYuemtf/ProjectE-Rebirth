package moze_intel.projecte.gameObjs.items.tools;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Charge scaling attack damage for weapons.
 * <p>
 * Forge had {@code IForgeItem#getAttributeModifiers(EquipmentSlot, ItemStack)}, so a weapon could read its charge from the stack it was asked about. The vanilla hook
 * ({@code Item#getDefaultAttributeModifiers(EquipmentSlot)}) has no stack, so the charge bonus is applied from {@code ItemStackMixin} where the stack is known.
 */
public interface IChargeAttributeModifiers {

	/**
	 * Adds the attack damage bonus for the given stack's charge to a copy of the passed in modifiers.
	 */
	@NotNull
	Multimap<Attribute, AttributeModifier> addChargeAttributeModifiers(@NotNull ItemStack stack, @NotNull EquipmentSlot slot,
			@NotNull Multimap<Attribute, AttributeModifier> currentModifiers);
}
