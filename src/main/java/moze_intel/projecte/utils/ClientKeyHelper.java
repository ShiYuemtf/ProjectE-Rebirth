package moze_intel.projecte.utils;

import com.google.common.collect.ImmutableBiMap;
import com.mojang.blaze3d.platform.InputConstants;
import moze_intel.projecte.PECore;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.to_server.KeyPressPKT;
import moze_intel.projecte.utils.text.PELang;
import moze_intel.projecte.utils.text.TextComponentUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ClientKeyHelper {

	private static ImmutableBiMap<KeyMapping, PEKeybind> mcToPe = ImmutableBiMap.of();
	private static ImmutableBiMap<PEKeybind, KeyMapping> peToMc = ImmutableBiMap.of();

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> keyPress());
	}

	private static void keyPress() {
		for (KeyMapping k : mcToPe.keySet()) {
			while (k.consumeClick()) {
				PEKeybind keyBind = mcToPe.get(k);
				//Vanilla key mappings cannot carry a modifier, so the shift only variant of the toggle key is resolved here, at press time
				if (keyBind == PEKeybind.BOOTS_TOGGLE && Screen.hasShiftDown()) {
					keyBind = PEKeybind.HELMET_TOGGLE;
				}
				PacketHandler.sendToServer(new KeyPressPKT(keyBind));
			}
		}
	}

	public static void registerKeyBindings() {
		ImmutableBiMap.Builder<KeyMapping, PEKeybind> builder = ImmutableBiMap.builder();
		//HELMET_TOGGLE shares the boots key: the modifier is decided in keyPress once the key actually fires
		addKeyBinding(builder, PEKeybind.BOOTS_TOGGLE, GLFW.GLFW_KEY_X);
		addKeyBinding(builder, PEKeybind.CHARGE, GLFW.GLFW_KEY_V);
		addKeyBinding(builder, PEKeybind.EXTRA_FUNCTION, GLFW.GLFW_KEY_C);
		addKeyBinding(builder, PEKeybind.FIRE_PROJECTILE, GLFW.GLFW_KEY_R);
		addKeyBinding(builder, PEKeybind.MODE, GLFW.GLFW_KEY_G);
		mcToPe = builder.build();
		peToMc = mcToPe.inverse();
	}

	private static void addKeyBinding(ImmutableBiMap.Builder<KeyMapping, PEKeybind> builder, PEKeybind keyBind, int keyCode) {
		builder.put(KeyBindingHelper.registerKeyBinding(new KeyMapping(keyBind.getTranslationKey(), InputConstants.Type.KEYSYM, keyCode,
				PELang.PROJECTE.getTranslationKey())), keyBind);
	}

	public static Component getKeyName(PEKeybind k) {
		if (k == PEKeybind.HELMET_TOGGLE && peToMc.containsKey(PEKeybind.BOOTS_TOGGLE)) {
			//shortcut: the shift modifier has no KeyMapping representation, so it is prepended as plain text, upgrade when Fabric gains modifier aware key mappings
			return Component.literal("Shift + ").append(peToMc.get(PEKeybind.BOOTS_TOGGLE).getTranslatedKeyMessage());
		}
		if (peToMc.containsKey(k)) {
			return peToMc.get(k).getTranslatedKeyMessage();
		}
		//Fallback to the translation key of the key's function
		return TextComponentUtil.build(k);
	}
}
