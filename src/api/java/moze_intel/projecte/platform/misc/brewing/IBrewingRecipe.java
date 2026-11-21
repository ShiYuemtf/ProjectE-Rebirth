package moze_intel.projecte.platform.misc.brewing;

import net.minecraft.world.item.ItemStack;

/**
 * A brewing stand conversion ProjectE can enumerate for EMC mapping.
 */
public interface IBrewingRecipe {

	/**
	 * @return The result of brewing {@code reagent} into {@code input}, or {@link ItemStack#EMPTY} if this recipe does not apply.
	 */
	ItemStack getOutput(ItemStack input, ItemStack reagent);
}
