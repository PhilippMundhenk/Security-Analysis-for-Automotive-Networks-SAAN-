package sas.components.task;

import net.sf.opendse.model.Element;
import net.sf.opendse.model.Task;

public class Receiver extends Task {
	public Receiver(Element element) {
		super(element);
	}
	
	public Receiver(String string) {
		super(string);
	}
}
