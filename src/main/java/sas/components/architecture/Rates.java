package sas.components.architecture;

import java.util.HashSet;

import net.sf.opendse.model.Application;
import net.sf.opendse.model.Architecture;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Link;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;

public class Rates {
	private String name;
	private Double exploitability;
	private Double patchability;
	
	private static HashSet<Rates> ratesList;
	private static Architecture<Resource, Link> arch;
	private static Application<Task, Dependency> appl;
	
	public Rates(String name, Double exploitability, Double fixability)
	{
		this.name = name;
		this.exploitability = exploitability;
		this.patchability = fixability;
	}
	
	public Rates(String name, Integer exploitability, Integer fixability)
	{
		this.name = name;
		this.exploitability = new Double(exploitability);
		this.patchability = new Double(fixability);
	}
	
	public Rates(String name, Integer exploitability, Double fixability)
	{
		this.name = name;
		this.exploitability = new Double(exploitability);
		this.patchability = new Double(fixability);
	}
	
	public Rates(String name, Double exploitability, Integer fixability)
	{
		this.name = name;
		this.exploitability = new Double(exploitability);
		this.patchability = new Double(fixability);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Double getExploitability() {
		return exploitability;
	}
	public void setExploitability(Double exploitability) {
		this.exploitability = exploitability;
	}
	public Double getFixability() {
		return patchability;
	}
	public void setFixability(Double fixability) {
		this.patchability = fixability;
	}
	
	public static void initialize(Architecture<Resource, Link> architecture, Application<Task, Dependency> application)
	{
		arch = architecture;
		appl = application;
		ratesList = new HashSet<Rates>();
	}
	
	public static void addRates(Rates rates)
	{
		try
		{
			ratesList.add(rates);
			try
			{
				arch.getVertex(rates.getName()).setAttribute("explt", rates.getExploitability());
				arch.getVertex(rates.getName()).setAttribute("patch", rates.getFixability());
			}
			catch(NullPointerException e)
			{
				appl.getVertex(rates.getName()).setAttribute("explt", rates.getExploitability());
				appl.getVertex(rates.getName()).setAttribute("patch", rates.getFixability());
			}
		}
		catch(Exception e)
		{
			System.err.println("Error! Did you initialize?");
			e.printStackTrace();
		}
	}
}
