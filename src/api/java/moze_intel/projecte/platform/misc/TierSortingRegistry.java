package moze_intel.projecte.platform.misc;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fabric side stand-in for Forge's {@code TierSortingRegistry}. 1.20.1 has no tier sorting: tool tiers are only compared through {@code #needs_*_tool} block tags.
 * ProjectE only uses this to decide whether a tool may drop a block, so that check becomes the vanilla tag test and registration is a no-op.
 */
public class TierSortingRegistry {

	public static void registerTier(Tier tier, ResourceLocation name, List<?> after, List<?> before) {
		//shortcut: no tier ordering exists on 1.20.1 Fabric, tools are wired through block tags in PECore instead
	}

	public static boolean isCorrectTierForDrops(Tier tier, BlockState state) {
		if (state.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
			return tier.getLevel() >= 3;
		} else if (state.is(BlockTags.NEEDS_IRON_TOOL)) {
			return tier.getLevel() >= 2;
		} else if (state.is(BlockTags.NEEDS_STONE_TOOL)) {
			return tier.getLevel() >= 1;
		}
		return true;
	}

	@Nullable
	public static Tier getTier(ResourceLocation name) {
		return null;
	}
}
