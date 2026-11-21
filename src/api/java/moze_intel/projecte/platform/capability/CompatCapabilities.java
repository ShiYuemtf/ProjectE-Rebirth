package moze_intel.projecte.platform.capability;

import moze_intel.projecte.api.item_handlers.IItemHandler;
import moze_intel.projecte.platform.fluid.IFluidHandler;
import moze_intel.projecte.platform.fluid.IFluidHandlerItem;

/**
 * Fabric side stand-in for Forge's {@code ForgeCapabilities}: the well known capability keys other mods look for.
 */
public class CompatCapabilities {

	public static final Capability<IItemHandler> ITEM_HANDLER = CapabilityManager.get(IItemHandler.class);
	public static final Capability<IFluidHandler> FLUID_HANDLER = CapabilityManager.get(IFluidHandler.class);
	public static final Capability<IFluidHandlerItem> FLUID_HANDLER_ITEM = CapabilityManager.get(IFluidHandlerItem.class);

	private CompatCapabilities() {
	}
}
