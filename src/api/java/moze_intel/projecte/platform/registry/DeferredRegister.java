package moze_intel.projecte.platform.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Fabric side stand-in for Forge's {@code DeferredRegister}. Forge hooks a registry event to fill the objects; Fabric has no equivalent phase, so
 * {@link #register()} performs the registrations immediately and must be called from the mod initialiser (order decides cross-registry dependencies such as
 * blocks before their block items).
 */
public class DeferredRegister<T> {

	private final Registry<T> registry;
	private final String modid;
	private final List<RegistryObject<? extends T>> pending = new ArrayList<>();
	private final Map<String, RegistryObject<? extends T>> byName = new LinkedHashMap<>();

	private DeferredRegister(Registry<T> registry, String modid) {
		this.registry = registry;
		this.modid = modid;
	}

	public static <T> DeferredRegister<T> create(IForgeRegistry<T> forgeRegistry, String modid) {
		return new DeferredRegister<>(forgeRegistry.unwrap(), modid);
	}

	@SuppressWarnings("unchecked")
	public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> registryKey, String modid) {
		Registry<?> registry = BuiltInRegistries.REGISTRY.get(registryKey.location());
		if (registry == null) {
			throw new IllegalStateException("Unknown registry " + registryKey.location());
		}
		return new DeferredRegister<>((Registry<T>) registry, modid);
	}

	public static <T> DeferredRegister<T> create(Registry<T> registry, String modid) {
		return new DeferredRegister<>(registry, modid);
	}

	public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> supplier) {
		RegistryObject<I> object = RegistryObject.deferred(new ResourceLocation(modid, name), supplier);
		pending.add(object);
		byName.put(name, object);
		return object;
	}

	/**
	 * Creates and registers every entry queued so far. Safe to call once per register from the mod initialiser.
	 */
	public void register() {
		for (RegistryObject<? extends T> object : pending) {
			T value = object.get();
			Registry.register(registry, object.getId(), value);
		}
	}

	public Registry<T> getRegistry() {
		return registry;
	}

	public String getNamespace() {
		return modid;
	}

	public Collection<RegistryObject<? extends T>> getEntries() {
		return List.copyOf(byName.values());
	}
}
