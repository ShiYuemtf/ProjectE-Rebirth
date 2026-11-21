package moze_intel.projecte;

import moze_intel.projecte.events.ClientEvents;
import moze_intel.projecte.events.ToolTipEvent;
import moze_intel.projecte.gameObjs.gui.AbstractCollectorScreen;
import moze_intel.projecte.gameObjs.gui.AbstractCondenserScreen;
import moze_intel.projecte.gameObjs.gui.AlchBagScreen;
import moze_intel.projecte.gameObjs.gui.AlchChestScreen;
import moze_intel.projecte.gameObjs.gui.GUIDMFurnace;
import moze_intel.projecte.gameObjs.gui.GUIEternalDensity;
import moze_intel.projecte.gameObjs.gui.GUIMercurialEye;
import moze_intel.projecte.gameObjs.gui.GUIRMFurnace;
import moze_intel.projecte.gameObjs.gui.GUIRelay.GUIRelayMK1;
import moze_intel.projecte.gameObjs.gui.GUIRelay.GUIRelayMK2;
import moze_intel.projecte.gameObjs.gui.GUIRelay.GUIRelayMK3;
import moze_intel.projecte.gameObjs.gui.GUITransmutation;
import moze_intel.projecte.gameObjs.registration.impl.ContainerTypeRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEContainerTypes;
import moze_intel.projecte.gameObjs.registries.PEEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.rendering.ChestRenderer;
import moze_intel.projecte.rendering.EntitySpriteRenderer;
import moze_intel.projecte.rendering.LayerYue;
import moze_intel.projecte.rendering.NovaRenderer;
import moze_intel.projecte.rendering.PedestalRenderer;
import moze_intel.projecte.rendering.TransmutationRenderingOverlay;
import moze_intel.projecte.gameObjs.items.rings.ArchangelSmite;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.to_server.LeftClickArchangelPKT;
import moze_intel.projecte.utils.ClientKeyHelper;
import moze_intel.projecte.utils.Constants;
import moze_intel.projecte.utils.ItemHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import java.util.function.Supplier;
import moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.ItemLike;

public class ClientRegistration implements ClientModInitializer {

	public static final ResourceLocation ACTIVE_OVERRIDE = PECore.rl("active");
	public static final ResourceLocation MODE_OVERRIDE = PECore.rl("mode");

	@Override
	public void onInitializeClient() {
		PacketHandler.registerClientReceiver();
		registerScreens();
		registerPropertyOverrides();
		ClientKeyHelper.registerKeyBindings();
		ClientKeyHelper.register();
		registerRenderers();
		registerLayers();
		TransmutationRenderingOverlay.register();
		ClientEvents.register();
		ToolTipEvent.register();
		registerArchangelEmptySwing();
	}

	private static void registerArchangelEmptySwing() {
		//Forge sent LeftClickArchangelPKT from PlayerInteractEvent.LeftClickEmpty; the closest 1.20.1 equivalent is this client pre attack hook,
		// which runs for every left click, so the hit type has to be checked to avoid double firing on blocks and entities
		ClientPreAttackCallback.EVENT.register((client, player, cooldown) -> {
			if (client.hitResult != null && client.hitResult.getType() == HitResult.Type.MISS
					&& player.getMainHandItem().getItem() instanceof ArchangelSmite) {
				PacketHandler.sendToServer(new LeftClickArchangelPKT());
			}
			return false;
		});
	}

	private static void registerScreens() {
		registerScreen(PEContainerTypes.RM_FURNACE_CONTAINER, GUIRMFurnace::new);
		registerScreen(PEContainerTypes.DM_FURNACE_CONTAINER, GUIDMFurnace::new);
		registerScreen(PEContainerTypes.CONDENSER_CONTAINER, AbstractCondenserScreen.MK1::new);
		registerScreen(PEContainerTypes.CONDENSER_MK2_CONTAINER, AbstractCondenserScreen.MK2::new);
		registerScreen(PEContainerTypes.ALCH_CHEST_CONTAINER, AlchChestScreen::new);
		registerScreen(PEContainerTypes.ALCH_BAG_CONTAINER, AlchBagScreen::new);
		registerScreen(PEContainerTypes.ETERNAL_DENSITY_CONTAINER, GUIEternalDensity::new);
		registerScreen(PEContainerTypes.TRANSMUTATION_CONTAINER, GUITransmutation::new);
		registerScreen(PEContainerTypes.RELAY_MK1_CONTAINER, GUIRelayMK1::new);
		registerScreen(PEContainerTypes.RELAY_MK2_CONTAINER, GUIRelayMK2::new);
		registerScreen(PEContainerTypes.RELAY_MK3_CONTAINER, GUIRelayMK3::new);
		registerScreen(PEContainerTypes.COLLECTOR_MK1_CONTAINER, AbstractCollectorScreen.MK1::new);
		registerScreen(PEContainerTypes.COLLECTOR_MK2_CONTAINER, AbstractCollectorScreen.MK2::new);
		registerScreen(PEContainerTypes.COLLECTOR_MK3_CONTAINER, AbstractCollectorScreen.MK3::new);
		registerScreen(PEContainerTypes.MERCURIAL_EYE_CONTAINER, GUIMercurialEye::new);
	}

	private static void registerPropertyOverrides() {
		//Property Overrides
		addPropertyOverrides(ACTIVE_OVERRIDE, (stack, level, entity, seed) -> ItemHelper.checkItemNBT(stack, Constants.NBT_KEY_ACTIVE) ? 1F : 0F,
				PEItems.GEM_OF_ETERNAL_DENSITY, PEItems.VOID_RING, PEItems.ARCANA_RING, PEItems.ARCHANGEL_SMITE, PEItems.BLACK_HOLE_BAND, PEItems.BODY_STONE,
				PEItems.HARVEST_GODDESS_BAND, PEItems.IGNITION_RING, PEItems.LIFE_STONE, PEItems.MIND_STONE, PEItems.SOUL_STONE, PEItems.WATCH_OF_FLOWING_TIME,
				PEItems.ZERO_RING);
		addPropertyOverrides(MODE_OVERRIDE, (stack, level, entity, seed) -> stack.hasTag() ? stack.getOrCreateTag().getInt(Constants.NBT_KEY_MODE) : 0F,
				PEItems.ARCANA_RING, PEItems.SWIFTWOLF_RENDING_GALE);
		//shortcut: no JEI dependency on Fabric build yet, recipe GUI switching is unavailable
	}

	private static void registerRenderers() {
		//Block Entity
		registerEmcChestRenderer(PEBlockEntityTypes.ALCHEMICAL_CHEST.get(), PECore.rl("textures/block/alchemical_chest.png"), () -> PEBlocks.ALCHEMICAL_CHEST);
		registerEmcChestRenderer(PEBlockEntityTypes.CONDENSER.get(), PECore.rl("textures/block/condenser_mk1.png"), () -> PEBlocks.CONDENSER);
		registerEmcChestRenderer(PEBlockEntityTypes.CONDENSER_MK2.get(), PECore.rl("textures/block/condenser_mk2.png"), () -> PEBlocks.CONDENSER_MK2);
		BlockEntityRendererRegistry.register(PEBlockEntityTypes.DARK_MATTER_PEDESTAL.get(), PedestalRenderer::new);

		//Entities
		EntityRendererRegistry.register(PEEntityTypes.WATER_PROJECTILE.get(), context -> new EntitySpriteRenderer<>(context, PECore.rl("textures/entity/water_orb.png")));
		EntityRendererRegistry.register(PEEntityTypes.LAVA_PROJECTILE.get(), context -> new EntitySpriteRenderer<>(context, PECore.rl("textures/entity/lava_orb.png")));
		EntityRendererRegistry.register(PEEntityTypes.MOB_RANDOMIZER.get(), context -> new EntitySpriteRenderer<>(context, PECore.rl("textures/entity/randomizer.png")));
		EntityRendererRegistry.register(PEEntityTypes.LENS_PROJECTILE.get(), context -> new EntitySpriteRenderer<>(context, PECore.rl("textures/entity/lens_explosive.png")));
		EntityRendererRegistry.register(PEEntityTypes.FIRE_PROJECTILE.get(), context -> new EntitySpriteRenderer<>(context, PECore.rl("textures/entity/fireball.png")));
		EntityRendererRegistry.register(PEEntityTypes.SWRG_PROJECTILE.get(), context -> new EntitySpriteRenderer<>(context, PECore.rl("textures/entity/lightning.png")));
		EntityRendererRegistry.register(PEEntityTypes.NOVA_CATALYST_PRIMED.get(), context -> new NovaRenderer<>(context, PEBlocks.NOVA_CATALYST.getBlock()::defaultBlockState));
		EntityRendererRegistry.register(PEEntityTypes.NOVA_CATACLYSM_PRIMED.get(), context -> new NovaRenderer<>(context, PEBlocks.NOVA_CATACLYSM.getBlock()::defaultBlockState));
		//Explicit witness: TippableArrowRenderer renders Arrow, which is the supertype of EntityHomingArrow
		EntityRendererRegistry.<Arrow>register(PEEntityTypes.HOMING_ARROW.get(), TippableArrowRenderer::new);
	}

	//The Fabric block entity renderer registry is typed on the concrete block entity class, but one ChestRenderer covers every EMC chest
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void registerEmcChestRenderer(BlockEntityType<?> type, ResourceLocation texture, Supplier<BlockRegistryObject<?, ?>> block) {
		BlockEntityRendererRegistry.register((BlockEntityType) type,
				(BlockEntityRendererProvider) context -> new ChestRenderer(context, texture, block));
	}

	private static void registerLayers() {
		//Forge exposed the skin map through EntityRenderersEvent.AddLayers, Fabric only offers a per entity type callback
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityType == EntityType.PLAYER && entityRenderer instanceof PlayerRenderer playerRenderer) {
				registrationHelper.register(new LayerYue(playerRenderer));
			}
		});
	}

	private static void addPropertyOverrides(ResourceLocation override, ClampedItemPropertyFunction propertyGetter, ItemLike... itemProviders) {
		for (ItemLike itemProvider : itemProviders) {
			ItemProperties.register(itemProvider.asItem(), override, propertyGetter);
		}
	}

	private static <C extends AbstractContainerMenu, U extends Screen & MenuAccess<C>> void registerScreen(ContainerTypeRegistryObject<C> type, ScreenConstructor<C, U> factory) {
		MenuScreens.register(type.get(), factory);
	}
}
