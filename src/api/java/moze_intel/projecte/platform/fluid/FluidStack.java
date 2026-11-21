package moze_intel.projecte.platform.fluid;

import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

/**
 * Fabric side stand-in for Forge's {@code FluidStack}. Forge carries fluid, amount and a tag; on Fabric fluids are still vanilla objects in 1.20.1, so this keeps
 * the Forge shaped API and bridges to Fabric's {@code FluidVariant} where inventories need it (see {@link StorageFluidHandler}).
 */
public class FluidStack {

	public static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY, 0);

	private final Fluid fluid;
	private final int amount;

	public FluidStack(Fluid fluid, int amount) {
		this.fluid = fluid;
		this.amount = fluid == Fluids.EMPTY ? 0 : amount;
	}

	public Fluid getFluid() {
		return fluid;
	}

	public int getAmount() {
		return amount;
	}

	public boolean isEmpty() {
		return fluid == Fluids.EMPTY || amount <= 0;
	}

	@NotNull
	public FluidType getFluidType() {
		return FluidType.of(fluid);
	}

	public String getTranslationKey() {
		ResourceLocation key = BuiltInRegistries.FLUID.getKey(fluid);
		return Util.makeDescriptionId("fluid", key == null ? BuiltInRegistries.FLUID.getKey(Fluids.EMPTY) : key);
	}

	public FluidStack copy() {
		return isEmpty() ? EMPTY : new FluidStack(fluid, amount);
	}

	public FluidStack copyWithAmount(int amount) {
		return isEmpty() ? EMPTY : new FluidStack(fluid, amount);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof FluidStack other)) {
			return false;
		}
		return amount == other.amount && fluid == other.fluid;
	}

	@Override
	public int hashCode() {
		return 31 * fluid.hashCode() + amount;
	}

	@Override
	public String toString() {
		return "FluidStack{" + BuiltInRegistries.FLUID.getKey(fluid) + " x " + amount + "}";
	}

	public Component getDisplayName() {
		return Component.translatable(getFluidType().getDescriptionId());
	}
}
