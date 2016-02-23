package sas.components.architecture.ecu;

import java.util.HashMap;
import java.util.Map;

import net.sf.opendse.model.Element;
import net.sf.opendse.model.Resource;
import sas.Settings;
import sas.components.architecture.bus.items.FlexRay;

public class Interface_FlexRay_BusGuardian extends Interface{
	public Interface_FlexRay_BusGuardian(String string) {
		super(string);
	}

	public Interface_FlexRay_BusGuardian(Element element) {
		super(element);
	}

	public Interface_FlexRay_BusGuardian(String ecuID, String busID) {
		super(ecuID+"_i_"+busID+"_BG");
	}

	@Override
	public Integer getInitState() {
		return 0;
	}

	@Override
	public Integer getNumberOfStates() {
		return 1+Settings.MAX_PARALLEL_INTERFACE_FR_BG_EXPLOITS;
	}

	public boolean isActive() {
		return true;
	}

	public Integer checkInstantTransitions(Integer oldValue,
			HashMap<Resource, Integer> neighbours, HashMap<Resource, HashMap<Resource, Integer>> secondNeighbours) {

		Integer returnVal = 0;

		for (Map.Entry<Resource, Integer> n : neighbours.entrySet()) {
			if (n.getKey() instanceof FlexRay) {
				if (n.getValue() > 0) {
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
