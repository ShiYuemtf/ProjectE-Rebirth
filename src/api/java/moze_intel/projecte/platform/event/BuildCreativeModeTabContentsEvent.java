package moze_intel.projecte.platform.event;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Fabric side stand-in for Forge's {@code BuildCreativeModeTabContentsEvent}. Replaces Forge's event bus hook with the Fabric item group entry callback, which
 * reports the tab instance rather than its key, so {@code CreativeTabDeferredRegister} resolves the key once per call.
 */
public class BuildCreativeModeTabContentsEvent {

	private final ResourceKey<CreativeModeTab> tabKey;
	private final CreativeModeTab.Output output;

	public BuildCreativeModeTabContentsEvent(ResourceKey<CreativeModeTab> tabKey, CreativeModeTab.Output output) {
		this.tabKey = tabKey;
		this.output = output;
	}

	public ResourceKey<CreativeModeTab> getTabKey() {
		return tabKey;
	}

	public void accept(ItemStack stack) {
		output.accept(stack);
	}

	public void accept(ItemLike item) {
		output.accept(new ItemStack(item));
	}

	public void accept(ItemStack stack, CreativeModeTab.TabVisibility visibility) {
		output.accept(stack, visibility);
	}
}
