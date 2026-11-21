package moze_intel.projecte.integration.jei.world_transmute;

import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import moze_intel.projecte.api.imc.WorldTransmutationEntry;
import moze_intel.projecte.platform.fluid.FluidStack;
import moze_intel.projecte.platform.fluid.FluidType;
import moze_intel.projecte.platform.fluid.IFluidBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Display model for one world transmutation row. Forge's {@code net.minecraftforge.fluids.*} types are replaced by the project's compat
 * layer ({@code moze_intel.projecte.platform.fluid}), which keeps Forge's fluid/amount shape on top of vanilla 1.20.1 fluids.
 */
public class WorldTransmuteEntry {

	private record StateInfo(ItemStack item, FluidStack fluid) {

		public boolean isEmpty() {
			return item.isEmpty() && fluid.isEmpty();
		}

		public Either<ItemStack, FluidStack> toEither() {
			if (fluid.isEmpty()) {
				return Either.left(item);
			}
			return Either.right(fluid);
		}
	}

	private static final StateInfo EMPTY = new StateInfo(ItemStack.EMPTY, FluidStack.EMPTY);

	private final StateInfo input;
	private final StateInfo leftOutput;
	private final StateInfo rightOutput;

	public WorldTransmuteEntry(WorldTransmutationEntry transmutationEntry) {
		BlockState leftOutputState = transmutationEntry.result();
		BlockState rightOutputState = transmutationEntry.altResult();
		if (leftOutputState == rightOutputState) {
			//Don't show two outputs if it is just a fallback from the primary
			rightOutputState = null;
		}
		input = createInfo(transmutationEntry.origin());
		leftOutput = createInfo(leftOutputState);
		rightOutput = createInfo(rightOutputState);
	}

	private StateInfo createInfo(@Nullable BlockState output) {
		if (output == null) {
			return EMPTY;
		}
		FluidStack outputFluid = fluidFromBlock(output);
		if (outputFluid.isEmpty()) {
			return new StateInfo(itemFromBlock(output.getBlock(), output), outputFluid);
		}
		return new StateInfo(ItemStack.EMPTY, outputFluid);
	}

	private FluidStack fluidFromBlock(BlockState state) {
		Block block = state.getBlock();
		if (block instanceof LiquidBlock) {
			//Vanilla 1.20.1 LiquidBlock has no getFluid() (that is a 1.21 API); the fluid is only exposed through the block state's fluid state.
			return new FluidStack(state.getFluidState().getType(), FluidType.BUCKET_VOLUME);
		} else if (block instanceof IFluidBlock fluidBlock) {
			return new FluidStack(fluidBlock.getFluid(), FluidType.BUCKET_VOLUME);
		}
		return FluidStack.EMPTY;
	}

	private ItemStack itemFromBlock(Block block, BlockState state) {
		try {
			//No level or position is available when building JEI recipes, so the vanilla three-arg pick-block is used instead of Forge's
			//five-arg variant. Blocks that need a live level throw and fall back to the plain block item below.
			return block.getCloneItemStack(null, null, state);
		} catch (Exception e) {
			//It failed, probably because of the null world and pos
			return new ItemStack(block);
		}
	}

	public boolean isRenderable() {
		return !input.isEmpty() && (!leftOutput.isEmpty() || !rightOutput.isEmpty());
	}

	public Optional<Either<ItemStack, FluidStack>> getInput() {
		if (input.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(input.toEither());
	}

	public Iterable<Either<ItemStack, FluidStack>> getOutput() {
		List<Either<ItemStack, FluidStack>> outputs = new ArrayList<>();
		if (!leftOutput.isEmpty()) {
			outputs.add(leftOutput.toEither());
		}
		if (!rightOutput.isEmpty()) {
			outputs.add(rightOutput.toEither());
		}
		return outputs;
	}

	public ItemStack getInputItem() {
		return input.item();
	}

	public FluidStack getInputFluid() {
		return input.fluid();
	}
}
