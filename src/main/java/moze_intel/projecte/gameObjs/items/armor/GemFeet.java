package moze_intel.projecte.gameObjs.items.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import moze_intel.projecte.gameObjs.items.IFlightProvider;
import moze_intel.projecte.gameObjs.items.IStepAssister;
import moze_intel.projecte.utils.ClientKeyHelper;
import moze_intel.projecte.utils.Constants;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.PEKeybind;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import moze_intel.projecte.platform.misc.DistExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GemFeet extends GemArmorBase implements IFlightProvider, IStepAssister {

	private static final UUID MODIFIER = UUID.fromString("A4334312-DFF8-4582-9F4F-62AD0C070475");
	/** Vertical boost added per tick while jump is held in the air. Kept at the upstream value so the feel matches Forge. */
	private static final double JUMP_ASSIST_PER_TICK = 0.1D;
	/** Upstream cap for the horizontal air boost (squared length of the delta movement). */
	private static final double MAX_AIR_SPEED_SQR = 3.0D;
	/** Last client tick the assist ran for a player, so it can never be applied twice in one tick. */
	private static final Map<Player, Integer> LAST_ASSIST_TICK = new WeakHashMap<>();

	private final Multimap<Attribute, AttributeModifier> attributes;

	public GemFeet(Properties props) {
		super(ArmorItem.Type.BOOTS, props);
		Builder<Attribute, AttributeModifier> attributesBuilder = ImmutableMultimap.builder();
		//Must call super: our own override reads this.attributes, which is not assigned yet
		attributesBuilder.putAll(super.getDefaultAttributeModifiers(EquipmentSlot.FEET));
		attributesBuilder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(MODIFIER, "Armor modifier", 1.0, Operation.MULTIPLY_TOTAL));
		this.attributes = attributesBuilder.build();
	}

	public void toggleStepAssist(ItemStack boots, Player player) {
		boolean value;
		CompoundTag bootsTag = boots.getOrCreateTag();
		if (bootsTag.contains(Constants.NBT_KEY_STEP_ASSIST, Tag.TAG_BYTE)) {
			value = !bootsTag.getBoolean(Constants.NBT_KEY_STEP_ASSIST);
			bootsTag.putBoolean(Constants.NBT_KEY_STEP_ASSIST, value);
		} else {
			//If we don't have the tag count that as it already being "false"
			bootsTag.putBoolean(Constants.NBT_KEY_STEP_ASSIST, true);
			value = true;
		}
		if (value) {
			player.sendSystemMessage(PELang.STEP_ASSIST.translate(ChatFormatting.GREEN, PELang.GEM_ENABLED));
		} else {
			player.sendSystemMessage(PELang.STEP_ASSIST.translate(ChatFormatting.RED, PELang.GEM_DISABLED));
		}
	}

	private static boolean isJumpPressed() {
		return DistExecutor.unsafeRunForDist(() -> () -> Minecraft.getInstance().options.keyJump.isDown(), () -> () -> false);
	}

	@Override
	public void onArmorTick(ItemStack stack, Level level, Player player) {
		if (!level.isClientSide) {
			ServerPlayer playerMP = (ServerPlayer) player;
			playerMP.fallDistance = 0;
			return;
		}
		//The boots can be reached from more than one inventory walk in the same tick (worn stack plus anything that forwards inventoryTick);
		//running the boost twice per tick doubles both the climb and the air acceleration, so only allow one pass per client tick.
		Integer lastTick = LAST_ASSIST_TICK.get(player);
		if (lastTick != null && lastTick == player.tickCount) {
			return;
		}
		LAST_ASSIST_TICK.put(player, player.tickCount);
		if (!player.getAbilities().flying && isJumpPressed()) {
			//Upstream value, no extra clamp: the assist is meant to lift the player like the original does.
			player.setDeltaMovement(player.getDeltaMovement().add(0, JUMP_ASSIST_PER_TICK, 0));
		}
		if (!player.onGround()) {
			Vec3 deltaMovement = player.getDeltaMovement();
			if (deltaMovement.y() <= 0) {
				player.setDeltaMovement(deltaMovement = deltaMovement.multiply(1, 0.9, 1));
			}
			if (!player.getAbilities().flying) {
				if (player.zza < 0) {
					player.setDeltaMovement(deltaMovement.multiply(0.9, 1, 0.9));
				} else if (player.zza > 0 && deltaMovement.lengthSqr() < MAX_AIR_SPEED_SQR) {
					player.setDeltaMovement(deltaMovement.multiply(1.1, 1, 1.1));
				}
			}
		}
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltips, @NotNull TooltipFlag flags) {
		super.appendHoverText(stack, level, tooltips, flags);
		tooltips.add(PELang.GEM_LORE_FEET.translate());
		tooltips.add(PELang.STEP_ASSIST_PROMPT.translate(ClientKeyHelper.getKeyName(PEKeybind.BOOTS_TOGGLE)));
		if (ItemHelper.checkItemNBT(stack, Constants.NBT_KEY_STEP_ASSIST)) {
			tooltips.add(PELang.STEP_ASSIST.translate(ChatFormatting.GREEN, PELang.GEM_ENABLED));
		} else {
			tooltips.add(PELang.STEP_ASSIST.translate(ChatFormatting.RED, PELang.GEM_DISABLED));
		}
	}

	@NotNull
	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NotNull EquipmentSlot slot) {
		return slot == EquipmentSlot.FEET ? attributes : super.getDefaultAttributeModifiers(slot);
	}

	@Override
	public boolean canProvideFlight(ItemStack stack, ServerPlayer player) {
		return player.getItemBySlot(EquipmentSlot.FEET) == stack;
	}

	@Override
	public boolean canAssistStep(ItemStack stack, Player player) {
		return player.getItemBySlot(EquipmentSlot.FEET) == stack && ItemHelper.checkItemNBT(stack, Constants.NBT_KEY_STEP_ASSIST);
	}
}