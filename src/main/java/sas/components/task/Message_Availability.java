package sas.components.task;

import java.util.HashMap;
import java.util.Map;

import net.sf.opendse.model.Application;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Element;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;
import sas.Settings;
import sas.components.AutoDependency;
import sas.components.SASCommunication;

public class Message_Availability extends SASCommunication {

	private Boolean hasSender = false;
	private static Application<Task, Dependency> app;

	public Message_Availability(Element element) {
		super(element);
		super.setAttribute(Settings.UTILIZATION, 1);
	}

	public Message_Availability(String string) {
		super(string);
		super.setAttribute(Settings.UTILIZATION, 1);
	}

	public static Message_Availability create(Application<Task, Dependency> application, String name) {
		app = application;
		Message_Availability m = new Message_Availability(name);
		app.addVertex(m);
		return m;
	}

	public Sender createSender(String name) {
		if (!hasSender) {
			Sender s = new Sender(name);
			app.addVertex(s);
			app.addEdge(AutoDependency.create(), s, this);
			hasSender = true;
			return s;
		} else {
			return null;
		}
	}

	public Receiver addReceiver(String name) {
		Receiver r = new Receiver(name);
		app.addVertex(r);
		app.addEdge(AutoDependency.create(), this, r);
		return r;
	}

	@Override
	public Integer getNumberOfStates() {
		return 1 + Settings.MAX_PARALLEL_MSG_EXPLOITS;
	}

	@Override
	public Integer getInitState() {
		return 0;
	}

	public boolean isActive() {
		return false;
	}

	public Integer checkInstantTransitions(Integer oldValue, HashMap<Resource, Integer> neighbours, HashMap<Resource, HashMap<Resource, Integer>> secondNeighbours) {

		Integer returnVal = 0;
		Boolean allZero = true;
//		System.out.println();
//		System.out.println(this+":");
//		System.out.println("------------------------");
//		System.out.println("oldValue: "+oldValue);
		for (Map.Entry<Resource, Integer> n : neighbours.entrySet()) {
			if (n.getKey() instanceof Resource) {
//				System.out.println(n);
				if (n.getValue() > 0) {
//					System.out.println(n.getKey() + "=>" + this);
					if (oldValue >= 1) {
						returnVal = oldValue;
					} else if (oldValue == 0) {
						returnVal = 1;
					}
					allZero = false;
				}
			}
		}

		if (allZero) {
			returnVal = 0;
		}

//		System.out.println("returnVal="+returnVal);
		return returnVal;
	}

	public Integer getMinState() {
		return 0;
	}
}
