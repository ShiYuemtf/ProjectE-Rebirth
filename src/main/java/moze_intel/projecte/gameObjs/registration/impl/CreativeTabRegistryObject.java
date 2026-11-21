package moze_intel.projecte.gameObjs.registration.impl;

import moze_intel.projecte.gameObjs.registration.WrappedRegistryObject;
import net.minecraft.world.item.CreativeModeTab;
import moze_intel.projecte.platform.registry.RegistryObject;

public class CreativeTabRegistryObject extends WrappedRegistryObject<CreativeModeTab> {

	public CreativeTabRegistryObject(RegistryObject<CreativeModeTab> registryObject) {
		super(registryObject);
	}
}