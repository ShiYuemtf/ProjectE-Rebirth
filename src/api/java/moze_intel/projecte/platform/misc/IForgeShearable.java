package moze_intel.projecte.platform.misc;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Fabric side stand-in for Forge's {@code IForgeShearable}. Vanilla 1.20.1 blocks and entities do not implement it, so the shear paths in {@code ToolHelper} and
 * {@code PEKatar} fall through to the vanilla shear handling instead.
 */
public interface IForgeShearable {

	default boolean isShearable(@Nullable ItemStack item, Level level, BlockPos pos) {
		return true;
	}

	List<ItemStack> onSheared(@Nullable Player player, ItemStack item, Level level, BlockPos pos, int fortune);
}
