package sas.components.architecture.bus.builders;

import net.sf.opendse.model.Architecture;
import net.sf.opendse.model.Link;
import net.sf.opendse.model.Resource;
import sas.Settings;
import sas.components.architecture.bus.items.ThreeG;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ThreeGBuilder extends BusBuilder {
	private Architecture<Resource, Link> arch;
	private int counter=1;
	
	@Inject
	private ThreeGBuilder(Architecture<Resource, Link> architecture) {
		arch = architecture;
	}
	
	public ThreeG getNew()
	{
		ThreeG b = new ThreeG("3g"+counter++);
		b.setAttribute("explt", Settings.MAX_EXPLOITABILITY_RATE);
		b.setAttribute("patch", 0);
		arch.addVertex(b);
		return b;
	}
	
	public ThreeG getNew(String name)
	{
		ThreeG b = new ThreeG(name);
		b.setAttribute("explt", Settings.MAX_EXPLOITABILITY_RATE);
		b.setAttribute("patch", 0);
		arch.addVertex(b);
		return b;
	}
}
