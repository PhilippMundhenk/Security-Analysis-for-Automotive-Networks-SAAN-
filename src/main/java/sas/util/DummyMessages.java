package sas.util;

import java.util.ArrayList;
import java.util.HashSet;

import net.sf.opendse.model.Application;
import net.sf.opendse.model.Architecture;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Link;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Mappings;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;
import sas.components.AutoDependency;
import sas.components.architecture.bus.items.Bus;
import sas.components.architecture.ecu.ECU;
import sas.components.architecture.ecu.Interface_3G_out;
import sas.components.architecture.ecu.Interface_in;
import sas.components.architecture.ecu.Interface_out;

public class DummyMessages {

	public static void createDummyMessages(HashSet<ECU> mandatoryECUs, Architecture<Resource, Link> arch, Application<Task, Dependency> application, Mappings<Task, Resource> mappings) {
		System.out.println("creating dummy messages...");
		for (ECU ecu : mandatoryECUs) {
			ArrayList<Resource> interfaces_in = new ArrayList<Resource>();
			ArrayList<Resource> interfaces_out = new ArrayList<Resource>();
			for (Resource r : arch.getNeighbors(ecu)) {
				if (Interface_in.class.isAssignableFrom(r.getClass())) {
					interfaces_in.add(r);
				} else if (Interface_out.class.isAssignableFrom(r.getClass()) || r instanceof Interface_3G_out) {
					interfaces_out.add(r);
				}
			}

			ArrayList<Resource> buses = new ArrayList<Resource>();
			for (Resource r : interfaces_out) {
				for (Resource resource : arch.getNeighbors(r)) {
					if (resource instanceof Bus) {
						buses.add(resource);
					}
				}
			}

			for (Resource r : buses) {
				Task d1 = new Task("dummy_" + ecu.getId() + "_" + r.getId() + "_1");
				Task d2 = new Task("dummy_" + ecu.getId() + "_" + r.getId() + "_2");
				Task d3 = new Task("dummy_" + ecu.getId() + "_" + r.getId() + "_3");
				application.addVertex(d1);
				application.addVertex(d2);
				application.addVertex(d3);

				mappings.add(new Mapping<Task, Resource>("dummyMap_" + ecu.getId() + "_" + r.getId() + "_1", d1, ecu));
				mappings.add(new Mapping<Task, Resource>("dummyMap_" + ecu.getId() + "_" + r.getId() + "_2", d2, ecu));
				mappings.add(new Mapping<Task, Resource>("dummyMap_" + ecu.getId() + "_" + r.getId() + "_3", d3, r));

				for (Resource i : interfaces_out) {
					for (Resource resource : arch.getNeighbors(r)) {
						if (i.equals(resource)) {
							Communication md1 = new Communication("dummyMsg_" + ecu.getId() + "_" + r.getId() + "_out");
							application.addVertex(md1);

							application.addEdge(AutoDependency.create(), d1, md1);
							application.addEdge(AutoDependency.create(), md1, d3);
						}
					}
				}

				for (Resource i : interfaces_in) {
					for (Resource resource : arch.getNeighbors(r)) {
						if (i.equals(resource)) {
							Communication md2 = new Communication("dummyMsg_" + ecu.getId() + "_" + r.getId() + "_in");
							application.addVertex(md2);

							application.addEdge(AutoDependency.create(), d3, md2);
							application.addEdge(AutoDependency.create(), md2, d2);
						}
					}
				}
			}
		}
	}

}
