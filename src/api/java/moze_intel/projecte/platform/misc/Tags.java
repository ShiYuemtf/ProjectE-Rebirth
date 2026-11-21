package moze_intel.projecte.platform.misc;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Fabric side stand-in for Forge's {@code Tags}, restricted to the common {@code forge:} tags ProjectE actually reads.
 * <p>
 * The {@code forge} namespace is kept on purpose: the generated recipes and tags that ship with the mod reference {@code #forge:...}, so those tag
 * files are emitted under {@code data/forge/tags/} instead of being rewritten to a Fabric specific name.
 */
public class Tags {

	private Tags() {
	}

	public static class Items {

		public static final TagKey<Item> ORES = tag("ores");
		public static final TagKey<Item> RAW_MATERIALS = tag("raw_materials");

		private Items() {
		}

		private static TagKey<Item> tag(String name) {
			return TagKey.create(Registries.ITEM, new ResourceLocation("forge", name));
		}
	}

	public static class Blocks {

		public static final TagKey<Block> ORES = tag("ores");
		public static final TagKey<Block> GRAVEL = tag("gravel");

		private Blocks() {
		}

		private static TagKey<Block> tag(String name) {
			return TagKey.create(Registries.BLOCK, new ResourceLocation("forge", name));
		}
	}
}
