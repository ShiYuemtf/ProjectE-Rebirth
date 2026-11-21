package moze_intel.projecte.impl;

/**
 * Forge routed integrations through the {@code InterModComms} channel, which has no Fabric equivalent. shortcut: no Fabric IMC channel, integrations are wired
 * directly, so there is nothing to receive here. The method is kept so that existing call sites still compile.
 */
public class IMCHandler {

	public static void handleMessages() {
	}
}
