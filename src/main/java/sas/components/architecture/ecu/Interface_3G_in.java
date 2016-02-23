package sas.components.architecture.ecu;

import java.util.HashMap;

import sas.Settings;
import net.sf.opendse.model.Element;
import net.sf.opendse.model.Resource;

public class Interface_3G_in extends Interface implements Interface_in {
	public Interface_3G_in(String string) {
		super(string);
	}

	public Interface_3G_in(Element element) {
		super(element);
	}

	public Interface_3G_in(String ecuID, String busID) {
		super(ecuID+"_i_in_"+busID);
	}

	@Override
	public Integer getInitState() {
		return 0;
	}

	@Override
	public Integer getNumberOfStates() {
		return 1+Settings.MAX_PARALLEL_INTERFACE_3G_EXPLOITS;
	}

	public boolean isActive() {
		return true;
	}

	public Integer checkInstantTransitions(Integer oldValue,
			HashMap<Resource, Integer> neighbours, HashMap<Resource, HashMap<Resource, Integer>> secondNeighbours) {

		/* incoming interfaces do not have instantaneous transitions */
		return oldValue;
	}

	public Integer getMinState() {
		return 0;
	}
}
