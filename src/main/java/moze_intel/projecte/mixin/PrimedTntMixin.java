package moze_intel.projecte.mixin;

import moze_intel.projecte.gameObjs.entity.EntityNovaCataclysmPrimed;
import moze_intel.projecte.gameObjs.entity.EntityNovaCatalystPrimed;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.world.entity.item.PrimedTnt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * PrimedTnt#explode is private in 1.20.1, so ProjectE's nova TNT cannot override it. Intercepting the head of that method is the Fabric equivalent of Forge
 * opening it up with an access transformer.
 */
@Mixin(PrimedTnt.class)
public class PrimedTntMixin {

	@Inject(method = "explode", at = @At("HEAD"), cancellable = true)
	private void projecte$novaExplode(CallbackInfo ci) {
		PrimedTnt self = (PrimedTnt) (Object) this;
		float radius;
		if (self instanceof EntityNovaCatalystPrimed) {
			//Upstream EntityNovaCatalystPrimed#explode used 16.0F; 24 made the blast roughly 3.4x the intended volume
			radius = 16.0F;
		} else if (self instanceof EntityNovaCataclysmPrimed) {
			radius = 48.0F;
		} else {
			return;
		}
		WorldHelper.createNovaExplosion(self.level(), self, self.getX(), self.getY(), self.getZ(), radius);
		ci.cancel();
	}
}
