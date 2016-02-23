package sas.components.task;

import net.sf.opendse.model.Element;
import net.sf.opendse.model.Task;

public class Sender extends Task {
	public Sender(Element element) {
		super(element);
	}
	
	public Sender(String string) {
		super(string);
	}
}
