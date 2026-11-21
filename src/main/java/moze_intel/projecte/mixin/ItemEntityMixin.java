package moze_intel.projecte.mixin;

import moze_intel.projecte.events.PlayerEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets alchemical bags with a suction item grab items before they enter the player inventory, replacing EntityItemPickupEvent.
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

	@Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
	private void projecte$bagSuction(Player player, CallbackInfo ci) {
		ItemEntity self = (ItemEntity) (Object) this;
		//playerTouch is only reached for the intended owner, but the injection sits in front of that check
		Entity owner = self.getOwner();
		if (owner != null && owner != player) {
			return;
		}
		if (PlayerEvents.onItemPickup(self, player)) {
			ci.cancel();
		}
	}
}
