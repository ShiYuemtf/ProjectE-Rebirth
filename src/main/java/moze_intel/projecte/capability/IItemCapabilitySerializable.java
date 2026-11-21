package moze_intel.projecte.capability;

import net.minecraft.nbt.Tag;
import moze_intel.projecte.platform.util.INBTSerializable;

public interface IItemCapabilitySerializable extends INBTSerializable<Tag> {

	String getStorageKey();
}