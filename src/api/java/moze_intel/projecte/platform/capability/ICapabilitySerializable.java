package moze_intel.projecte.platform.capability;

import moze_intel.projecte.platform.util.INBTSerializable;
import net.minecraft.nbt.Tag;

/**
 * Fabric side stand-in for Forge's {@code ICapabilitySerializable}: a provider whose capabilities can be written to and read from NBT.
 */
public interface ICapabilitySerializable<T extends Tag> extends ICapabilityProvider, INBTSerializable<T> {
}
