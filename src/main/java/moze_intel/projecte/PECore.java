package moze_intel.projecte;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import com.mojang.brigadier.CommandDispatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.capability.CapabilityHelper;
import moze_intel.projecte.platform.capability.CompatCapabilities;
import moze_intel.projecte.platform.config.ModConfigEvent;
import moze_intel.projecte.platform.fluid.FluidStack;
import moze_intel.projecte.platform.fluid.FluidType;
import moze_intel.projecte.platform.fluid.IFluidHandler;
import moze_intel.projecte.platform.misc.FMLEnvironment;
import moze_intel.projecte.platform.misc.ModContainer;
import moze_intel.projecte.platform.misc.ServerLifecycleHooks;
import moze_intel.projecte.config.CustomEMCParser;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.emc.EMCMappingHandler;
import moze_intel.projecte.emc.json.NSSSerializer;
import moze_intel.projecte.emc.mappers.recipe.CraftingMapper;
import moze_intel.projecte.emc.nbt.NBTManager;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.gameObjs.blocks.Pedestal;
import moze_intel.projecte.integration.IntegrationHelper;
import moze_intel.projecte.gameObjs.blocks.ProjectETNT;
import moze_intel.projecte.gameObjs.items.ItemPE;
import moze_intel.projecte.gameObjs.items.rings.Arcana;
import moze_intel.projecte.gameObjs.registries.PEArgumentTypes;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEContainerTypes;
import moze_intel.projecte.gameObjs.registries.PECreativeTabs;
import moze_intel.projecte.gameObjs.registries.PEEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.gameObjs.registries.PERecipeSerializers;
import moze_intel.projecte.gameObjs.registries.PESoundEvents;
import moze_intel.projecte.impl.TransmutationOffline;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.ThreadCheckUUID;
import moze_intel.projecte.network.ThreadCheckUpdate;
import moze_intel.projecte.network.commands.EMCCMD;
import moze_intel.projecte.network.commands.KnowledgeCMD;
import moze_intel.projecte.network.commands.RemoveEmcCMD;
import moze_intel.projecte.network.commands.ResetEmcCMD;
import moze_intel.projecte.network.commands.SetEmcCMD;
import moze_intel.projecte.network.commands.ShowBagCMD;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.WorldTransmutations;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import moze_intel.projecte.platform.config.ModConfig;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;

public class PECore implements ModInitializer {

	public static final String MODID = ProjectEAPI.PROJECTE_MODID;
	public static final String MODNAME = "ProjectE";
	public static final GameProfile FAKEPLAYER_GAMEPROFILE = new GameProfile(UUID.fromString("590e39c7-9fb6-471b-a4c2-c0e539b2423d"), "[" + MODNAME + "]");
	public static final Logger LOGGER = LogUtils.getLogger();

	public static final List<String> uuids = new ArrayList<>();

	public static ModContainer MOD_CONTAINER;

	public static void debugLog(String msg, Object... args) {
		if (!FMLEnvironment.production || ProjectEConfig.common.debugLogging.get()) {
			LOGGER.info(msg, args);
		} else {
			LOGGER.debug(msg, args);
		}
	}

	public static ResourceLocation rl(String path) {
		return new ResourceLocation(MODID, path);
	}

	@Override
	public void onInitialize() {
		MOD_CONTAINER = new ModContainer(MODID);

		//Fabric has no registry event, so entries are registered here in dependency order: block entity types before blocks, blocks before their block items
		PEArgumentTypes.ARGUMENT_TYPES.register();
		PEBlockEntityTypes.BLOCK_ENTITY_TYPES.register();
		PEBlocks.BLOCKS.register();
		PEContainerTypes.CONTAINER_TYPES.register();
		PECreativeTabs.CREATIVE_TABS.register();
		PEEntityTypes.ENTITY_TYPES.register();
		PEItems.ITEMS.register();
		PERecipeSerializers.RECIPE_SERIALIZERS.register();
		PESoundEvents.SOUND_EVENTS.register();

		//Register our config files, then drop their cached values whenever Forge Config API Port loads, reloads or unloads them
		ModConfig.onLoad(config -> {
			if (MODID.equals(config.getModId())) {
				onConfigChange(config, new ModConfigEvent.Loading(config));
			}
		});
		ModConfig.onUnload(config -> {
			if (MODID.equals(config.getModId())) {
				onConfigChange(config, new ModConfigEvent.Unloading(config));
			}
		});
		ProjectEConfig.register();
		ProjectEConfig.loadAll();

		new ThreadCheckUpdate().start();

		EMCMappingHandler.loadMappers();
		CraftingMapper.loadMappers();
		NBTManager.loadProcessors();

		//Ensure our tags are all initialized
		PETags.init();
		IntegrationHelper.registerTrinkets();
		PacketHandler.register();
		WorldTransmutations.init();
		NSSSerializer.init();
		registerDispenseBehaviors();
		registerBlockEvents();

		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			ServerLifecycleHooks.setCurrentServer(server);
			if (!ThreadCheckUUID.hasRunServer()) {
				new ThreadCheckUUID(true).start();
			}
		});
		ServerLifecycleEvents.SERVER_STARTED.register(PECore::updateEmcMapping);
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
			//Re-run the mapping when a datapack changes recipes or tags, this replaces Forge's TagsUpdatedEvent hook
			if (success && server != null) {
				updateEmcMapping(server);
			}
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			//Ensure we save any changes to the custom emc file
			CustomEMCParser.flush();
			//Safe now: vanilla has already written the player files, so dropping the cached providers cannot
			//blank out the knowledge/EMC data that was just saved.
			CapabilityHelper.pruneOfflinePlayers();
			TransmutationOffline.cleanAll();
			EMCMappingHandler.clearEmcMap();
			ServerLifecycleHooks.setCurrentServer(null);
		});
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> registerCommands(dispatcher, buildContext));
	}

	private static void onConfigChange(ModConfig config, ModConfigEvent event) {
		if (config.getModId().equals(MODID)) {
			ProjectEConfig.clearCache(event);
		}
	}

	private static void updateEmcMapping(MinecraftServer server) {
		long start = System.currentTimeMillis();
		//Clear the cached created tags
		AbstractNSSTag.clearCreatedTags();
		CustomEMCParser.init();
		try {
			EMCMappingHandler.map(server.getRecipeManager(), server.registryAccess(), server.getResourceManager());
			LOGGER.info("Registered {} EMC values. (took {} ms)", EMCMappingHandler.getEmcMapSize(), System.currentTimeMillis() - start);
			PacketHandler.sendFragmentedEmcPacketToAll();
		} catch (Throwable t) {
			LOGGER.error("Error calculating EMC values", t);
		}
	}

	private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
		dispatcher.register(Commands.literal("projecte")
				.requires(PEPermissions.COMMAND)
				.then(RemoveEmcCMD.register(context))
				.then(ResetEmcCMD.register(context))
				.then(SetEmcCMD.register(context))
				.then(ShowBagCMD.register(context))
				.then(EMCCMD.register(context))
				.then(KnowledgeCMD.register(context))
		);
	}

	private static void registerBlockEvents() {
		//Replaces Forge's Block#onDestroyedByPlayer override: creative breaking of a loaded pedestal ejects its item and keeps the block
		PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
			if (state.getBlock() instanceof Pedestal pedestal && pedestal.handleCreativeBreak(state, level, pos, player)) {
				return false;
			}
			return true;
		});
	}

	private static void registerDispenseBehaviors() {
		registerDispenseBehavior(new ShearsDispenseItemBehavior(), PEItems.DARK_MATTER_SHEARS, PEItems.RED_MATTER_SHEARS, PEItems.RED_MATTER_KATAR);
		DispenserBlock.registerBehavior(PEBlocks.NOVA_CATALYST, PEBlocks.NOVA_CATALYST.getBlock().createDispenseItemBehavior());
		DispenserBlock.registerBehavior(PEBlocks.NOVA_CATACLYSM, PEBlocks.NOVA_CATACLYSM.getBlock().createDispenseItemBehavior());
		registerDispenseBehavior(new OptionalDispenseItemBehavior() {
			@NotNull
			@Override
			protected ItemStack execute(@NotNull BlockSource source, @NotNull ItemStack stack) {
				//Based off the flint and steel dispense behavior
				if (stack.getItem() instanceof Arcana item) {
					if (item.getMode(stack) != 1) {
						//Only allow using the arcana ring to ignite things when on ignition mode
						setSuccess(false);
						return super.execute(source, stack);
					}
				}
				Level level = source.getLevel();
				setSuccess(true);
				Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
				BlockPos pos = source.getPos().relative(direction);
				BlockState state = level.getBlockState(pos);
				if (BaseFireBlock.canBePlacedAt(level, pos, direction)) {
					level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
				} else if (CampfireBlock.canLight(state)) {
					level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, true));
				//ProjectE's TNT overrides Forge's getFlammability, which Fabric replaces with FlammableBlockRegistry; those blocks are
				//not in that registry (registering them there would only let fire consume the block, never prime it), so match them explicitly.
				} else if (state.getBlock() instanceof ProjectETNT || FlammableBlockRegistry.getDefaultInstance().get(state.getBlock()).getBurnChance() > 0) {
					//Forge asked the block whether it catches fire; 1.20.1 has no such hook, so light a fire above the flammable block instead
					BlockPos above = pos.above();
					if (BaseFireBlock.canBePlacedAt(level, above, Direction.UP)) {
						level.setBlockAndUpdate(above, BaseFireBlock.getState(level, above));
					}
					if (state.getBlock() instanceof ProjectETNT projectETNT) {
						//Forge called Block#onCaughtFire here, which spawned the custom primed entity; call it directly so a dispenser
						//igniting a nova block actually detonates instead of silently deleting it. ProjectETNT extends TntBlock, so this
						//branch must remove the block itself or the next redstone pulse primes a second entity from the same block.
						projectETNT.onCaughtFire(state, level, pos, direction, null);
						level.removeBlock(pos, false);
					} else if (state.getBlock() instanceof TntBlock) {
						//Vanilla TNT has no onCaughtFire equivalent, its static helper does the same priming
						TntBlock.explode(level, pos);
						level.removeBlock(pos, false);
					}
				} else {
					setSuccess(false);
				}
				return stack;
			}
		}, PEItems.IGNITION_RING, PEItems.ARCANA_RING);
		DispenserBlock.registerBehavior(PEItems.EVERTIDE_AMULET, new DefaultDispenseItemBehavior() {
			@NotNull
			@Override
			public ItemStack execute(@NotNull BlockSource source, @NotNull ItemStack stack) {
				//Based off of vanilla's bucket dispense behaviors
				// Note: We only do evertide, not volcanite, as placing lava requires EMC
				Level level = source.getLevel();
				Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
				BlockPos pos = source.getPos().relative(direction);
				BlockEntity blockEntity = WorldHelper.getBlockEntity(level, pos);
				Direction sideHit = direction.getOpposite();
				if (blockEntity != null) {
					Optional<IFluidHandler> capability = CapabilityHelper.getCapability(blockEntity, CompatCapabilities.FLUID_HANDLER, sideHit).resolve();
					if (capability.isPresent()) {
						capability.get().fill(new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.EXECUTE);
						return stack;
					}
				}
				BlockState state = level.getBlockState(pos);
				if (state.getBlock() == Blocks.CAULDRON) {
					level.setBlockAndUpdate(pos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 1));
					return stack;
				} else if (state.getBlock() == Blocks.WATER_CAULDRON) {
					if (!((LayeredCauldronBlock) state.getBlock()).isFull(state)) {
						level.setBlockAndUpdate(pos, state.setValue(LayeredCauldronBlock.LEVEL, state.getValue(LayeredCauldronBlock.LEVEL) + 1));
						return stack;
					}
				} else {
					WorldHelper.placeFluid(null, level, pos, Fluids.WATER, !ProjectEConfig.server.items.opEvertide.get());
					level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), PESoundEvents.WATER_MAGIC.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
					return stack;
				}
				return super.execute(source, stack);
			}
		});
		CauldronInteraction.EMPTY.put(PEItems.EVERTIDE_AMULET.get(), (state, level, pos, player, hand, stack) -> {
			//Raise the fill level
			if (!level.isClientSide) {
				level.setBlockAndUpdate(pos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 1));
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		});
		CauldronInteraction.WATER.put(PEItems.EVERTIDE_AMULET.get(), (state, level, pos, player, hand, stack) -> {
			if (((LayeredCauldronBlock) state.getBlock()).isFull(state)) {
				return InteractionResult.PASS;
			} else if (!level.isClientSide) {
				//Raise the fill level
				level.setBlockAndUpdate(pos, state.setValue(LayeredCauldronBlock.LEVEL, state.getValue(LayeredCauldronBlock.LEVEL) + 1));
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		});
		CauldronInteraction.EMPTY.put(PEItems.VOLCANITE_AMULET.get(), (state, level, pos, player, hand, stack) -> {
			if (!level.isClientSide && ItemPE.consumeFuel(player, stack, 32, true)) {
				level.setBlockAndUpdate(pos, Blocks.LAVA_CAULDRON.defaultBlockState());
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		});
	}

	private static void registerDispenseBehavior(DispenseItemBehavior behavior, ItemLike... items) {
		for (ItemLike item : items) {
			DispenserBlock.registerBehavior(item, behavior);
		}
	}
}
