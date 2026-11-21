package moze_intel.projecte.platform.util;

/**
 * Fabric side stand-in for Forge's {@code INBTSerializable}.
 */
public interface INBTSerializable<T> {

	T serializeNBT();

	void deserializeNBT(T nbt);
}
