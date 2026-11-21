package moze_intel.projecte.platform.misc;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric side stand-in for Forge's {@code CreativeModeTabRegistry}, which exists because Forge's creative tab registry is not queryable by location.
 * Vanilla's registry already is, so this is a thin lookup.
 */
public class CreativeModeTabRegistry {

	private CreativeModeTabRegistry() {
	}

	@Nullable
	public static CreativeModeTab getTab(ResourceLocation name) {
		return BuiltInRegistries.CREATIVE_MODE_TAB.get(name);
	}
}
