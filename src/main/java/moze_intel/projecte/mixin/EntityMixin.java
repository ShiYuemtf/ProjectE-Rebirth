package moze_intel.projecte.mixin;

import moze_intel.projecte.gameObjs.items.IStepAssister;
import moze_intel.projecte.utils.PlayerHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gem armor step assist.
 * <p>
 * Forge implemented this as a player attribute ({@code ForgeMod.STEP_HEIGHT_ADDITION}, +0.4 over the vanilla 0.6), which the attribute map synced to
 * the client for free. 1.20.1 has no step height attribute, so the vanilla value is raised directly here instead; injecting on both sides keeps the
 * client's movement prediction in agreement with the server.
 */
@Mixin(Entity.class)
public class EntityMixin {

	private static final float PROJECTE_STEP_HEIGHT = 1.0F;

	@Inject(method = "maxUpStep", at = @At("RETURN"), cancellable = true)
	private void projecte$stepAssist(CallbackInfoReturnable<Float> cir) {
		if (cir.getReturnValueF() < PROJECTE_STEP_HEIGHT && (Object) this instanceof Player player
				&& PlayerHelper.checkArmorHotbarCurios(player, stack -> !stack.isEmpty() && stack.getItem() instanceof IStepAssister assister
				&& assister.canAssistStep(stack, player))) {
			cir.setReturnValue(PROJECTE_STEP_HEIGHT);
		}
	}
}
