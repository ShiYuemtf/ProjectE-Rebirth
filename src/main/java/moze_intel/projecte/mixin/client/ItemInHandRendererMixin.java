package moze_intel.projecte.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ProjectE items rewrite their own NBT every tick (stored EMC, charge, active flag). Vanilla's {@code ItemInHandRenderer#tick} keeps the previously rendered
 * stack in {@code mainHandItem} and only replaces it once {@code mainHandHeight} has dropped below 0.1, and it decides whether to raise the new stack by
 * comparing that field with the held stack <em>by identity</em>. Repeatedly replacing the stack object (which writing NBT does) therefore restarts the raise
 * animation every tick and leaves the item sunk at the bottom of the screen.
 * <p>
 * Forge had {@code IForgeItem#shouldCauseReequipAnimation} for this; 1.20.1 has no such hook. We emulate it here: while the held item is of the same
 * <em>type</em> as the one currently being rendered, keep the renderer's stack in sync with the real one and pin the height at 1. Comparing types is what
 * matters - comparing NBT would defeat the purpose, and comparing our own cached copy against the renderer field made the pin stick forever when the player
 * switched to a different item, which left the old model drawn in hand until something else reset the renderer.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

	@Shadow
	private ItemStack mainHandItem;

	@Shadow
	private ItemStack offHandItem;

	@Shadow
	private float mainHandHeight;

	@Shadow
	private float oMainHandHeight;

	@Shadow
	private float offHandHeight;

	@Shadow
	private float oOffHandHeight;

	@Inject(method = "tick", at = @At("TAIL"))
	private void projecte$keepEquippedItemsRaised(CallbackInfo ci) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		ItemStack heldMain = player.getMainHandItem();
		//Only pin when the renderer is still showing a *different stack object* of the same item, which is exactly the "same item, refreshed NBT"
		//case. When vanilla has already moved its field onto the new stack (same object) the swap is real and its rise animation must be left alone,
		//otherwise every item snaps up instantly.
		if (!heldMain.isEmpty() && mainHandItem != heldMain && mainHandItem.getItem() == heldMain.getItem()) {
			mainHandItem = heldMain;
			mainHandHeight = 1.0F;
			oMainHandHeight = 1.0F;
		}
		ItemStack heldOff = player.getOffhandItem();
		if (!heldOff.isEmpty() && offHandItem != heldOff && offHandItem.getItem() == heldOff.getItem()) {
			offHandItem = heldOff;
			offHandHeight = 1.0F;
			oOffHandHeight = 1.0F;
		}
	}
}
