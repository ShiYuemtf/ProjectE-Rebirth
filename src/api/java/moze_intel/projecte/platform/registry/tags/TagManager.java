package moze_intel.projecte.platform.registry.tags;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

/**
 * {@link ITagManager} over a vanilla registry. Tag contents are resolved on every call so datapack reloads are picked up without re-creating the manager.
 */
public class TagManager<T> implements ITagManager<T> {

	private final Registry<T> registry;

	public TagManager(Registry<T> registry) {
		this.registry = registry;
	}

	@Override
	public ITag<T> getTag(TagKey<T> key) {
		return new RegistryTag(key);
	}

	@Override
	public TagKey<T> createTagKey(ResourceLocation location) {
		return TagKey.create(registry.key(), location);
	}

	@Override
	public boolean isKnownTagName(TagKey<T> key) {
		return registry.getTag(key).isPresent();
	}

	@Override
	public ITag<T> emptyTag() {
		return new RegistryTag(null);
	}

	@Override
	public Stream<ITag<T>> getTags() {
		return registry.getTags().map(entry -> (ITag<T>) new RegistryTag(entry.getFirst()));
	}

	private final class RegistryTag implements ITag<T> {

		private final TagKey<T> key;

		private RegistryTag(TagKey<T> key) {
			this.key = key;
		}

		@Override
		public TagKey<T> getKey() {
			return key;
		}

		private Optional<? extends HolderSet.Named<T>> named() {
			return key == null ? Optional.empty() : registry.getTag(key);
		}

		@Override
		public boolean contains(T value) {
			return named().map(named -> named.stream().anyMatch(holder -> holder.value() == value)).orElse(false);
		}

		@Override
		public boolean isEmpty() {
			return named().map(named -> named.size() == 0).orElse(true);
		}

		@Override
		public int size() {
			return named().map(HolderSet.Named::size).orElse(0);
		}

		@Override
		public Stream<T> stream() {
			return named().<Stream<T>>map(named -> named.stream().map(Holder::value)).orElseGet(Stream::empty);
		}
	}
}
