package sas.components.architecture.ecu;

import java.util.HashMap;
import java.util.Map;

import net.sf.opendse.model.Element;
import net.sf.opendse.model.Resource;
import sas.Settings;

public class Interface_CAN_out extends Interface implements Interface_out {
	public Interface_CAN_out(String string) {
		super(string);
	}

	public Interface_CAN_out(Element element) {
		super(element);
	}

	public Interface_CAN_out(String ecuID, String busID) {
		super(ecuID+"_i_out_"+busID);
	}

	@Override
	public Integer getInitState() {
		return 0;
	}

	@Override
	public Integer getNumberOfStates() {
		return 1+Settings.MAX_PARALLEL_INTERFACE_CAN_EXPLOITS;
	}

	public boolean isActive() {
		return false;
	}

	public Integer checkInstantTransitions(Integer oldValue,
			HashMap<Resource, Integer> neighbours, HashMap<Resource, HashMap<Resource, Integer>> secondNeighbours) {

		Integer returnVal = 0;

//		System.out.println(this+" oldValue="+oldValue);
		
		/* outgoing interfaces follow the ECU */
		for (Map.Entry<Resource, Integer> n : neighbours.entrySet()) {
			if (n.getKey() instanceof ECU) {
				if (n.getValue() > 0) {
//					System.out.println(n.getKey() + "=>" + this);
					returnVal = 1;
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
