package moze_intel.projecte.platform.misc;

import java.util.function.Supplier;

/**
 * Fabric side stand-in for Forge's {@code InterModComms}.
 * <p>
 * Fabric has no inter-mod message channel; mods talk to each other through direct API calls or entrypoints. ProjectE only used IMC to let other mods
 * register NSS serializers and world transmutations at runtime, and both of those are already reachable through the public API, so every send is a
 * no-op here.
 */
public class InterModComms {

	private InterModComms() {
	}

	//shortcut: no Fabric IMC channel, integrations register through ProjectEAPI instead
	public static <T> void sendTo(String modId, String method, Supplier<T> message) {
	}

	//shortcut: no Fabric IMC channel, integrations register through ProjectEAPI instead
	public static <T> void sendToAll(String method, Supplier<T> message) {
	}
}
