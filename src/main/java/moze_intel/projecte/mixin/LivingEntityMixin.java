package moze_intel.projecte.mixin;

import moze_intel.projecte.events.PlayerEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hooks the two places Forge used to expose as LivingAttackEvent/LivingHurtEvent: the whole hit being denied, and the amount being reduced by armor.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

	@Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
	private void projecte$denyFullyReducedHit(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (PlayerEvents.blocksAllDamage((LivingEntity) (Object) this, source, amount)) {
			cir.setReturnValue(false);
		}
	}

	@ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true)
	private float projecte$armorDamageReduction(float amount, DamageSource source) {
		return PlayerEvents.getReducedDamage((LivingEntity) (Object) this, source, amount);
	}
}
