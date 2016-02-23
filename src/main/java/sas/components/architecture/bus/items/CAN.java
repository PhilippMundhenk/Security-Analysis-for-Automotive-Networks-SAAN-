package sas.components.architecture.bus.items;

import java.util.HashMap;
import java.util.Map;

import net.sf.opendse.model.Element;
import net.sf.opendse.model.Resource;
import net.sf.opendse.optimization.constraints.SpecificationConstraints;
import sas.Settings;
import sas.components.architecture.ecu.Interface_CAN_out;

public class CAN extends Bus {

	public CAN(Element element) {
		super(element);
		super.setAttribute(Settings.UTILIZATION+SpecificationConstraints.CAPACITY_MAX, Integer.MAX_VALUE);
	}

	public CAN(String string) {
		super(string);
		super.setAttribute(Settings.UTILIZATION+SpecificationConstraints.CAPACITY_MAX, Integer.MAX_VALUE);
	}

	@Override
	public Integer getInitState() {
		return 0;
	}

	@Override
	public Integer getNumberOfStates() {
		return 2;
	}

	public boolean isActive() {
		return false;
	}

	public Integer checkInstantTransitions(Integer oldValue,
			HashMap<Resource, Integer> neighbours, HashMap<Resource, HashMap<Resource, Integer>> secondNeighbours) {

		Integer returnVal = 0;

		for (Map.Entry<Resource, Integer> n : neighbours.entrySet()) {
			if (n.getKey() instanceof Interface_CAN_out) {
//				for (Map.Entry<Resource, Integer> e : secondNeighbours.get(n.getKey()).entrySet()) {
//					if(e.getKey() instanceof ECU)
//					{
//						if (n.getValue() > 0 && e.getValue() > 0) {
////							System.out.println(e.getKey() + "&&" + n.getKey() + "=>" + this);
//							returnVal = 1;
//						}
//					}
//				}
				if (n.getValue() > 0) {
//					System.out.println(n.getKey() + "=> " + this);
					returnVal = 1;
				}
			}
		}
		return returnVal;
	}

	public Integer getMinState() {
		return 0;
	}
}
