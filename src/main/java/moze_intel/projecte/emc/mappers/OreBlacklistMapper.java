package moze_intel.projecte.emc.mappers;

import moze_intel.projecte.platform.config.TomlFile;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.gameObjs.PETags;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;

@EMCMapper
public class OreBlacklistMapper implements IEMCMapper<NormalizedSimpleStack, Long> {

	@Override
	public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper, TomlFile config, RecipeManager recipeManager,
			RegistryAccess registryAccess, ResourceManager resourceManager) {
		for (Item ore : PETags.Items.ORES_LOOKUP.tag().stream().toList()) {
			NSSItem nssOre = NSSItem.createItem(ore);
			mapper.setValueBefore(nssOre, 0L);
			mapper.setValueAfter(nssOre, 0L);
		}
	}

	@Override
	public String getName() {
		return "OresBlacklistMapper";
	}

	@Override
	public String getDescription() {
		return "Set EMC=0 for everything in the forge:ores tag";
	}
}