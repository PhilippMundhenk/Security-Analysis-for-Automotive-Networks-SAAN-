package sas.ModelChecker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Specification;
import net.sf.opendse.model.Task;
import sas.components.Rules;
import sas.components.SASCommunication;
import sas.components.SASResource;
import sas.components.architecture.bus.items.Bus;
import sas.components.architecture.ecu.ECU;
import sas.components.architecture.ecu.Interface_in;
import sas.components.architecture.ecu.Interface_out;

public class MatrixBuilder {

	Specification implementation;
	ArrayList<Rules> transitionObjects;
	LinkedHashSet<List<Integer>> finalStates;
	Double[][] Q;

	Boolean debugOutput = false;

	public MatrixBuilder(Specification implementation) {
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
		ArrayList<Integer> initialState = new ArrayList<Integer>();
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
			for (int j = 0; j < r.getNumberOfStates(); j++) {
				set.add(j);
			}

			possibleStates.add(set);
			transitionObjects.add(r);

			initialState.add(r.getInitState());
		}

		for (Iterator<Task> i = implementation.getApplication().getVertices().iterator(); i.hasNext();) {
			Task t = i.next();

			if (t instanceof SASCommunication) {
				SASCommunication msg = (SASCommunication) t;

				HashSet<Integer> set = new HashSet<Integer>();
				for (int j = 0; j < msg.getNumberOfStates(); j++) {
					set.add(j);
				}
				possibleStates.add(set);
				transitionObjects.add(msg);
				initialState.add(msg.getInitState());
			}
		}

		finalStates = new LinkedHashSet<List<Integer>>();

		// iterate over all combinations of states:
		if (debugOutput) {
			System.out.print("transitions: {");
			for (Rules r : transitionObjects) {
				System.out.print(r + ",");
			}
			System.out.println("}");
		}
		// System.out.print("initialState: {");
		// for (Integer r : initialState) {
		// System.out.print(r + ",");
		// }
		// System.out.println("}");

		List<Integer> initialAfterInstant = processInstantTransitions(initialState);

		// System.out.print("adding state: {");
		// for (Integer r : initialAfterInstant) {
		// System.out.print(r + ",");
		// }
		// System.out.println("}");

		finalStates.add(copyList(initialAfterInstant));

		addValidStatesToFinalState(initialAfterInstant);

		// finalStates.add(initialState);

		// for (List<Integer> state : Sets.cartesianProduct(possibleStates)) {
		// List<Integer> processedForInstantTransitions =
		// processInstantTransitions(state);
		// finalStates.add(processedForInstantTransitions);
		// }

		if (debugOutput) {
			Integer cnt = 0;
			System.out.println();
			System.out.println("list of states:");
			for (List<Integer> l : finalStates) {
				System.out.print(cnt + ": {");
				for (Integer integer : l) {
					System.out.print(integer + ",");
				}
				System.out.println("}");
				cnt++;
			}
		}
		Q = new Double[finalStates.size()][finalStates.size()];

		for (int i = 0; i < Q.length; i++) {
			for (int j = 0; j < Q[i].length; j++) {
				Q[i][j] = 0.0;
			}
		}

		Integer outer = -1;
		Integer inner = -1;
		for (List<Integer> l1 : finalStates) {
			outer++;
			for (List<Integer> l2 : finalStates) {
				inner++;
				LinkedList<Rules> actives = new LinkedList<Rules>();
				Integer exploitableTransitions = 0;
				for (int i = 0; i < l1.size(); i++) {
					if (l1.get(i) != l2.get(i)) {
						Rules t = transitionObjects.get(i);
						if (t.isActive()) {
							actives.add(t);
						}
					}
					if (l1.get(i) + 1 == l2.get(i)) {
						Rules t = transitionObjects.get(i);
						if (t.isActive()) {
							exploitableTransitions++;
						}
					}
				}

				if (actives.size() == 1 && exploitableTransitions == 1) {
					Rules r = actives.pop();

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
							Q[inner][outer] = ((Resource) r).getAttribute("patch");
						}
						if (((Resource) r).getAttribute("explt") == null) {
							throw new NullPointerException("Exploitation rate is not set for " + r + ". Did you forget to set it?");
						} else {
							Q[outer][inner] = ((Resource) r).getAttribute("explt");
						}
					} else if (r instanceof SASCommunication) {
						// System.out.println("r is SASCommunication");
						if (((SASCommunication) r).getAttribute("patch") == null) {
							throw new NullPointerException("Patch rate is not set for " + r + ". Did you forget to set it?");
						} else {
							Q[inner][outer] = ((SASCommunication) r).getAttribute("patch");
						}
						if (((SASCommunication) r).getAttribute("explt") == null) {
							throw new NullPointerException("Exploitation rate is not set for " + r + ". Did you forget to set it?");
						} else {
							Q[outer][inner] = ((SASCommunication) r).getAttribute("explt");
						}
					}
				}
			}
			inner = -1;
		}

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

	private void addValidStatesToFinalState(List<Integer> oldState) {

		// System.out.print("oldState: {");
		// for (Integer r : oldState) {
		// System.out.print(r + ",");
		// }
		// System.out.println("}");

		for (int i = 0; i < oldState.size(); i++) {
			List<Integer> newState = copyList(oldState);
			Rules r = transitionObjects.get(i);
			if (r.isActive()) {
				if (r instanceof SASResource) {
					if (newState.get(i) + 1 < ((SASResource) r).getNumberOfStates()) {
						newState.set(i, newState.get(i) + 1);
						newState = processInstantTransitions(newState);
						// System.out.print("adding state: {");
						// for (Integer x : newState) {
						// System.out.print(x + ",");
						// }
						// System.out.println("}");
						if(!finalStates.contains(newState))
						{
							finalStates.add(copyList(newState));
							addValidStatesToFinalState(newState);
						}

						// System.out.print("returned to oldState: {");
						// for (Integer x : oldState) {
						// System.out.print(x + ",");
						// }
						// System.out.println("}");
					} else {
						continue;
						// newState.set(integer,
						// ((SASResource)r).getNumberOfStates()-1);
					}
				} else if (r instanceof SASCommunication) {
					if (newState.get(i) + 1 < ((SASCommunication) r).getNumberOfStates()) {
						newState.set(i, newState.get(i) + 1);
						newState = processInstantTransitions(newState);
						// System.out.print("adding state: {");
						// for (Integer x : newState) {
						// System.out.print(x + ",");
						// }
						// System.out.println("}");
						if(!finalStates.contains(newState))
						{
							finalStates.add(copyList(newState));
							addValidStatesToFinalState(newState);
						}

						// System.out.print("returned to oldState: {");
						// for (Integer x : oldState) {
						// System.out.print(x + ",");
						// }
						// System.out.println("}");
					} else {
						continue;
						// newState.set(integer,
						// ((SASCommunication)r).getNumberOfStates()-1);
					}
				}
			}
		}
	}

	private List<Integer> processInstantTransitions(List<Integer> state) {

		List<Integer> oldState = new ArrayList<Integer>();
		List<Integer> returnVal = new ArrayList<Integer>(state);
		while (!isListEqual(oldState, returnVal)) {
			oldState = copyList(returnVal);
			for (int phase = 0; phase < 5; phase++) {
				Integer cnt = 0;
				if (debugOutput) {
					System.out.println("phase=" + phase);

					System.out.print("before: {");
					for (Integer integ : returnVal) {
						System.out.print(integ + ",");
					}
					System.out.println("}");
				}

				for (Integer s : returnVal) {
					Rules node = transitionObjects.get(cnt);
					if (Interface_in.class.isAssignableFrom(node.getClass()) && 0 == phase) {
						if (node instanceof Resource) {
							/* just a safety check, should always hold */
							returnVal.set(cnt, node.checkInstantTransitions(s, getNeighboursResource((Resource) node, returnVal), null));
						}
					} else if (ECU.class.isAssignableFrom(node.getClass()) && 1 == phase) {
						if (node instanceof Resource) {
							/* just a safety check, should always hold */
							returnVal.set(cnt, node.checkInstantTransitions(s, getNeighboursResource((Resource) node, returnVal), getSecondNeighboursResource((Resource) node, returnVal)));
						}
					} else if (Interface_out.class.isAssignableFrom(node.getClass()) && 2 == phase) {
						if (node instanceof Resource) {
							/* just a safety check, should always hold */
							returnVal.set(cnt, node.checkInstantTransitions(s, getNeighboursResource((Resource) node, returnVal), null));
						}
					} else if (Bus.class.isAssignableFrom(node.getClass()) && 3 == phase) {
						if (node instanceof Resource) {
							/* just a safety check, should always hold */
							returnVal.set(cnt, node.checkInstantTransitions(s, getNeighboursResource((Resource) node, returnVal), getSecondNeighboursResource((Resource) node, returnVal)));
						}
					} else if (SASCommunication.class.isAssignableFrom(node.getClass()) && 4 == phase) {
						if (node instanceof SASCommunication) {
							/* just a safety check, should always hold */
							if (!((SASCommunication) node).getId().startsWith("dummy")) {
								/* do not process dummy messages */
								returnVal.set(cnt, node.checkInstantTransitions(s, getNeighboursMessage((SASCommunication) node, returnVal), null));
							}
						}
					}
					cnt++;
				}

				if (debugOutput) {
					System.out.print("after : {");
					for (Integer integ : returnVal) {
						System.out.print(integ + ",");
					}
					System.out.println("}");
				}
			}
		}

		if (debugOutput) {
			System.out.print("FINAL: {");
			for (Integer integ : returnVal) {
				System.out.print(integ + ",");
			}
			System.out.println("}");
		}

		return returnVal;
	}

	private List<Integer> copyList(List<Integer> returnVal) {
		List<Integer> l = new ArrayList<Integer>();
		for (Integer integer : returnVal) {
			l.add(integer);
		}
		return l;
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

	private boolean isListEqual(List<Integer> listA, List<Integer> listB) {
		if (listA.size() != listB.size()) {
			return false;
		}

		for (int i = 0; i < listA.size(); i++) {
			if (listA.get(i) != listB.get(i)) {
				return false;
			}
		}

		return true;
	}

	public List<Rules> getTransitionObjects() {
		return transitionObjects;
	}

	public LinkedHashSet<List<Integer>> getStates() {
		return finalStates;
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
		LinkedHashSet<List<Integer>> states = getStates();
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
