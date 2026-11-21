package moze_intel.projecte.emc.mappers;

import moze_intel.projecte.platform.config.TomlFile;
import java.util.Collections;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.server.packs.resources.ResourceManager;

public class TagMapper implements IEMCMapper<NormalizedSimpleStack, Long> {

	@Override
	public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper, final TomlFile config, RecipeManager recipeManager,
			RegistryAccess registryAccess, ResourceManager resourceManager) {
		AbstractNSSTag.getAllCreatedTags().forEach(stack -> stack.forEachElement(normalizedSimpleStack -> {
			//Tag -> element
			mapper.addConversion(1, stack, Collections.singletonList(normalizedSimpleStack));
			//Element -> tag
			mapper.addConversion(1, normalizedSimpleStack, Collections.singletonList(stack));
		}));
	}

	@Override
	public String getName() {
		return "TagMapper";
	}

	@Override
	public String getDescription() {
		return "Adds back and forth conversions of objects and their Tag variant. (EMC values assigned to tags will not behave properly if this mapper is disabled)";
	}
}