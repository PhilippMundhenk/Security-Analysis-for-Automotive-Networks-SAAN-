package sas.components.architecture.bus.builders;

import net.sf.opendse.model.Architecture;
import net.sf.opendse.model.Link;
import net.sf.opendse.model.Resource;
import sas.components.architecture.bus.items.CAN;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class CANBuilder extends BusBuilder {
	private Architecture<Resource, Link> arch;
	private int counter=1;
	
	@Inject
	private CANBuilder(Architecture<Resource, Link> architecture) {
		arch = architecture;
	}
	
	public CAN getNew()
	{
		CAN b = new CAN("can"+counter++);
		arch.addVertex(b);
		return b;
	}
	
	public CAN getNew(String name)
	{
		CAN b = new CAN(name);
		arch.addVertex(b);
		return b;
	}
}
