package sas.components;

import java.util.HashMap;

import net.sf.opendse.model.Resource;

public interface Rules {

//	/**
//	 * Checks if object changes with neighbour change of object of given type.
//	 *
//	 * @param  neighbourType Type of the nieghbour object
//	 * @return      -1: no change
//	 * 				else: new value
//	 */
//	public Integer checkInstantTransition(Class<? extends Node> neighbourType, Integer oldValue, Integer neighbourChange);

	public Integer checkInstantTransitions(Integer oldValue, HashMap<Resource, Integer> neighbours, HashMap<Resource, HashMap<Resource, Integer>> secondNeighbours);
	public boolean isActive();
}
