package moze_intel.projecte.platform.event;

/**
 * Fabric side stand-in for Forge's {@code MinecraftForge}, reduced to the single game event bus ProjectE posts its API events on.
 */
public class MinecraftForge {

	public static final IEventBus EVENT_BUS = new EventBus();

	private MinecraftForge() {
	}
}
