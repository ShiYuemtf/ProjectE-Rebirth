package moze_intel.projecte.platform.capability;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Contract for items that hand out capability providers, standing in for Forge's {@code IItemExtension#initCapabilities}.
 * <p>
 * Forge patches {@link Item} itself, so any item could override {@code initCapabilities} and the capability lookup found it through the item instance. Fabric has no
 * such hook, so the lookup in {@code CapabilityHelper} has to go through a type it can test for. ProjectE's tools extend vanilla item classes ({@code AxeItem},
 * {@code SwordItem}, ...) instead of {@code ItemPE}, so keying the lookup off {@code ItemPE} silently dropped every tool capability: charging, mode switching and
 * projectile shooting all worked on the Philosopher's Stone and did nothing on the tools.
 */
public interface ICapabilityProviderItem {

	/**
	 * @return the capability provider for this stack, or {@code null} when the item supports no capabilities.
	 */
	@Nullable
	ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt);
}
