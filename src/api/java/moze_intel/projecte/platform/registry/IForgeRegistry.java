package moze_intel.projecte.platform.registry;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import moze_intel.projecte.platform.registry.tags.ITagManager;
import moze_intel.projecte.platform.registry.tags.TagManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Fabric side stand-in for Forge's {@code IForgeRegistry}, which on 1.20.1 Fabric is just the vanilla registry plus tag lookup and id helpers.
 */
public class IForgeRegistry<T> {

	private final Registry<T> registry;
	private final ITagManager<T> tags;

	public IForgeRegistry(Registry<T> registry) {
		this.registry = registry;
		this.tags = new TagManager<>(registry);
	}

	public Registry<T> unwrap() {
		return registry;
	}

	public ITagManager<T> tags() {
		return tags;
	}

	public ResourceLocation getRegistryName() {
		return registry.key().location();
	}

	@Nullable
	public ResourceLocation getKey(T value) {
		return registry.getKey(value);
	}

	@Nullable
	public T getValue(@Nullable ResourceLocation key) {
		return key == null ? null : registry.get(key);
	}

	@Nullable
	public ResourceKey<T> getResourceKey(T value) {
		return registry.getResourceKey(value).orElse(null);
	}

	public boolean containsKey(ResourceLocation key) {
		return registry.containsKey(key);
	}

	public boolean containsValue(T value) {
		return registry.getKey(value) != null;
	}

	public Set<Map.Entry<ResourceKey<T>, T>> getEntries() {
		return registry.entrySet();
	}

	public Collection<T> getValues() {
		return registry.stream().toList();
	}

	public Optional<ResourceKey<T>> getResourceKeyOptional(T value) {
		return registry.getResourceKey(value);
	}
}
