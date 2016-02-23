package sas.components.architecture.bus.items;

import java.util.HashMap;

import sas.Settings;
import net.sf.opendse.model.Element;
import net.sf.opendse.model.Resource;
import net.sf.opendse.optimization.constraints.SpecificationConstraints;

public class ThreeG extends Bus {

	public ThreeG(Element element) {
		super(element);
		super.setAttribute(Settings.UTILIZATION+SpecificationConstraints.CAPACITY_MAX, 0);
	}
	
	public ThreeG(String string) {
		super(string);
		super.setAttribute(Settings.UTILIZATION+SpecificationConstraints.CAPACITY_MAX, 0);
	}

	@Override
	public Integer getInitState() {
		return 1;
	}

	@Override
	public Integer getNumberOfStates() {
		return 1;
	}
	
	public boolean isActive() {
		return true;
	}

	public Integer checkInstantTransitions(Integer oldValue,
			HashMap<Resource, Integer> neighbours, HashMap<Resource, HashMap<Resource, Integer>> secondNeighbours) {
		/* 3G network does not have instantaneous transitions, is always exploitable */
		return 1;
	}

	public Integer getMinState() {
		return 1;
	}
}
