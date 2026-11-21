package moze_intel.projecte.gameObjs.items.armor;

import moze_intel.projecte.PECore;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public abstract class GemArmorBase extends PEArmor {

	public GemArmorBase(ArmorItem.Type armorType, Properties props) {
		super(GemArmorMaterial.INSTANCE, armorType, props);
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean isCurrentItem) {
		super.inventoryTick(stack, level, entity, slot, isCurrentItem);
		//1.20.1 has no IForgeItem#onArmorTick: vanilla only ticks worn armor via Inventory#tick, which calls ItemStack#inventoryTick on the
		//same stack instance that getItemBySlot returns, so this is the only hook that both sides reach for a worn piece.
		if (entity instanceof Player player && player.getItemBySlot(getType().getSlot()) == stack) {
			onArmorTick(stack, level, player);
		}
	}

	/**
	 * Called every tick for a piece that is actually worn in its own armor slot. Forge called this from IForgeItem#onArmorTick.
	 */
	public abstract void onArmorTick(ItemStack stack, Level level, Player player);

	@Override
	public float getFullSetBaseReduction() {
		return 0.9F;
	}

	@Override
	public float getMaxDamageAbsorb(ArmorItem.Type type, DamageSource source) {
		if (source.is(DamageTypeTags.IS_EXPLOSION)) {
			return 750;
		}
		if (type == ArmorItem.Type.BOOTS && source.is(DamageTypeTags.IS_FALL)) {
			return 15 / getPieceEffectiveness(type);
		} else if (type == ArmorItem.Type.HELMET && source.is(DamageTypeTags.IS_DROWNING)) {
			return 15 / getPieceEffectiveness(type);
		}
		if (source.is(DamageTypeTags.BYPASSES_ARMOR)) {
			return 0;
		}
		//If the source is not unblockable, allow our piece to block a certain amount of damage
		if (type == ArmorItem.Type.HELMET || type == ArmorItem.Type.BOOTS) {
			return 400;
		}
		return 500;
	}

	public static boolean hasAnyPiece(Player player) {
		return player.getInventory().armor.stream().anyMatch(i -> !i.isEmpty() && i.getItem() instanceof GemArmorBase);
	}

	public static boolean hasFullSet(Player player) {
		return player.getInventory().armor.stream().noneMatch(i -> i.isEmpty() || !(i.getItem() instanceof GemArmorBase));
	}

	private static class GemArmorMaterial implements ArmorMaterial {

		private static final GemArmorMaterial INSTANCE = new GemArmorMaterial();

		@Override
		public int getDurabilityForType(@NotNull ArmorItem.Type type) {
			return 0;
		}

		@Override
		public int getDefenseForType(@NotNull ArmorItem.Type type) {
			return switch (type) {
				case BOOTS -> 3;
				case LEGGINGS -> 6;
				case CHESTPLATE -> 8;
				case HELMET -> 3;
			};
		}

		@Override
		public int getEnchantmentValue() {
			return 0;
		}

		@NotNull
		@Override
		public SoundEvent getEquipSound() {
			return SoundEvents.ARMOR_EQUIP_DIAMOND;
		}

		@NotNull
		@Override
		public Ingredient getRepairIngredient() {
			return Ingredient.EMPTY;
		}

		@NotNull
		@Override
		public String getName() {
			//Only used on the client
			return PECore.rl("gem_armor").toString();
		}

		@Override
		public float getToughness() {
			return 2;
		}

		@Override
		public float getKnockbackResistance() {
			return 0.25F;
		}
	}
}