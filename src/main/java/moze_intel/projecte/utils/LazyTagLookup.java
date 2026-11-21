package moze_intel.projecte.utils;

import net.minecraft.tags.TagKey;
import moze_intel.projecte.platform.util.Lazy;
import moze_intel.projecte.platform.registry.IForgeRegistry;
import moze_intel.projecte.platform.registry.tags.ITag;
import moze_intel.projecte.platform.registry.tags.ITagManager;

//From Mekanism
public record LazyTagLookup<TYPE>(TagKey<TYPE> key, Lazy<ITag<TYPE>> lazyTag) {

	public static <TYPE> LazyTagLookup<TYPE> create(IForgeRegistry<TYPE> registry, TagKey<TYPE> key) {
		return new LazyTagLookup<>(key, Lazy.of(() -> tagManager(registry).getTag(key)));
	}

	public ITag<TYPE> tag() {
		return lazyTag.get();
	}

	public boolean contains(TYPE element) {
		return tag().contains(element);
	}

	public boolean isEmpty() {
		return tag().isEmpty();
	}

	public static <TYPE> ITagManager<TYPE> tagManager(IForgeRegistry<TYPE> registry) {
		ITagManager<TYPE> tags = registry.tags();
		if (tags == null) {
			throw new IllegalStateException("Expected " + registry.getRegistryName() + " to have tags.");
		}
		return tags;
	}
}