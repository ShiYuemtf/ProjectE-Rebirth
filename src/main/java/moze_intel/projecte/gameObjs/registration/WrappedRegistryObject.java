package moze_intel.projecte.gameObjs.registration;

import java.util.function.Supplier;
import moze_intel.projecte.platform.registry.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class WrappedRegistryObject<T> implements Supplier<T>, INamedEntry {

	@NotNull
	protected RegistryObject<T> registryObject;

	protected WrappedRegistryObject(@NotNull RegistryObject<T> registryObject) {
		this.registryObject = registryObject;
	}

	@NotNull
	@Override
	public T get() {
		return registryObject.get();
	}

	@Override
	public String getInternalRegistryName() {
		return registryObject.getId().getPath();
	}
}