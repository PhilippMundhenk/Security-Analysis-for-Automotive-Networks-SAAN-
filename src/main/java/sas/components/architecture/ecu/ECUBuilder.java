package sas.components.architecture.ecu;

import net.sf.opendse.model.Architecture;
import net.sf.opendse.model.Link;
import net.sf.opendse.model.Resource;
import sas.components.AutoLink;
import sas.components.architecture.bus.items.Bus;
import sas.components.architecture.bus.items.CAN;
import sas.components.architecture.bus.items.FlexRay;
import sas.components.architecture.bus.items.ThreeG;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import edu.uci.ics.jung.graph.util.EdgeType;

@Singleton
public class ECUBuilder {
	private Architecture<Resource, Link> arch;
	private int counter=1;
	
	@Inject
	private ECUBuilder(Architecture<Resource, Link> architecture) {
		arch = architecture;
	}
	
	public ECU getNewECU(Bus b)
	{
		ECU e = new ECU("ecu"+counter++);
		arch.addVertex(e);
		createDependencies(e, b);
		return e;
	}
	
	public ECU getNewECU(String name, Bus b)
	{
		ECU e = new ECU(name);
		arch.addVertex(e);
		createDependencies(e, b);
		return e;
	}
	
	private void createDependencies(ECU e, Bus b)
	{
		if(b instanceof FlexRay)
		{
			Interface i_out = new Interface_FlexRay_out(e.getId(), b.getId());
			Interface i_in = new Interface_FlexRay_in(e.getId(), b.getId());
			Interface i_bg = new Interface_FlexRay_BusGuardian(e.getId(), b.getId());
			
			arch.addEdge(AutoLink.create(), e, i_out, EdgeType.DIRECTED);
			arch.addEdge(AutoLink.create(), i_out, i_bg, EdgeType.DIRECTED);
			arch.addEdge(AutoLink.create(), i_bg, b, EdgeType.DIRECTED);
			
			arch.addEdge(AutoLink.create(), i_in, e, EdgeType.DIRECTED);
			arch.addEdge(AutoLink.create(), b, i_in, EdgeType.DIRECTED);
		}
		else if(b instanceof CAN)
		{
			Interface i_in;
			Interface i_out;
			i_in = new Interface_CAN_in(e.getId(), b.getId());
			i_out = new Interface_CAN_out(e.getId(), b.getId());

			arch.addEdge(AutoLink.create(), i_in, e, EdgeType.DIRECTED);
			arch.addEdge(AutoLink.create(), b, i_in, EdgeType.DIRECTED);
			
			arch.addEdge(AutoLink.create(), e, i_out, EdgeType.DIRECTED);
			arch.addEdge(AutoLink.create(), i_out, b, EdgeType.DIRECTED);
		}
		else if(b instanceof ThreeG)
		{
			Interface i_in = new Interface_3G_in(e.getId(), b.getId());
			Interface i_out = new Interface_3G_out(e.getId(), b.getId());
			
			arch.addEdge(AutoLink.create(), i_in, e, EdgeType.DIRECTED);
			arch.addEdge(AutoLink.create(), b, i_in, EdgeType.DIRECTED);
			
			arch.addEdge(AutoLink.create(), e, i_out, EdgeType.DIRECTED);
			arch.addEdge(AutoLink.create(), i_out, b, EdgeType.DIRECTED);
		}
		else
		{
			return;
		}
	}

	public void addECUToBus(ECU e, Bus b) {
		createDependencies(e, b);
	}
}
