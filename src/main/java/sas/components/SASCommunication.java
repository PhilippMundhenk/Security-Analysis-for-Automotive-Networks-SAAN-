package sas.components;

import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Element;

public abstract class SASCommunication extends Communication implements Rules {
	public SASCommunication(Element parent) {
		super(parent);
	}
	
	public SASCommunication(String str) {
		super(str);
	}
	
	public abstract Integer getNumberOfStates();
	public abstract Integer getInitState();
	public abstract Integer getMinState();
}
