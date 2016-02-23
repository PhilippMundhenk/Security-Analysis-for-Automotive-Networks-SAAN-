package sas.components.architecture.ecu;

import net.sf.opendse.model.Element;
import net.sf.opendse.model.Node;
import sas.components.SASResource;

public abstract class Interface extends SASResource {
	public Interface(Element element) {
		super(element);
	}

	public Interface(String string) {
		super(string);
	}
	
	public Integer checkInstantTransition(Class<? extends Node> neighbourType, Integer oldValue, Integer neighbourChange) {
		if(neighbourType==ECU.class)
		{
			if(neighbourChange>0)
			{
				if(oldValue+1 >= getNumberOfStates())
				{
					return oldValue;
				}
				else
				{
					return oldValue+1;
				}
			}
			else if(neighbourChange<0)
			{
				if(oldValue == 0)
				{
					return 0;
				}
				else
				{
					return oldValue-1;
				}
			}
			else
			{
				return -1;
			}
		}
		else
		{
			return -1;
		}
	}
}
