package moze_intel.projecte.platform.misc;

/**
 * Fabric side stand-in for Forge's {@code Dist}.
 */
public enum Dist {
	CLIENT,
	DEDICATED_SERVER;

	public boolean isClient() {
		return this == CLIENT;
	}

	public boolean isDedicatedServer() {
		return this == DEDICATED_SERVER;
	}
}
