package moze_intel.projecte.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nbt.INBTProcessor;
import moze_intel.projecte.api.nbt.NBTProcessor;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.platform.misc.ModList;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import org.jetbrains.annotations.Nullable;

/**
 * Discovers EMC mappers, recipe type mappers and NBT processors.
 * <p>
 * Forge found these by scanning every mod's {@code ModFileScanData} for the annotations. Fabric has no annotation scanning across mods, so each mod lists its
 * implementation class names in its own {@code fabric.mod.json} under the custom keys below, and the classes still carry the annotation that supplies the
 * priority and the required mods. The two ways matter: the annotation is the single source of truth for ordering, the JSON only replaces the scan.
 */
public class AnnotationHelper {

	private static final String EMC_MAPPERS_KEY = "projecte:emc_mappers";
	private static final String RECIPE_TYPE_MAPPERS_KEY = "projecte:recipe_type_mappers";
	private static final String NBT_PROCESSORS_KEY = "projecte:nbt_processors";

	public static List<INBTProcessor> getNBTProcessors() {
		List<INBTProcessor> nbtProcessors = new ArrayList<>();
		Map<INBTProcessor, Integer> priorities = new HashMap<>();
		for (String className : findDeclaredClasses(NBT_PROCESSORS_KEY)) {
			NBTProcessor annotation = getAnnotation(className, NBTProcessor.class);
			if (annotation != null && checkRequiredMods(className, annotation.requiredMods())) {
				//If all the mods were loaded then attempt to get the processor
				INBTProcessor processor = getNBTProcessor(className);
				if (processor != null) {
					int priority = annotation.priority();
					nbtProcessors.add(processor);
					priorities.put(processor, priority);
					PECore.LOGGER.info("Found and loaded NBT Processor: {}, with priority {}", processor.getName(), priority);
				}
			}
		}
		nbtProcessors.sort(Collections.reverseOrder(Comparator.comparing(priorities::get)));
		return nbtProcessors;
	}

	public static List<IRecipeTypeMapper> getRecipeTypeMappers() {
		List<IRecipeTypeMapper> recipeTypeMappers = new ArrayList<>();
		Map<IRecipeTypeMapper, Integer> priorities = new HashMap<>();
		for (String className : findDeclaredClasses(RECIPE_TYPE_MAPPERS_KEY)) {
			RecipeTypeMapper annotation = getAnnotation(className, RecipeTypeMapper.class);
			if (annotation != null && checkRequiredMods(className, annotation.requiredMods())) {
				//If all the mods were loaded then attempt to get the mapper
				IRecipeTypeMapper mapper = getRecipeTypeMapper(className);
				if (mapper != null) {
					int priority = annotation.priority();
					recipeTypeMappers.add(mapper);
					priorities.put(mapper, priority);
					PECore.LOGGER.info("Found and loaded RecipeType Mapper: {}, with priority {}", mapper.getName(), priority);
				}
			}
		}
		recipeTypeMappers.sort(Collections.reverseOrder(Comparator.comparing(priorities::get)));
		return recipeTypeMappers;
	}

	//Note: We don't bother caching this value because EMCMappingHandler#loadMappers caches our processed result
	@SuppressWarnings("unchecked")
	public static List<IEMCMapper<NormalizedSimpleStack, Long>> getEMCMappers() {
		List<IEMCMapper<NormalizedSimpleStack, Long>> emcMappers = new ArrayList<>();
		Map<IEMCMapper<NormalizedSimpleStack, Long>, Integer> priorities = new HashMap<>();
		for (String className : findDeclaredClasses(EMC_MAPPERS_KEY)) {
			EMCMapper annotation = getAnnotation(className, EMCMapper.class);
			if (annotation != null && checkRequiredMods(className, annotation.requiredMods())) {
				//If all the mods were loaded then attempt to get the mapper
				IEMCMapper<?, ?> mapper = getEMCMapper(className);
				if (mapper != null) {
					try {
						IEMCMapper<NormalizedSimpleStack, Long> emcMapper = (IEMCMapper<NormalizedSimpleStack, Long>) mapper;
						int priority = annotation.priority();
						emcMappers.add(emcMapper);
						priorities.put(emcMapper, priority);
						PECore.LOGGER.info("Found and loaded EMC mapper: {}, with priority {}", mapper.getName(), priority);
					} catch (ClassCastException e) {
						PECore.LOGGER.error("{}: Is not a mapper for {}, to {}", mapper.getClass(), NormalizedSimpleStack.class, Long.class, e);
					}
				}
			}
		}
		emcMappers.sort(Collections.reverseOrder(Comparator.comparing(priorities::get)));
		return emcMappers;
	}

	/**
	 * Collects the class names every mod declared under {@code customKey}. Reading every mod rather than only ProjectE keeps the addon contract of the Forge
	 * version: another mod may ship its own mappers.
	 */
	private static List<String> findDeclaredClasses(String customKey) {
		List<String> classNames = new ArrayList<>();
		for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
			CustomValue value = mod.getMetadata().getCustomValue(customKey);
			if (value == null) {
				continue;
			}
			if (value.getType() != CustomValue.CvType.ARRAY) {
				PECore.LOGGER.error("Mod {} declared {} but it is not an array of class names", mod.getMetadata().getId(), customKey);
				continue;
			}
			for (CustomValue entry : value.getAsArray()) {
				if (entry.getType() == CustomValue.CvType.STRING) {
					classNames.add(entry.getAsString());
				} else {
					PECore.LOGGER.error("Mod {} declared a non string entry in its {} array", mod.getMetadata().getId(), customKey);
				}
			}
		}
		return classNames;
	}

	@Nullable
	private static <ANNOTATION extends Annotation> ANNOTATION getAnnotation(String className, Class<ANNOTATION> annotationClass) {
		try {
			//Load without initializing so the annotation can be checked before any static initializer touches classes of a missing mod
			Class<?> clazz = Class.forName(className, false, AnnotationHelper.class.getClassLoader());
			ANNOTATION annotation = clazz.getAnnotation(annotationClass);
			if (annotation == null) {
				PECore.LOGGER.error("Class {} is declared as a {} but is missing the annotation", className, annotationClass.getSimpleName());
			}
			return annotation;
		} catch (ClassNotFoundException | LinkageError e) {
			PECore.LOGGER.error("Failed to load declared class: {}", className, e);
			return null;
		}
	}

	@Nullable
	private static IEMCMapper<?, ?> getEMCMapper(String className) {
		return createOrGetInstance(className, IEMCMapper.class, EMCMapper.Instance.class, IEMCMapper::getName);
	}

	@Nullable
	private static IRecipeTypeMapper getRecipeTypeMapper(String className) {
		return createOrGetInstance(className, IRecipeTypeMapper.class, RecipeTypeMapper.Instance.class, IRecipeTypeMapper::getName);
	}

	@Nullable
	private static INBTProcessor getNBTProcessor(String className) {
		return createOrGetInstance(className, INBTProcessor.class, NBTProcessor.Instance.class, INBTProcessor::getName);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private static <T> T createOrGetInstance(String className, Class<T> baseClass, Class<? extends Annotation> instanceAnnotation, Function<T, String> nameFunction) {
		//Try to create an instance of the class
		try {
			Class<? extends T> subClass = Class.forName(className).asSubclass(baseClass);
			//First try looking at the fields of the class to see if one of them is specified as the instance
			Field[] fields = subClass.getDeclaredFields();
			for (Field field : fields) {
				if (field.isAnnotationPresent(instanceAnnotation)) {
					if (Modifier.isStatic(field.getModifiers())) {
						try {
							Object fieldValue = field.get(null);
							if (baseClass.isInstance(fieldValue)) {
								T instance = (T) fieldValue;
								PECore.debugLog("Found specified {} instance for: {}. Using it rather than creating a new instance.", baseClass.getSimpleName(),
										nameFunction.apply(instance));
								return instance;
							} else {
								PECore.LOGGER.error("{} annotation found on non {} field: {}", instanceAnnotation.getSimpleName(), baseClass.getSimpleName(), field);
								return null;
							}
						} catch (IllegalAccessException e) {
							PECore.LOGGER.error("{} annotation found on inaccessible field: {}", instanceAnnotation.getSimpleName(), field);
							return null;
						}
					} else {
						PECore.LOGGER.error("{} annotation found on non static field: {}", instanceAnnotation.getSimpleName(), field);
						return null;
					}
				}
			}
			//If we don't have any fields that have the Instance annotation, then try to create a new instance of the class
			return subClass.getDeclaredConstructor().newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | LinkageError | InvocationTargetException | NoSuchMethodException e) {
			PECore.LOGGER.error("Failed to load: {}", className, e);
		}
		return null;
	}

	private static boolean checkRequiredMods(String className, String[] requiredMods) {
		for (String modid : requiredMods) {
			//The annotations default to a single empty string for "no dependencies"
			if (!modid.isEmpty() && !ModList.get().isLoaded(modid)) {
				PECore.debugLog("Skipped loading class {}, as its required mods ({}) are not loaded.", className, String.join(", ", requiredMods));
				return false;
			}
		}
		return true;
	}
}
