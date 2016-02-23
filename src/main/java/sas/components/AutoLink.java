package sas.components;

import net.sf.opendse.model.Element;
import net.sf.opendse.model.Link;

public class AutoLink extends Link {

	private static int counter=0;
	
	public AutoLink(Element element) {
		super(element);
	}
	
	public AutoLink(String string) {
		super(string);
	}
	
	public static AutoLink create()
	{
		return new AutoLink("l"+counter++);
	}
}
