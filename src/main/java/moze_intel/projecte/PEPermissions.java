package moze_intel.projecte;

import java.util.function.Predicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * Command permission gate. Forge's permission API (and the LuckPerms style permission nodes ProjectE exposed) has no Fabric equivalent that is worth the
 * dependency, so every node degrades to a vanilla permission level: node holders get their documented fallback level, and ops keep full access.
 */
public class PEPermissions {

	//Commands
	public static final CommandPermissionNode COMMAND = new CommandPermissionNode(Commands.LEVEL_ALL);

	public static final CommandPermissionNode COMMAND_REMOVE_EMC = new CommandPermissionNode(Commands.LEVEL_GAMEMASTERS);
	public static final CommandPermissionNode COMMAND_RESET_EMC = new CommandPermissionNode(Commands.LEVEL_GAMEMASTERS);
	public static final CommandPermissionNode COMMAND_SET_EMC = new CommandPermissionNode(Commands.LEVEL_GAMEMASTERS);
	public static final CommandPermissionNode COMMAND_SHOW_BAG = new CommandPermissionNode(Commands.LEVEL_GAMEMASTERS);
	public static final CommandPermissionNode COMMAND_EMC = new CommandPermissionNode(Commands.LEVEL_GAMEMASTERS);
	public static final CommandPermissionNode COMMAND_EMC_ADD = new CommandPermissionNode(Commands.LEVEL_GAMEMASTERS);
	public static final CommandPermissionNode COMMAND_EMC_REMOVE = new CommandPermissionNode(Commands.LEVEL_GAMEMASTERS);
	public static final CommandPermissionNode COMMAND_EMC_SET = new CommandPermissionNode(Commands.LEVEL_GAMEMASTERS);
	public static final CommandPermissionNode COMMAND_EMC_TEST = new CommandPermissionNode(Commands.LEVEL_GAMEMASTERS);
	public static final CommandPermissionNode COMMAND_EMC_GET = new CommandPermissionNode(Commands.LEVEL_GAMEMASTERS);
	public static final CommandPermissionNode COMMAND_KNOWLEDGE = new CommandPermissionNode(Commands.LEVEL_GAMEMASTERS);
	public static final CommandPermissionNode COMMAND_KNOWLEDGE_CLEAR = new CommandPermissionNode(Commands.LEVEL_GAMEMASTERS);
	public static final CommandPermissionNode COMMAND_KNOWLEDGE_LEARN = new CommandPermissionNode(Commands.LEVEL_GAMEMASTERS);
	public static final CommandPermissionNode COMMAND_KNOWLEDGE_UNLEARN = new CommandPermissionNode(Commands.LEVEL_GAMEMASTERS);
	public static final CommandPermissionNode COMMAND_KNOWLEDGE_TEST = new CommandPermissionNode(Commands.LEVEL_GAMEMASTERS);

	public static void registerPermissionNodes() {
		//Vanilla permission levels need no registration.
	}

	public record CommandPermissionNode(int fallbackLevel) implements Predicate<CommandSourceStack> {

		@Override
		public boolean test(CommandSourceStack source) {
			return source.hasPermission(fallbackLevel);
		}

		/**
		 * Checks whether a player holds this node. Used where only a player is available (no command source).
		 */
		public boolean test(ServerPlayer player) {
			return player.hasPermissions(fallbackLevel);
		}
	}
}
