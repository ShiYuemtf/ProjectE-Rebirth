package moze_intel.projecte.platform.misc;

import java.util.Set;

/**
 * Fabric side stand-in for Forge's {@code ToolActions}: the named action groups ProjectE's tools advertise.
 */
public class ToolActions {

	public static final ToolAction AXE_DIG = ToolAction.get("axe_dig");
	public static final ToolAction PICKAXE_DIG = ToolAction.get("pickaxe_dig");
	public static final ToolAction SHOVEL_DIG = ToolAction.get("shovel_dig");
	public static final ToolAction HOE_DIG = ToolAction.get("hoe_dig");
	public static final ToolAction SHEARS_DIG = ToolAction.get("shears_dig");
	public static final ToolAction SWORD_DIG = ToolAction.get("sword_dig");
	public static final ToolAction AXE_STRIP = ToolAction.get("axe_strip");
	public static final ToolAction AXE_SCRAPE = ToolAction.get("axe_scrape");
	public static final ToolAction AXE_WAX_OFF = ToolAction.get("axe_wax_off");
	public static final ToolAction SHOVEL_FLATTEN = ToolAction.get("shovel_flatten");
	public static final ToolAction HOE_TILL = ToolAction.get("hoe_till");
	public static final ToolAction SHEARS_HARVEST = ToolAction.get("shears_harvest");

	public static final Set<ToolAction> DEFAULT_AXE_ACTIONS = Set.of(AXE_DIG, AXE_STRIP, AXE_SCRAPE, AXE_WAX_OFF);
	public static final Set<ToolAction> DEFAULT_PICKAXE_ACTIONS = Set.of(PICKAXE_DIG);
	public static final Set<ToolAction> DEFAULT_SHOVEL_ACTIONS = Set.of(SHOVEL_DIG, SHOVEL_FLATTEN);
	public static final Set<ToolAction> DEFAULT_HOE_ACTIONS = Set.of(HOE_DIG, HOE_TILL);
	public static final Set<ToolAction> DEFAULT_SHEARS_ACTIONS = Set.of(SHEARS_DIG, SHEARS_HARVEST);
	public static final Set<ToolAction> DEFAULT_SWORD_ACTIONS = Set.of(SWORD_DIG);

	private ToolActions() {
	}
}
