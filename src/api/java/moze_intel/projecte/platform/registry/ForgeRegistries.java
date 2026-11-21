package moze_intel.projecte.platform.registry;

import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Fabric side stand-in for Forge's {@code ForgeRegistries}: the vanilla registries wrapped in {@link IForgeRegistry} so ported call sites keep their shape.
 */
public class ForgeRegistries {

	public static final IForgeRegistry<net.minecraft.world.item.Item> ITEMS = new IForgeRegistry<>(BuiltInRegistries.ITEM);
	public static final IForgeRegistry<net.minecraft.world.level.block.Block> BLOCKS = new IForgeRegistry<>(BuiltInRegistries.BLOCK);
	public static final IForgeRegistry<net.minecraft.world.level.material.Fluid> FLUIDS = new IForgeRegistry<>(BuiltInRegistries.FLUID);
	public static final IForgeRegistry<net.minecraft.world.effect.MobEffect> MOB_EFFECTS = new IForgeRegistry<>(BuiltInRegistries.MOB_EFFECT);
	public static final IForgeRegistry<net.minecraft.sounds.SoundEvent> SOUND_EVENTS = new IForgeRegistry<>(BuiltInRegistries.SOUND_EVENT);
	public static final IForgeRegistry<net.minecraft.world.level.block.entity.BlockEntityType<?>> BLOCK_ENTITY_TYPES = new IForgeRegistry<>(
			(net.minecraft.core.Registry) BuiltInRegistries.BLOCK_ENTITY_TYPE);
	public static final IForgeRegistry<net.minecraft.world.entity.EntityType<?>> ENTITY_TYPES = new IForgeRegistry<>(
			(net.minecraft.core.Registry) BuiltInRegistries.ENTITY_TYPE);
	public static final IForgeRegistry<net.minecraft.world.inventory.MenuType<?>> MENU_TYPES = new IForgeRegistry<>(
			(net.minecraft.core.Registry) BuiltInRegistries.MENU);
	public static final IForgeRegistry<net.minecraft.world.item.crafting.RecipeSerializer<?>> RECIPE_SERIALIZERS = new IForgeRegistry<>(
			(net.minecraft.core.Registry) BuiltInRegistries.RECIPE_SERIALIZER);
	public static final IForgeRegistry<net.minecraft.world.item.crafting.RecipeType<?>> RECIPE_TYPES = new IForgeRegistry<>(
			(net.minecraft.core.Registry) BuiltInRegistries.RECIPE_TYPE);
	public static final IForgeRegistry<net.minecraft.world.item.enchantment.Enchantment> ENCHANTMENTS = new IForgeRegistry<>(BuiltInRegistries.ENCHANTMENT);
	public static final IForgeRegistry<net.minecraft.world.item.alchemy.Potion> POTIONS = new IForgeRegistry<>(BuiltInRegistries.POTION);
	public static final IForgeRegistry<net.minecraft.world.entity.ai.attributes.Attribute> ATTRIBUTES = new IForgeRegistry<>(BuiltInRegistries.ATTRIBUTE);
	public static final IForgeRegistry<net.minecraft.core.particles.ParticleType<?>> PARTICLE_TYPES = new IForgeRegistry<>(
			(net.minecraft.core.Registry) BuiltInRegistries.PARTICLE_TYPE);
	public static final IForgeRegistry<net.minecraft.world.item.CreativeModeTab> CREATIVE_MODE_TABS = new IForgeRegistry<>(BuiltInRegistries.CREATIVE_MODE_TAB);

	private ForgeRegistries() {
	}
}
