package moze_intel.projecte.platform.registry.tags;

import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

/**
 * Fabric side stand-in for Forge's {@code ITagManager}.
 */
public interface ITagManager<T> {

	ITag<T> getTag(TagKey<T> key);

	TagKey<T> createTagKey(ResourceLocation location);

	boolean isKnownTagName(TagKey<T> key);

	ITag<T> emptyTag();

	Stream<ITag<T>> getTags();
}
