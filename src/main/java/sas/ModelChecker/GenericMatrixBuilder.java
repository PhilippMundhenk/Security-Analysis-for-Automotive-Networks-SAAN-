package sas.ModelChecker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Specification;
import net.sf.opendse.model.Task;
import sas.components.Rules;
import sas.components.SASCommunication;
import sas.components.SASResource;
import sas.components.architecture.bus.items.Bus;

import com.google.common.collect.Sets;

public class GenericMatrixBuilder {

	Specification implementation;
	ArrayList<Rules> transitionObjects;
	Set<List<Integer>> finalStates;
	Double[][] Q;

	Boolean debugOutput = true;

	public GenericMatrixBuilder(Specification implementation) {
		this.implementation = implementation;
	}

	public Double[][] generateStateSpaceDouble() {
		Double[][] Q_old = generateStateSpace();

		Q = new Double[Q_old.length][Q_old.length];

		for (int i = 0; i < Q.length; i++) {
			for (int j = 0; j < Q[i].length; j++) {
				Q[i][j] = new Double(Q_old[i][j]);
			}
		}

		return Q;
	}

	public Double[][] generateStateSpace() {
		ArrayList<HashSet<Integer>> possibleStates = new ArrayList<HashSet<Integer>>();
		transitionObjects = new ArrayList<Rules>();

		if (debugOutput) {
			System.out.println("routings:");
			for (Task t : implementation.getRoutings().getTasks()) {
				System.out.println("message: " + t);
				System.out.println(implementation.getRoutings().get(t));
			}
		}

		/* generate initial state */
		for (Iterator<Resource> i = implementation.getArchitecture().getVertices().iterator(); i.hasNext();) {
			SASResource r = (SASResource) i.next();

			HashSet<Integer> set = new HashSet<Integer>();
			for (int j = r.getMinState(); j < r.getNumberOfStates(); j++) {
				set.add(j);
			}
			if (r.getMinState() == r.getNumberOfStates()) {
				set.add(r.getInitState());
			}

			possibleStates.add(set);
			transitionObjects.add(r);
		}

		for (Iterator<Task> i = implementation.getApplication().getVertices().iterator(); i.hasNext();) {
			Task t = i.next();

			if (t instanceof SASCommunication) {
				SASCommunication msg = (SASCommunication) t;

				HashSet<Integer> set = new HashSet<Integer>();
				for (int j = msg.getMinState(); j < msg.getNumberOfStates(); j++) {
					set.add(j);
				}
				if (msg.getMinState() == msg.getNumberOfStates()) {
					set.add(msg.getInitState());
				}

				possibleStates.add(set);
				transitionObjects.add(msg);
			}
		}

		if (debugOutput) {
			System.out.print("transitions: {");
			for (Rules r : transitionObjects) {
				System.out.print(r + ",");
			}
			System.out.println("}");
		}

//		if (debugOutput) {
//			System.out.println("possibleStates:");
//			for (Set<Integer> set : possibleStates) {
//				System.out.print("{");
//				for (Integer i : set) {
//					System.out.print(i + ",");
//				}
//				System.out.println("}");
//			}
//		}

		finalStates = Sets.cartesianProduct(possibleStates);

//		if (debugOutput) {
//			Integer cnt = 0;
//			System.out.println();
//			System.out.println("list of states:");
//			for (List<Integer> l : finalStates) {
//				System.out.print(cnt + ": {");
//				for (Integer integer : l) {
//					System.out.print(integer + ",");
//				}
//				System.out.println("}");
//				cnt++;
//			}
//		}

		if(debugOutput)
		{
			System.out.println("number of states: "+finalStates.size());
		}
		
		Q = new Double[finalStates.size()][finalStates.size()];

		for (int i = 0; i < Q.length; i++) {
			for (int j = 0; j < Q[i].length; j++) {
				Q[i][j] = 0.0;
			}
		}

		int numberOfTransitions = 0;

		Integer outer = -1;
		Integer inner = -1;
		for (List<Integer> l1 : finalStates) {
			outer++;
			for (List<Integer> l2 : finalStates) {
				inner++;
				Integer exploitableTransitions = 0;
				Rules t = null;
				for (int i = 0; i < l1.size(); i++) {
					if (l1.get(i) != l2.get(i)) {
						exploitableTransitions++;
						if (l1.get(i) + 1 == l2.get(i)) {
							t = transitionObjects.get(i);
						}
					}
				}

				if (exploitableTransitions == 1 && t!=null) {
					Rules r = t;

					this.implementation.getArchitecture().getNeighbors((Resource) t);

					if (debugOutput) {
						System.out.println("single Transition found:");
						System.out.print("from: {");
						for (Integer x : l1) {
							System.out.print(x + ",");
						}
						System.out.println("}");
						System.out.print("to  : {");
						for (Integer x : l2) {
							System.out.print(x + ",");
						}
						System.out.println("}");
						System.out.println("with: " + r);
					}

					if (r instanceof Resource) {
						/* outgoing interfaces shouldn't have any rates */
						if (((Resource) r).getAttribute("patch") == null) {
							throw new NullPointerException("Patch rate is not set for " + r + ". Did you forget to set it?");
						} else {

							if (r.isActive() || 0 == r.checkInstantTransitions(new Integer(1), getNeighboursResource((Resource) r, l2), getSecondNeighboursResource((Resource) r, l2))) {
								/* Resource changes in this transition */
								Q[inner][outer] = ((Resource) r).getAttribute("patch");
								numberOfTransitions++;
								System.out.println("patch rate= "+Q[inner][outer]);
							}
						}
						if (((Resource) r).getAttribute("explt") == null) {
							throw new NullPointerException("Exploitation rate is not set for " + r + ". Did you forget to set it?");
						} else {
							if (r.isActive() || 1 == r.checkInstantTransitions(new Integer(0), getNeighboursResource((Resource) r, l1), getSecondNeighboursResource((Resource) r, l1))) {
								Q[outer][inner] = ((Resource) r).getAttribute("explt");
								numberOfTransitions++;
								System.out.println("explt rate= "+Q[outer][inner]);
							}
						}
					} else if (r instanceof SASCommunication) {
						// System.out.println("r is SASCommunication");
						if (((SASCommunication) r).getAttribute("patch") == null) {
							throw new NullPointerException("Patch rate is not set for " + r + ". Did you forget to set it?");
						} else {
							if (r.isActive() || 0 == r.checkInstantTransitions(new Integer(1), getNeighboursMessage((SASCommunication) r, l2), null)) {
								Q[inner][outer] = ((SASCommunication) r).getAttribute("patch");
								numberOfTransitions++;
								System.out.println("patch rate= "+Q[inner][outer]);
							}
						}
						if (((SASCommunication) r).getAttribute("explt") == null) {
							throw new NullPointerException("Exploitation rate is not set for " + r + ". Did you forget to set it?");
						} else {
							if (r.isActive() || 1 == r.checkInstantTransitions(new Integer(0), getNeighboursMessage((SASCommunication) r, l1), null)) {
								Q[outer][inner] = ((SASCommunication) r).getAttribute("explt");
								numberOfTransitions++;
								System.out.println("explt rate= "+Q[outer][inner]);
							}
						}
					}
				}
			}
			inner = -1;
		}

		System.out.println("numberOfTransitions: " + numberOfTransitions);

		for (int i = 0; i < Q.length; i++) {
			Double sum = 0.0;
			for (int j = 0; j < Q[i].length; j++) {

				sum += Q[i][j];
			}
			Q[i][i] = (-1) * sum;
		}

		if (debugOutput) {
			System.out.println();
			System.out.print("Q:\t");
			for (int i = 0; i < Q.length; i++) {
				System.out.print(i + "\t");
			}
			System.out.println();
			System.out.println("-----------------------------------------------------------------------------------");
			for (int i = 0; i < Q.length; i++) {
				System.out.print(i + " |\t");
				for (int j = 0; j < Q[i].length; j++) {
					System.out.print(Q[i][j] + "\t");
				}
				System.out.println();
			}
		}
		return Q;
	}

	public List<Rules> getTransitionObjects() {
		return transitionObjects;
	}

	public Set<List<Integer>> getStates() {
		return finalStates;
	}

	private HashMap<Resource, HashMap<Resource, Integer>> getSecondNeighboursResource(Resource node, List<Integer> values) {
		HashMap<Resource, Integer> neighbourVals = new HashMap<Resource, Integer>();
		HashMap<Resource, HashMap<Resource, Integer>> secondNeighbours = new HashMap<Resource, HashMap<Resource, Integer>>();
		for (Resource n : implementation.getArchitecture().getNeighbors(node)) {
			neighbourVals.put(n, values.get(transitionObjects.indexOf(n)));
			HashMap<Resource, Integer> secondNeighbourVals = new HashMap<Resource, Integer>();
			for (Resource second : implementation.getArchitecture().getNeighbors(n)) {
				secondNeighbourVals.put(second, values.get(transitionObjects.indexOf(second)));
			}
			secondNeighbours.put(n, secondNeighbourVals);
		}
		return secondNeighbours;
	}

	private HashMap<Resource, Integer> getNeighboursResource(Resource node, List<Integer> values) {
		HashMap<Resource, Integer> neighbourVals = new HashMap<Resource, Integer>();
		for (Resource n : implementation.getArchitecture().getNeighbors(node)) {
			neighbourVals.put(n, values.get(transitionObjects.indexOf(n)));
		}

		return neighbourVals;
	}

	private HashMap<Resource, Integer> getNeighboursMessage(SASCommunication node, List<Integer> returnVal) {
		HashMap<Resource, Integer> neighbourVals = new HashMap<Resource, Integer>();

		Resource random = implementation.getRoutings().get(node).getVertices().iterator().next();
		/* find sender */
		while (implementation.getRoutings().get(node).getPredecessors(random).iterator().hasNext()) {
			random = implementation.getRoutings().get(node).getPredecessors(random).iterator().next();
		}
		neighbourVals.put(random, returnVal.get(transitionObjects.indexOf(random)));
		/* find receiver */
		while (implementation.getRoutings().get(node).getSuccessors(random).iterator().hasNext()) {
			random = implementation.getRoutings().get(node).getSuccessors(random).iterator().next();
		}
		neighbourVals.put(random, returnVal.get(transitionObjects.indexOf(random)));

		/* also consider all buses on the way */
		for (Resource n : implementation.getRoutings().get(node).getVertices()) {
			if (n instanceof Bus) {
				neighbourVals.put(n, returnVal.get(transitionObjects.indexOf(n)));
			}
		}

		return neighbourVals;
	}

	private String newlineChar = "\r\n";

	public void exportPRISMFiles(File model, File transitions) {
		if (model != null) {
			try {
				FileWriter fw = new FileWriter(model.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write("(");
				String divider = "";
				for (Rules r : transitionObjects) {
					bw.write(divider + r);
					divider = ",";
				}
				bw.write(")" + newlineChar);

				Integer cnt = 0;
				for (List<Integer> l : finalStates) {
					bw.write(cnt + ":(");
					divider = "";
					for (Integer integer : l) {
						bw.write(divider + integer);
						divider = ",";
					}
					bw.write(")" + newlineChar);
					cnt++;
				}

				bw.close();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		if (transitions != null) {
			try {
				FileWriter fw = new FileWriter(transitions.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);

				bw.write(finalStates.size() + " ");
				Integer cnt = 0;
				for (int i = 0; i < Q.length; i++) {
					for (int j = 0; j < Q[i].length; j++) {
						if (Q[i][j] > 0 && i != j) {
							cnt++;
						}
					}
				}
				bw.write(cnt + newlineChar);

				for (int i = 0; i < Q.length; i++) {
					for (int j = 0; j < Q[i].length; j++) {
						if (Q[i][j] > 0 && i != j) {
							bw.write(i + " " + j + " " + Q[i][j] + newlineChar);
						}
					}
				}

				bw.close();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void exportRewards(File file, Double[] rho) {
		if (file != null && rho != null) {
			try {
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);

				bw.write(finalStates.size() + " ");
				Integer cnt = 0;
				for (int i = 0; i < rho.length; i++) {
					if (rho[i] > 0) {
						cnt++;
					}
				}
				bw.write(cnt + newlineChar);

				for (int i = 0; i < rho.length; i++) {
					if (rho[i] > 0) {
						bw.write(i + " " + 1 + newlineChar);
					}
				}

				bw.close();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void toPDF(File file) {
		Set<List<Integer>> states = getStates();
		ArrayList<String> stateStrings = new ArrayList<String>();
		for (List<Integer> s : states) {
			String str = new String("\"");
			String c = "";
			for (Integer integer : s) {
				str = str + c + integer;
				c = ",";
			}
			str += "\"";
			stateStrings.add(str);
		}

		List<Rules> t = getTransitionObjects();
		String label = new String();
		String c = "";
		for (Rules rules : t) {
			label = label + c + rules;
			c = ",";
		}

		Boolean graphOutput = true;
		if (graphOutput) {
			try {
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write("digraph {");
				bw.write("label=\"" + label + "\";");
				for (int i = 0; i < Q.length; i++) {
					for (int j = 0; j < Q[i].length; j++) {
						if (Q[i][j] > 0) {
							bw.write(stateStrings.get(i) + "->" + stateStrings.get(j) + "[label=\"" + Q[i][j] + "\",weight=\"" + Q[i][j] + "\"];");
						}
					}
				}
				bw.write("}");
				bw.close();

				String command = "dot -Tpdf " + file.getCanonicalPath() + " -o " + file.getCanonicalPath() + ".pdf";
				Runtime.getRuntime().exec(command);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
