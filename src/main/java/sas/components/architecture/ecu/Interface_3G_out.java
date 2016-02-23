package sas.components.architecture.ecu;

import java.util.HashMap;

import net.sf.opendse.model.Element;
import net.sf.opendse.model.Resource;

public class Interface_3G_out extends Interface {
	public Interface_3G_out(String string) {
		super(string);
	}

	public Interface_3G_out(Element element) {
		super(element);
	}

	public Interface_3G_out(String ecuID, String busID) {
		super(ecuID + "_i_out_" + busID);
	}

	@Override
	public Integer getInitState() {
		return 0;
	}

	@Override
	public Integer getNumberOfStates() {
		return 1;
	}

	public boolean isActive() {
		return false;
	}

	public Integer checkInstantTransitions(Integer oldValue, HashMap<Resource, Integer> neighbours, HashMap<Resource, HashMap<Resource, Integer>> secondNeighbours) {

		/*
		 * 3G ECUs do not affect the 3G bus. This interface is only introduced
		 * for dummy purposes
		 */
		return 0;
	}
	
	public Integer getMinState() {
		return 0;
	}
}
