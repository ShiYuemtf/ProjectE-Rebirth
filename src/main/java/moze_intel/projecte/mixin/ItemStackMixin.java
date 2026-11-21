package moze_intel.projecte.mixin;

import com.google.common.collect.Multimap;
import moze_intel.projecte.gameObjs.items.tools.IChargeAttributeModifiers;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Charge scaled attack damage.
 * <p>
 * Forge exposed {@code IForgeItem#getAttributeModifiers(EquipmentSlot, ItemStack)} to let a weapon read the charge of the stack it belongs to; the vanilla
 * {@code Item#getDefaultAttributeModifiers(EquipmentSlot)} is stackless, so injecting on the stack keeps charged weapons hitting harder in melee.
 */
@Mixin(ItemStack.class)
public class ItemStackMixin {

	@Inject(method = "getAttributeModifiers", at = @At("RETURN"), cancellable = true)
	private void projecte$chargeAttributeModifiers(EquipmentSlot slot, CallbackInfoReturnable<Multimap<Attribute, AttributeModifier>> cir) {
		if (((ItemStack) (Object) this).getItem() instanceof IChargeAttributeModifiers modifiers) {
			cir.setReturnValue(modifiers.addChargeAttributeModifiers((ItemStack) (Object) this, slot, cir.getReturnValue()));
		}
	}
}
