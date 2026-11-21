package moze_intel.projecte.platform.event;

/**
 * Fabric side stand-in for Forge's {@code Event}. ProjectE's public API events extend this class so third party mods keep the same shape (cancel them with
 * {@link #setCanceled(boolean)}, check {@link #isCanceled()}).
 */
public class Event {

	private boolean canceled;

	public boolean isCancelable() {
		return getClass().isAnnotationPresent(Cancelable.class);
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		if (canceled && !isCancelable()) {
			throw new UnsupportedOperationException(getClass().getName() + " is not cancelable");
		}
		this.canceled = canceled;
	}
}
