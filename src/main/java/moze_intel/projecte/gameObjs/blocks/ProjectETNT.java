package moze_intel.projecte.gameObjs.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProjectETNT extends TntBlock {

	private final TNTEntityCreator tntEntityCreator;

	public ProjectETNT(Properties properties, TNTEntityCreator tntEntityCreator) {
		super(properties);
		this.tntEntityCreator = tntEntityCreator;
	}

	/**
	 * Upstream overrode Forge's {@code getFlammability} here and let Forge's fire turn it into a primed entity. Fabric fire has no such hook and the flame registry
	 * would only consume the block, so this stays a marker for the ignition paths ({@code WorldHelper#igniteBlock}, and the overrides below prime explicitly).
	 */
	public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
		return 100;
	}

	/**
	 * Forge's {@code BlockStateBase#onCaughtFire} hook does not exist in 1.20.1, so the ignition paths call this directly
	 * ({@code WorldHelper#igniteBlock} for the ignition rings, see the overrides below for redstone and projectiles).
	 */
	public void onCaughtFire(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @Nullable Direction side, @Nullable LivingEntity igniter) {
		prime(level, pos, igniter);
	}

	public void createAndAddEntity(@NotNull Level level, @NotNull BlockPos pos, @Nullable LivingEntity igniter) {
		PrimedTnt tnt = tntEntityCreator.create(level, pos.getX() + 0.5F, pos.getY(), pos.getZ() + 0.5F, igniter);
		level.addFreshEntity(tnt);
		level.playSound(null, tnt.getX(), tnt.getY(), tnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	/**
	 * Vanilla {@link TntBlock} primes a plain {@link PrimedTnt} from every one of these entry points and offers no hook to replace it, so each path has to be
	 * reproduced here with {@link #tntEntityCreator}. Without this the nova blocks only produced their own entity when destroyed by an explosion
	 * ({@link #wasExploded}); lighting one with flint and steel, a fire charge, redstone or fire spawned a vanilla TNT entity.
	 */
	private void prime(@NotNull Level level, @NotNull BlockPos pos, @Nullable LivingEntity igniter) {
		if (!level.isClientSide) {
			createAndAddEntity(level, pos, igniter);
			level.gameEvent(igniter, GameEvent.PRIME_FUSE, pos);
		}
	}

	@Override
	public void onPlace(BlockState state, @NotNull Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (!oldState.is(state.getBlock()) && level.hasNeighborSignal(pos)) {
			prime(level, pos, null);
			level.removeBlock(pos, false);
		}
	}

	@Override
	public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
		if (level.hasNeighborSignal(pos)) {
			prime(level, pos, null);
			level.removeBlock(pos, false);
		}
	}

	@Override
	public void playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
		//Matches vanilla: a TNT block that was already armed by redstone or fire primes when broken in survival
		if (!level.isClientSide && !player.isCreative() && state.getValue(UNSTABLE)) {
			prime(level, pos, null);
			//TntBlock#playerWillDestroy would call TntBlock.explode and spawn a second, vanilla PrimedTnt, so the super call is
			//replaced by the three side effects of Block#playerWillDestroy that we still want.
			level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
			if (state.is(BlockTags.GUARDED_BY_PIGLINS)) {
				PiglinAi.angerNearbyPiglins(player, false);
			}
			return;
		}
		super.playerWillDestroy(level, pos, state, player);
	}

	@NotNull
	@Override
	public InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
		ItemStack stack = player.getItemInHand(hand);
		if (!stack.is(Items.FLINT_AND_STEEL) && !stack.is(Items.FIRE_CHARGE)) {
			return super.use(state, level, pos, player, hand, hit);
		}
		prime(level, pos, player);
		//Flag 3 (NEIGHBORS | CLIENTS) matches vanilla TntBlock#use; removeBlock(pos, false) used flag 2, so neighbours never saw the block disappear
		level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
		Item item = stack.getItem();
		if (!player.isCreative()) {
			if (stack.is(Items.FLINT_AND_STEEL)) {
				stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
			} else {
				stack.shrink(1);
			}
		}
		player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(item));
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	public void onProjectileHit(@NotNull Level level, @NotNull BlockState state, @NotNull BlockHitResult hit, @NotNull Projectile projectile) {
		if (!level.isClientSide) {
			BlockPos pos = hit.getBlockPos();
			//Vanilla TntBlock also requires Projectile#mayInteract here, that is the spawn protection check
			if (projectile.isOnFire() && projectile.mayInteract(level, pos)) {
				prime(level, pos, projectile.getOwner() instanceof LivingEntity living ? living : null);
				level.removeBlock(pos, false);
			}
		}
	}

	public DispenseItemBehavior createDispenseItemBehavior() {
		//Based off vanilla's TNT behavior
		return new DefaultDispenseItemBehavior() {
			@NotNull
			@Override
			protected ItemStack execute(@NotNull BlockSource source, @NotNull ItemStack stack) {
				BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
				createAndAddEntity(source.getLevel(), blockpos, null);
				source.getLevel().gameEvent(null, GameEvent.ENTITY_PLACE, blockpos);
				stack.shrink(1);
				return stack;
			}
		};
	}

	@Override
	public void wasExploded(@NotNull Level level, @NotNull BlockPos pos, @NotNull Explosion explosion) {
		if (!level.isClientSide) {
			PrimedTnt tnt = tntEntityCreator.create(level, (float) pos.getX() + 0.5F, pos.getY(), (float) pos.getZ() + 0.5F, explosion.getIndirectSourceEntity());
			int fuse = tnt.getFuse();
			tnt.setFuse((short) (level.random.nextInt(fuse / 4) + fuse / 8));
			level.addFreshEntity(tnt);
		}
	}

	@FunctionalInterface
	public interface TNTEntityCreator {

		PrimedTnt create(Level level, double posX, double posY, double posZ, @Nullable LivingEntity igniter);
	}
}
