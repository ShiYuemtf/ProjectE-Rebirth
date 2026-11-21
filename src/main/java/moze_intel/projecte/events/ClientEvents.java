package moze_intel.projecte.events;

import com.mojang.brigadier.CommandDispatcher;
import moze_intel.projecte.capability.CapabilityHelper;
import moze_intel.projecte.gameObjs.entity.EntitySWRGProjectile;
import moze_intel.projecte.gameObjs.sound.MovingSoundSWRG;
import moze_intel.projecte.network.commands.client.DumpMissingEmc;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.world.entity.Entity;

/**
 * Client only. Registered from {@code ClientRegistration#onInitializeClient()}, never loaded on a dedicated server.
 */
public class ClientEvents {

	public static void register() {
		ClientEntityEvents.ENTITY_LOAD.register(ClientEvents::onEntityLoad);
		ClientCommandRegistrationCallback.EVENT.register(ClientEvents::registerClientCommands);
		//Player capabilities are held per Player instance, so the leaving client player has to be dropped or the entry leaks
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			if (client.player != null) {
				CapabilityHelper.invalidatePlayerCapabilities(client.player);
			}
		});
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null) {
				//Player ticking used to arrive through TickEvent.PlayerTickEvent on both sides; the client half lives here
				TickEvents.tickPlayer(client.player);
			}
		});
	}

	private static void onEntityLoad(Entity entity, ClientLevel level) {
		Minecraft mc = Minecraft.getInstance();
		if (entity instanceof EntitySWRGProjectile projectile && mc.mouseHandler.isMouseGrabbed()) {
			mc.getSoundManager().play(new MovingSoundSWRG(projectile, level.getRandom()));
		}
	}

	private static void registerClientCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext context) {
		//Note: We can use projecte as the base command here as it will merge the trees properly
		dispatcher.register(ClientCommandManager.literal("projecte")
				.then(DumpMissingEmc.register(context))
		);
	}
}
