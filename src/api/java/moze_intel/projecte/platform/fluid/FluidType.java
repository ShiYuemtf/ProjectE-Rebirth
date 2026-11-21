package moze_intel.projecte.platform.fluid;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;

/**
 * Fabric side stand-in for Forge's {@code FluidType}, which in 1.20.1 Forge carries the fluid's description id and bucket volume. Vanilla 1.20.1 has no fluid type
 * registry, so this derives the same values from the fluid registry key.
 */
public class FluidType {

	public static final int BUCKET_VOLUME = 1000;

	private static final Map<Fluid, FluidType> TYPES = new ConcurrentHashMap<>();

	private final Fluid fluid;
	private final String descriptionId;

	private FluidType(Fluid fluid) {
		this.fluid = fluid;
		this.descriptionId = Util.makeDescriptionId("fluid", BuiltInRegistries.FLUID.getKey(fluid));
	}

	public static FluidType of(Fluid fluid) {
		return TYPES.computeIfAbsent(fluid, FluidType::new);
	}

	public Fluid getFluid() {
		return fluid;
	}

	public String getDescriptionId() {
		return descriptionId;
	}

	public int getBucketVolume() {
		return BUCKET_VOLUME;
	}
}
