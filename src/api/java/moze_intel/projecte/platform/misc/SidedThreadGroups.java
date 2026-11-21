package moze_intel.projecte.platform.misc;

/**
 * Fabric side stand-in for Forge's {@code SidedThreadGroups}. ProjectE only uses it to tell "are we on the logical server thread" apart from the client one.
 */
public class SidedThreadGroups {

	public static final ThreadGroup SERVER = new ThreadGroup("Server");
	public static final ThreadGroup CLIENT = new ThreadGroup("Client");

	public static boolean isServerThread() {
		Thread current = Thread.currentThread();
		return current.getName().startsWith("Server thread");
	}

	public static boolean isClientThread() {
		return !isServerThread();
	}

	private SidedThreadGroups() {
	}
}
