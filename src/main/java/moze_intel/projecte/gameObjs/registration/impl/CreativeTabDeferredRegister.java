package moze_intel.projecte.gameObjs.registration.impl;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import moze_intel.projecte.platform.event.BuildCreativeModeTabContentsEvent;
import moze_intel.projecte.gameObjs.registration.WrappedDeferredRegister;
import moze_intel.projecte.utils.text.ILangEntry;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;

public class CreativeTabDeferredRegister extends WrappedDeferredRegister<CreativeModeTab> {

	private final Consumer<BuildCreativeModeTabContentsEvent> addToExistingTabs;
	private final String modid;

	public CreativeTabDeferredRegister(String modid, Consumer<BuildCreativeModeTabContentsEvent> addToExistingTabs) {
		super(Registries.CREATIVE_MODE_TAB, modid);
		this.modid = modid;
		this.addToExistingTabs = addToExistingTabs;
	}

	@Override
	public void register() {
		super.register();
		//Fabric reports the tab instance, our ported call sites compare against ResourceKeys like CreativeModeTabs.BUILDING_BLOCKS
		ItemGroupEvents.MODIFY_ENTRIES_ALL.register((tab, entries) -> {
			ResourceKey<CreativeModeTab> key = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(tab).orElse(null);
			if (key != null) {
				addToExistingTabs.accept(new BuildCreativeModeTabContentsEvent(key, entries));
			}
		});
	}

	/**
	 * @apiNote We manually require the title and icon to be passed so that we ensure all tabs have one.
	 */
	public CreativeTabRegistryObject registerMain(ILangEntry title, ItemLike icon, UnaryOperator<CreativeModeTab.Builder> operator) {
		return register(modid, title, icon, operator);
	}

	/**
	 * @apiNote We manually require the title and icon to be passed so that we ensure all tabs have one.
	 */
	public CreativeTabRegistryObject register(String name, ILangEntry title, ItemLike icon, UnaryOperator<CreativeModeTab.Builder> operator) {
		return register(name, () -> {
			//FabricItemGroup.builder is required on Fabric: a vanilla-built tab is rejected by the item group registry
			CreativeModeTab.Builder builder = FabricItemGroup.builder()
					.title(title.translate())
					.icon(() -> icon.asItem().getDefaultInstance());
			return operator.apply(builder).build();
		}, CreativeTabRegistryObject::new);
	}
}
