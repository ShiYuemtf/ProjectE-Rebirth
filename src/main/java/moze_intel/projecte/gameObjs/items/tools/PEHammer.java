package moze_intel.projecte.gameObjs.items.tools;

import com.google.common.collect.Multimap;
import moze_intel.projecte.gameObjs.EnumMatterType;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.utils.ToolHelper;
import moze_intel.projecte.utils.ToolHelper.ChargeAttributeCache;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import moze_intel.projecte.platform.misc.ToolAction;
import moze_intel.projecte.platform.misc.ToolActions;
import org.jetbrains.annotations.NotNull;

public class PEHammer extends PETool implements IChargeAttributeModifiers {

	private final ChargeAttributeCache attributeCache = new ChargeAttributeCache();

	public PEHammer(EnumMatterType matterType, int numCharges, Properties props) {
		super(matterType, PETags.Blocks.MINEABLE_WITH_PE_HAMMER, 10, -3, numCharges, props);
	}

	@Override
	public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity damaged, @NotNull LivingEntity damager) {
		ToolHelper.attackWithCharge(stack, damaged, damager, 1.0F);
		return true;
	}

	public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
		return ToolActions.DEFAULT_PICKAXE_ACTIONS.contains(toolAction) || ToolHelper.DEFAULT_PE_HAMMER_ACTIONS.contains(toolAction);
	}

	@Override
	public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
		return ToolHelper.canMatterMine(matterType, state.getBlock()) ? 1_200_000 : super.getDestroySpeed(stack, state);
	}

	@NotNull
	@Override
	public Multimap<Attribute, AttributeModifier> addChargeAttributeModifiers(@NotNull ItemStack stack, @NotNull EquipmentSlot slot,
			@NotNull Multimap<Attribute, AttributeModifier> currentModifiers) {
		//1.20.1 has no stack aware attribute hook, so the charge bonus is applied from ItemStackMixin instead of getDefaultAttributeModifiers
		return attributeCache.addChargeAttributeModifier(currentModifiers, slot, stack);
	}

	@NotNull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null) {
			return InteractionResult.PASS;
		}
		return ToolHelper.digAOE(context.getLevel(), player, context.getHand(), context.getItemInHand(), context.getClickedPos(), context.getClickedFace(), true, 0);
	}
}