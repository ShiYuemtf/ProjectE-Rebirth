package moze_intel.projecte.platform.misc.brewing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Fabric side stand-in for Forge's {@code BrewingRecipeRegistry}.
 * <p>
 * Forge keeps a mutable list of brewing conversions that mods can append to. Vanilla only keeps one static table, so this registry holds the extra
 * modded conversions plus the single vanilla entry, which reads through to {@link net.minecraft.world.item.alchemy.PotionBrewing} on demand.
 */
public class BrewingRecipeRegistry {

	private static final List<IBrewingRecipe> RECIPES = new ArrayList<>();

	static {
		RECIPES.add(new VanillaBrewingRecipe());
	}

	public static void addRecipe(IBrewingRecipe recipe) {
		RECIPES.add(recipe);
	}

	public static void addRecipe(ItemStack input, ItemStack ingredient, ItemStack output) {
		addRecipe(new BrewingRecipe(Ingredient.of(input), Ingredient.of(ingredient), output));
	}

	public static List<IBrewingRecipe> getRecipes() {
		return Collections.unmodifiableList(RECIPES);
	}
}
