package moze_intel.projecte.gameObjs.entity;

import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEEntityTypes;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public class EntityNovaCatalystPrimed extends PrimedTnt {

	public EntityNovaCatalystPrimed(EntityType<EntityNovaCatalystPrimed> type, Level level) {
		super(type, level);
		setFuse(getFuse() / 4);
	}

	public EntityNovaCatalystPrimed(Level level, double x, double y, double z, LivingEntity placer) {
		super(level, x, y, z, placer);
		setFuse(getFuse() / 4);
		blocksBuilding = true;
	}

	@NotNull
	@Override
	public EntityType<?> getType() {
		return PEEntityTypes.NOVA_CATALYST_PRIMED.get();
	}

	@NotNull
	@Override
	public ItemStack getPickResult() {
		return new ItemStack(PEBlocks.NOVA_CATALYST);
	}
}
