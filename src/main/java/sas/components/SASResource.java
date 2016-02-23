package sas.components;

import net.sf.opendse.model.Element;
import net.sf.opendse.model.Resource;

public abstract class SASResource extends Resource implements Rules {
	public SASResource(Element parent) {
		super(parent);
	}
	
	public SASResource(String str) {
		super(str);
	}
	
	public abstract Integer getInitState();
	public abstract Integer getNumberOfStates();
	public abstract Integer getMinState();
}
