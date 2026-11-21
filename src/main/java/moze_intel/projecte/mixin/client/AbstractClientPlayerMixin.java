package moze_intel.projecte.mixin.client;

import moze_intel.projecte.events.PlayerRender;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gem boots reduce the fov change from the speed boost, replacing ComputeFovModifierEvent.
 */
@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {

	@Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true)
	private void projecte$gemFeetFov(CallbackInfoReturnable<Float> cir) {
		cir.setReturnValue(PlayerRender.modifyFov((AbstractClientPlayer) (Object) this, cir.getReturnValueF()));
	}
}
