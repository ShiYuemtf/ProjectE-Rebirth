package moze_intel.projecte.mixin;

import moze_intel.projecte.PECore;
import moze_intel.projecte.capability.CapabilityHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Persists the player capabilities. Forge attached an {@code ICapabilitySerializable} to the entity and its NBT was written for free; Fabric has no such hook, so
 * the values are written into the player's own tag.
 * <p>
 * Targeted at {@link Player} rather than {@code ServerPlayer} because {@code addAdditionalSaveData}/{@code readAdditionalSaveData} are declared on {@code Player}
 * and only the server instance ever reaches the save/load path, so there is nothing to gain from the narrower target and a real risk of the injector silently
 * attaching to a synthetic bridge.
 */
@Mixin(Player.class)
public abstract class PlayerCapabilityMixin {

	private static final String PROJECTE_CAPS = CapabilityHelper.NBT_KEY;
	private static final String KNOWLEDGE_KEY = "moze_intel.projecte.api.capabilities.IKnowledgeProvider";

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void projecte$saveCapabilities(CompoundTag tag, CallbackInfo ci) {
		CompoundTag caps = CapabilityHelper.serializePlayerCapabilities((Player) (Object) this);
		tag.put(PROJECTE_CAPS, caps);
		CompoundTag knowledge = caps.getCompound(KNOWLEDGE_KEY);
		PECore.debugLog("Saving player caps for {}: knowledge={} emc={}",
				((Player) (Object) this).getGameProfile().getName(), knowledgeSize(knowledge), knowledge.getString("transmutationEmc"));
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void projecte$loadCapabilities(CompoundTag tag, CallbackInfo ci) {
		boolean present = tag.contains(PROJECTE_CAPS, Tag.TAG_COMPOUND);
		if (present) {
			CompoundTag caps = tag.getCompound(PROJECTE_CAPS);
			CapabilityHelper.deserializePlayerCapabilities((Player) (Object) this, caps);
			CompoundTag knowledge = caps.getCompound(KNOWLEDGE_KEY);
			PECore.debugLog("Loaded player caps for {}: knowledge={} emc={}",
					((Player) (Object) this).getGameProfile().getName(), knowledgeSize(knowledge), knowledge.getString("transmutationEmc"));
		} else {
			PECore.debugLog("No player caps on disk for {}", ((Player) (Object) this).getGameProfile().getName());
		}
	}

	private static int knowledgeSize(CompoundTag knowledge) {
		ListTag list = knowledge.getList("knowledge", Tag.TAG_COMPOUND);
		return list.size();
	}
}
