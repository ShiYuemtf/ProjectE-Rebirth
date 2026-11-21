package moze_intel.projecte.platform.config;

import moze_intel.projecte.platform.event.Event;
import moze_intel.projecte.platform.config.ModConfig;

/**
 * Fabric side stand-in for Forge's {@code ModConfigEvent}. Forge Config API Port exposes loading/reloading/unloading as Fabric events, so
 * {@code ProjectEConfig} bridges those into these subclasses to keep the ported {@code clearCache} signatures.
 */
public class ModConfigEvent extends Event {

	private final ModConfig config;

	public ModConfigEvent(ModConfig config) {
		this.config = config;
	}

	public ModConfig getConfig() {
		return config;
	}

	public static class Loading extends ModConfigEvent {

		public Loading(ModConfig config) {
			super(config);
		}
	}

	public static class Reloading extends ModConfigEvent {

		public Reloading(ModConfig config) {
			super(config);
		}
	}

	public static class Unloading extends ModConfigEvent {

		public Unloading(ModConfig config) {
			super(config);
		}
	}
}
