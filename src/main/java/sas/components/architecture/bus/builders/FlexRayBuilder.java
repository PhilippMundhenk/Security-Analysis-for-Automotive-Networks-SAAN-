package sas.components.architecture.bus.builders;

import net.sf.opendse.model.Architecture;
import net.sf.opendse.model.Link;
import net.sf.opendse.model.Resource;
import sas.components.architecture.bus.items.FlexRay;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class FlexRayBuilder extends BusBuilder {
	private Architecture<Resource, Link> arch;
	private int counter=1;
	
	@Inject
	private FlexRayBuilder(Architecture<Resource, Link> architecture) {
		arch = architecture;
	}
	
	public FlexRay getNew()
	{
		FlexRay b = new FlexRay("fr"+counter++);
		arch.addVertex(b);
		return b;
	}
	
	public FlexRay getNew(String name)
	{
		FlexRay b = new FlexRay(name);
		arch.addVertex(b);
		return b;
	}
}
