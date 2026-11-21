package moze_intel.projecte.handlers;

import moze_intel.projecte.PECore;
import moze_intel.projecte.capability.managing.BasicCapabilityResolver;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.items.IFlightProvider;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.utils.PlayerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import moze_intel.projecte.platform.capability.Capability;
import moze_intel.projecte.platform.capability.CapabilityManager;
import moze_intel.projecte.platform.capability.CapabilityToken;
import org.jetbrains.annotations.NotNull;

public final class InternalAbilities {

	public static final Capability<InternalAbilities> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
	public static final ResourceLocation NAME = PECore.rl("internal_abilities");

	private final ServerPlayer player;
	private boolean swrgOverride = false;
	private boolean gemArmorReady = false;
	private boolean hadFlightItem = false;
	private boolean wasFlyingGamemode = false;
	private boolean isFlyingGamemode = false;
	private boolean wasFlying = false;
	private int projectileCooldown = 0;
	private int gemChestCooldown = 0;

	public InternalAbilities(ServerPlayer player) {
		this.player = player;
	}

	public void resetProjectileCooldown() {
		projectileCooldown = ProjectEConfig.server.cooldown.player.projectile.get();
	}

	public int getProjectileCooldown() {
		return projectileCooldown;
	}

	public void resetGemCooldown() {
		gemChestCooldown = ProjectEConfig.server.cooldown.player.gemChest.get();
	}

	public int getGemCooldown() {
		return gemChestCooldown;
	}

	public void setGemState(boolean state) {
		gemArmorReady = state;
	}

	public boolean getGemState() {
		return gemArmorReady;
	}

	// Checks if the server state of player caps mismatches with what ProjectE determines. If so, change it serverside and send a packet to client
	public void tick() {
		if (projectileCooldown > 0) {
			projectileCooldown--;
		}

		if (gemChestCooldown > 0) {
			gemChestCooldown--;
		}

		if (!shouldPlayerFly()) {
			if (hadFlightItem) {
				if (player.getAbilities().mayfly) {
					PlayerHelper.updateClientServerFlight(player, false);
				}
				hadFlightItem = false;
			}
			wasFlyingGamemode = false;
			wasFlying = false;
		} else {
			if (!hadFlightItem) {
				if (!player.getAbilities().mayfly) {
					PlayerHelper.updateClientServerFlight(player, true);
				}
				hadFlightItem = true;
			} else if (wasFlyingGamemode && !isFlyingGamemode) {
				//Player was in a gamemode that allowed flight, but no longer is, but they still should be allowed to fly
				//Sync the fact to the client. Also passes wasFlying so that if they were flying previously,
				//and are still allowed to the gamemode change doesn't force them out of it
				PlayerHelper.updateClientServerFlight(player, true, wasFlying);
			}
			wasFlyingGamemode = isFlyingGamemode;
			wasFlying = player.getAbilities().flying;
		}

		//Step assist used to be a Forge attribute modifier (ForgeMod.STEP_HEIGHT_ADDITION); 1.20.1 has no such attribute, so the vanilla step height is
		//modified by moze_intel.projecte.mixin.EntityMixin instead and there is nothing to reconcile per tick here.
	}

	public void onDimensionChange() {
		// Resend everything needed on clientside (all except fire resist)
		PlayerHelper.updateClientServerFlight(player, player.getAbilities().mayfly);
	}

	private boolean shouldPlayerFly() {
		if (!hasSwrg()) {
			disableSwrgFlightOverride();
		}
		isFlyingGamemode = player.isCreative() || player.isSpectator();
		if (isFlyingGamemode || swrgOverride) {
			return true;
		}
		return PlayerHelper.checkArmorHotbarCurios(player, stack -> !stack.isEmpty() && stack.getItem() instanceof IFlightProvider provider && provider.canProvideFlight(stack, player));
	}

	private boolean hasSwrg() {
		return PlayerHelper.checkHotbarCurios(player, stack -> !stack.isEmpty() && stack.getItem() == PEItems.SWIFTWOLF_RENDING_GALE.get());
	}

	public void enableSwrgFlightOverride() {
		swrgOverride = true;
	}

	public void disableSwrgFlightOverride() {
		swrgOverride = false;
	}

	public static class Provider extends BasicCapabilityResolver<InternalAbilities> {

		public Provider(ServerPlayer player) {
			super(() -> new InternalAbilities(player));
		}

		@NotNull
		@Override
		public Capability<InternalAbilities> getMatchingCapability() {
			return CAPABILITY;
		}
	}
}