package moze_intel.projecte.platform.misc.brewing;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * An ingredient based brewing conversion: {@code ingredient} brewed into {@code input} yields {@code output}.
 */
public class BrewingRecipe implements IBrewingRecipe {

	private final Ingredient input;
	private final Ingredient ingredient;
	private final ItemStack output;

	public BrewingRecipe(Ingredient input, Ingredient ingredient, ItemStack output) {
		this.input = input;
		this.ingredient = ingredient;
		this.output = output;
	}

	public Ingredient getInput() {
		return input;
	}

	public Ingredient getIngredient() {
		return ingredient;
	}

	public ItemStack getOutput() {
		return output;
	}

	@Override
	public ItemStack getOutput(ItemStack input, ItemStack reagent) {
		if (this.input.test(input) && this.ingredient.test(reagent)) {
			return output.copy();
		}
		return ItemStack.EMPTY;
	}
}
