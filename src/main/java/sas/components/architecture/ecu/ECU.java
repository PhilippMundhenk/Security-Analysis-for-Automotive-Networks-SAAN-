package sas.components.architecture.ecu;

import java.util.HashMap;
import java.util.Map;

import net.sf.opendse.model.Element;
import net.sf.opendse.model.Resource;
import sas.Settings;
import sas.components.SASResource;
import sas.components.architecture.bus.items.Bus;

public class ECU extends SASResource {

	public ECU(Element element) {
		super(element);
	}

	public ECU(String name) {
		super(name);
	}

	@Override
	public Integer getInitState() {
		return 0;
	}

	@Override
	public Integer getNumberOfStates() {
		return 1+Settings.MAX_PARALLEL_ECU_EXPLOITS;
	}

	public boolean isActive() {
		return false;
	}
	
	public Integer checkInstantTransitions(Integer oldValue,
			HashMap<Resource, Integer> neighbours, HashMap<Resource, HashMap<Resource, Integer>> secondNeighbours) {

		Integer returnVal = 0;

//		System.out.println(this+" oldValue="+oldValue);
		
		/* ECUs follow incoming interfaces & the attached bus */
		for (Map.Entry<Resource, Integer> n : neighbours.entrySet()) {
			if (Interface_in.class.isAssignableFrom(n.getKey().getClass())) {
				for (Map.Entry<Resource, Integer> e : secondNeighbours.get(n.getKey()).entrySet()) {
					if(e.getKey() instanceof Bus)
					{
						if (n.getValue() > 0 && e.getValue() > 0) {
//							System.out.println(e.getKey() + "&&" + n.getKey() + "=>" + this);
							returnVal = 1;
						}
					}
				}
			}
		}
//		System.out.println(this+" return="+returnVal);
		return returnVal;
	}

	public Integer getMinState() {
		return 0;
	}
}
