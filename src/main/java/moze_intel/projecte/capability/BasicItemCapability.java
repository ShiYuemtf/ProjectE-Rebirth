package moze_intel.projecte.capability;

import moze_intel.projecte.platform.util.LazyOptional;

public abstract class BasicItemCapability<TYPE> extends ItemCapability<TYPE> {

	private final LazyOptional<TYPE> capability = LazyOptional.of(() -> (TYPE) this);

	@Override
	public LazyOptional<TYPE> getLazyCapability() {
		return capability;
	}
}