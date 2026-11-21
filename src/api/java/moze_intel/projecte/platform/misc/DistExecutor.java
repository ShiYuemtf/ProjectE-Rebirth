package moze_intel.projecte.platform.misc;

import java.util.function.Supplier;

/**
 * Fabric side stand-in for Forge's {@code DistExecutor}. Fabric loads one jar for both sides, so "run when on" means a plain environment check plus a lazy
 * supplier; class loading of client-only types is guarded by the caller's own lambda.
 */
public class DistExecutor {

	public static void runWhenOn(Dist dist, Supplier<Runnable> toRun) {
		if (FMLEnvironment.dist == dist) {
			toRun.get().run();
		}
	}

	public static void unsafeRunWhenOn(Dist dist, Supplier<Runnable> toRun) {
		runWhenOn(dist, toRun);
	}

	public static void safeRunWhenOn(Dist dist, Supplier<Runnable> toRun) {
		runWhenOn(dist, toRun);
	}

	public static <T> T callWhenOn(Dist dist, Supplier<Supplier<T>> toCall) {
		return FMLEnvironment.dist == dist ? toCall.get().get() : null;
	}

	public static <T> T unsafeCallWhenOn(Dist dist, Supplier<Supplier<T>> toCall) {
		return callWhenOn(dist, toCall);
	}

	/**
	 * Forge's "run for the current dist, taking the supplier matching it". Both suppliers are passed lazily so the client body is only resolved on the client.
	 */
	public static <T> T unsafeRunForDist(Supplier<Supplier<T>> clientTarget, Supplier<Supplier<T>> serverTarget) {
		return FMLEnvironment.dist == Dist.CLIENT ? clientTarget.get().get() : serverTarget.get().get();
	}

	public static <T> T safeRunForDist(Supplier<Supplier<T>> clientTarget, Supplier<Supplier<T>> serverTarget) {
		return unsafeRunForDist(clientTarget, serverTarget);
	}

	private DistExecutor() {
	}
}
