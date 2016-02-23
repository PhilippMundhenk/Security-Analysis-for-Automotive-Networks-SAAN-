package sas.components.architecture.bus.builders;

import sas.components.architecture.bus.items.Bus;

public abstract class BusBuilder {
	public abstract Bus getNew();
	public abstract Bus getNew(String name);
}
