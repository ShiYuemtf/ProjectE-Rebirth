package moze_intel.projecte.platform.misc;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fabric side stand-in for the handful of Forge's {@code ForgeHooks} helpers that the ported code calls. Each one forwards to the vanilla equivalent.
 */
public class ForgeHooks {

	/**
	 * 1.20.1 has no per-stack burn time lookup: vanilla keeps one fuel table, so any recipe type resolves against it.
	 */
	public static int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
		return AbstractFurnaceBlockEntity.getFuel().getOrDefault(stack.getItem(), 0);
	}

	/**
	 * @return {@code -1} when the break is denied, otherwise the experience the break yields. Fabric has no break event, so breaks are always allowed.
	 */
	public static int onBlockBreakEvent(Level level, net.minecraft.world.level.GameType gameType, Player player, BlockPos pos) {
		//shortcut: no Forge block break event on Fabric; returning 0 means "allowed, no xp"
		return 0;
	}

	public static boolean canToolHarvestBlock(Level level, BlockPos pos, ItemStack stack) {
		BlockState state = level.getBlockState(pos);
		return stack.isCorrectToolForDrops(state);
	}
}
