package moze_intel.projecte.platform.misc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fabric side stand-in for Forge's {@code ToolAction}. Vanilla 1.20.1 has no tool action system (it landed in 1.20.2), so this is an inert identity token: the
 * ProjectE tools still expose {@code canPerformAction}, but nothing in vanilla consults it on this version.
 * <p>
 * shortcut: tool actions are cosmetic on 1.20.1 Fabric; wire them into real behaviour only if 1.20.1 gets a tool action hook
 */
public final class ToolAction {

	private static final Map<String, ToolAction> VALUES = new ConcurrentHashMap<>();

	private final String name;

	private ToolAction(String name) {
		this.name = name;
	}

	public static ToolAction get(String name) {
		return VALUES.computeIfAbsent(name, ToolAction::new);
	}

	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return "ToolAction[" + name + "]";
	}
}
