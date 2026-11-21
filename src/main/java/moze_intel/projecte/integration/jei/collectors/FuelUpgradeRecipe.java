package moze_intel.projecte.integration.jei.collectors;

import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.world.item.ItemStack;

/**
 * Collector fuel upgrade pseudo recipe. Identical to the Forge version: it only holds two vanilla stacks plus the EMC delta, so nothing here touched Forge.
 */
public record FuelUpgradeRecipe(ItemStack input, ItemStack output, long upgradeEMC) {

	public FuelUpgradeRecipe(ItemStack input, ItemStack output) {
		this(input, output, EMCHelper.getEmcValue(output) - EMCHelper.getEmcValue(input));
	}
}
