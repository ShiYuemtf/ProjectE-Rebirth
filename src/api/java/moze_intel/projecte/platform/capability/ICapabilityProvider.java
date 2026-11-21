package moze_intel.projecte.platform.capability;

import javax.annotation.Nullable;
import moze_intel.projecte.platform.util.LazyOptional;
import net.minecraft.core.Direction;

/**
 * Objects that expose optional capabilities. Mirrors Forge's {@code ICapabilityProvider} so that the ported item/block entity code keeps its shape; the lookup
 * entry point is {@link CapabilityHelper}.
 */
public interface ICapabilityProvider {

	<T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side);

	default <T> LazyOptional<T> getCapability(Capability<T> capability) {
		return getCapability(capability, null);
	}

	/**
	 * Drops every capability this provider handed out. Vanilla has no capability lifecycle, so this is only invoked by the provider itself.
	 */
	default void invalidateCaps() {
	}
}
