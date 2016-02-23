package sas.ModelChecker;

import java.util.List;
import java.util.Set;

import net.sf.opendse.model.Resource;
import sas.components.Rules;
import sas.components.SASCommunication;

public class ObjectiveGenerator {

	public Double[] getSingleObjectRho(Set<List<Integer>> states, List<Rules> transitionObjects, String objID, List<Integer> conditions) throws Exception {
		Double[] rho = new Double[states.size()];
		
		Integer cnt = -1;
		Integer objIndex = -1;
		for (Rules r : transitionObjects) {
			cnt++;
			if(r instanceof Resource)
			{
				if(((Resource) r).getId().equals(objID))
				{
					objIndex = cnt;
					break;
				}
			}
			else if(r instanceof SASCommunication)
			{
				if(((SASCommunication) r).getId().equals(objID))
				{
					objIndex = cnt;
					break;
				}
			}
		}
		
		if(objIndex == -1)
		{
			throw new Exception("Object requested for evaluation does not exist");
		}
		
		cnt = -1;
		for (List<Integer> l : states) {
			cnt++;
			Integer stateVal = l.get(objIndex);
			for (Integer integer : conditions) {
				if(stateVal == integer)
				{
					rho[cnt] = 1.0;
				}
				else
				{
					rho[cnt] = 0.0;
				}
			}
		}
		
		return rho;
	}
	
	//TODO: implement combinations of states (e.g. msg1 or msg2 exploited)
}
