package moze_intel.projecte.platform.registry.tags;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

/**
 * Fabric side stand-in for Forge's {@code ITag}. Backed by the vanilla holder set of the same key, so contents follow datapack reloads.
 */
public interface ITag<T> {

	TagKey<T> getKey();

	boolean contains(T value);

	boolean isEmpty();

	int size();

	Stream<T> stream();

	@NotNull
	default Optional<T> getRandomElement(RandomSource random) {
		List<T> values = stream().toList();
		return values.isEmpty() ? Optional.empty() : Optional.of(values.get(random.nextInt(values.size())));
	}
}
