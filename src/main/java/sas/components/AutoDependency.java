package sas.components;

import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Element;

public class AutoDependency extends Dependency {

	private static int counter=0;
	
	public AutoDependency(Element element) {
		super(element);
	}
	
	public AutoDependency(String string) {
		super(string);
	}
	
	public static AutoDependency create()
	{
		return new AutoDependency("l"+counter++);
	}
}
