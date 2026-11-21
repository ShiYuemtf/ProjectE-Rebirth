package moze_intel.projecte.events;

import moze_intel.projecte.gameObjs.items.armor.GemFeet;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

/**
 * Client only. Called from {@code moze_intel.projecte.mixin.AbstractClientPlayerMixin}, never loaded on a dedicated server.
 */
public class PlayerRender {

	//shortcut: Forge exposed ComputeFovModifierEvent, Fabric has no equivalent, so the modifier is patched right after vanilla computes it
	public static float modifyFov(Player player, float fovModifier) {
		if (player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof GemFeet) {
			return fovModifier - 0.5F * Minecraft.getInstance().options.fovEffectScale().get().floatValue();
		}
		return fovModifier;
	}
}
