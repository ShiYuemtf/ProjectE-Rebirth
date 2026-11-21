package moze_intel.projecte.events;

import java.util.List;
import java.util.Optional;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import moze_intel.projecte.capability.CapabilityHelper;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.utils.Constants;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.text.PELang;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Client only. Registered from {@code ClientRegistration#onInitializeClient()}.
 */
public class ToolTipEvent {

	public static void register() {
		ItemTooltipCallback.EVENT.register((stack, tooltipFlag, tooltip) -> tTipEvent(stack, tooltip));
	}

	private static void tTipEvent(ItemStack current, List<Component> tooltip) {
		if (current.isEmpty()) {
			return;
		}
		Player clientPlayer = Minecraft.getInstance().player;
		if (ProjectEConfig.client.pedestalToolTips.get()) {
			CapabilityHelper.getCapability(current, PECapabilities.PEDESTAL_ITEM_CAPABILITY).ifPresent(pedestalItem -> {
				tooltip.add(PELang.PEDESTAL_ON.translateColored(ChatFormatting.DARK_PURPLE));
				List<Component> description = pedestalItem.getPedestalDescription();
				if (description.isEmpty()) {
					tooltip.add(PELang.PEDESTAL_DISABLED.translateColored(ChatFormatting.RED));
				} else {
					tooltip.addAll(description);
				}
			});
		}

		if (ProjectEConfig.client.tagToolTips.get()) {
			current.getTags().forEach(tag -> tooltip.add(Component.literal("#" + tag.location())));
		}

		if (ProjectEConfig.client.emcToolTips.get() && (!ProjectEConfig.client.shiftEmcToolTips.get() || Screen.hasShiftDown())) {
			long value = EMCHelper.getEmcValue(current);
			if (value > 0) {
				tooltip.add(EMCHelper.getEmcTextComponent(value, 1));
				if (current.getCount() > 1) {
					tooltip.add(EMCHelper.getEmcTextComponent(value, current.getCount()));
				}
				if (clientPlayer != null && (!ProjectEConfig.client.shiftLearnedToolTips.get() || Screen.hasShiftDown())) {
					if (CapabilityHelper.getCapability(clientPlayer, PECapabilities.KNOWLEDGE_CAPABILITY).map(k -> k.hasKnowledge(current)).orElse(false)) {
						tooltip.add(PELang.EMC_HAS_KNOWLEDGE.translateColored(ChatFormatting.YELLOW));
					} else {
						tooltip.add(PELang.EMC_NO_KNOWLEDGE.translateColored(ChatFormatting.RED));
					}
				}
			}
		}

		if (current.hasTag()) {
			long value;
			CompoundTag tag = current.getOrCreateTag();
			if (tag.contains(Constants.NBT_KEY_STORED_EMC, Tag.TAG_LONG)) {
				value = tag.getLong(Constants.NBT_KEY_STORED_EMC);
			} else {
				Optional<IItemEmcHolder> holderCapability = CapabilityHelper.getCapability(current, PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).resolve();
				if (holderCapability.isPresent()) {
					value = holderCapability.get().getStoredEmc(current);
				} else {
					return;
				}
			}
			tooltip.add(PELang.EMC_STORED.translateColored(ChatFormatting.YELLOW, ChatFormatting.WHITE, Constants.EMC_FORMATTER.format(value)));
		}
	}
}
