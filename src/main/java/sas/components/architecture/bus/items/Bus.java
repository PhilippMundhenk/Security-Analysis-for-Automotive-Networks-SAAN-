package sas.components.architecture.bus.items;

import net.sf.opendse.model.Element;
import sas.components.SASResource;

public abstract class Bus extends SASResource{
	
	public Bus(Element element) {
		super(element);
	}
	
	public Bus(String string) {
		super(string);
	}	
}
