package moze_intel.projecte.platform.misc.brewing;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;

/**
 * The vanilla brewing conversions, queried straight from {@link PotionBrewing} instead of being mirrored into a registry the way Forge's version
 * has to be.
 */
public class VanillaBrewingRecipe implements IBrewingRecipe {

	@Override
	public ItemStack getOutput(ItemStack input, ItemStack reagent) {
		if (input.isEmpty() || reagent.isEmpty()) {
			return ItemStack.EMPTY;
		}
		//vanilla signature is mix(reagent, potion)
		ItemStack result = PotionBrewing.mix(reagent.copy(), input.copy());
		if (result.isEmpty() || result.getItem() == input.getItem() && ItemStack.isSameItemSameTags(result, input)) {
			return ItemStack.EMPTY;
		}
		return result;
	}
}
