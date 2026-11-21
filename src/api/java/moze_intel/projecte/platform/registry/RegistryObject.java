package moze_intel.projecte.platform.registry;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Fabric side stand-in for Forge's {@code RegistryObject}. Forge defers object creation to registry events; here {@link DeferredRegister#register()} creates and
 * registers everything eagerly during mod initialisation, and this class only carries the resolved instance.
 */
public class RegistryObject<T> implements Supplier<T> {

	private final ResourceLocation id;
	private final Supplier<? extends T> factory;
	private final Registry<T> lookupRegistry;
	private T value;

	private RegistryObject(ResourceLocation id, Supplier<? extends T> factory, @Nullable Registry<T> lookupRegistry) {
		this.id = id;
		this.factory = factory;
		this.lookupRegistry = lookupRegistry;
	}

	static <T> RegistryObject<T> deferred(ResourceLocation id, Supplier<? extends T> factory) {
		return new RegistryObject<>(id, factory, null);
	}

	/**
	 * Creates a reference to an object that another mod (or ProjectE itself, through a different deferred register) registers. Mirrors Forge's
	 * {@code RegistryObject.create}.
	 */
	public static <T> RegistryObject<T> create(ResourceLocation id, IForgeRegistry<T> registry) {
		return new RegistryObject<>(id, null, registry.unwrap());
	}

	public static <T> RegistryObject<T> create(ResourceLocation id, Registry<T> registry) {
		return new RegistryObject<>(id, null, registry);
	}

	void bind(T value) {
		this.value = value;
	}

	@Override
	public T get() {
		if (value == null) {
			if (lookupRegistry != null) {
				value = lookupRegistry.get(id);
			} else if (factory != null) {
				value = factory.get();
			}
			if (value == null) {
				throw new IllegalStateException("Registry object " + id + " is not present");
			}
		}
		return value;
	}

	public boolean isPresent() {
		if (value != null) {
			return true;
		}
		return lookupRegistry != null ? lookupRegistry.containsKey(id) : factory != null;
	}

	public void ifPresent(Consumer<? super T> consumer) {
		if (isPresent()) {
			consumer.accept(get());
		}
	}

	public Stream<T> stream() {
		return isPresent() ? Stream.of(get()) : Stream.empty();
	}

	public ResourceLocation getId() {
		return id;
	}

	public ResourceKey<T> getKey() {
		return ResourceKey.create(lookupRegistry != null ? lookupRegistry.key() : null, id);
	}

	public Optional<ResourceKey<T>> getKeyOrNull() {
		return Optional.ofNullable(getKey());
	}
}
