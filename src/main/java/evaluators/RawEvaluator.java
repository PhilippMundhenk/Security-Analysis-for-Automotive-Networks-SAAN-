package evaluators;

import java.io.File;
import java.util.ArrayList;

import net.sf.opendse.model.Specification;
import net.sf.opendse.optimization.ImplementationEvaluator;

import org.opt4j.core.Objective.Sign;
import org.opt4j.core.Objectives;

import sas.Settings;
import sas.ModelChecker.MatrixBuilder;
import sas.ModelChecker.ModelCheckerRaw;
import sas.ModelChecker.ObjectiveGenerator;
import sas.ModelChecker.SteadyStateAnalyzer;

public class RawEvaluator implements ImplementationEvaluator {

	private Boolean debugOutput = false;
	private static String elementToEvaluate = "msg1";
	private static Double accuracy = 1.0E-17; //PRISM delivers 17 decimal places. Highest possible accuracy: Double.MIN_VALUE; 
	private static Double time = 1.0;
	private static boolean doSteadyState = false;
	private Double[][] Q;
	private Double[] rho;
	private Long modelCheckingTime_ns;

	public Specification evaluate(Specification implementation, Objectives objectives) {

		if (debugOutput) {
			System.out.println();
			System.out.println("Running Evaluator...");
		}
		MatrixBuilder gen = new MatrixBuilder(implementation);
		Double[][] Q = gen.generateStateSpace();
		this.setQ(Q);

		Double[] rho = null;

		ObjectiveGenerator obj = new ObjectiveGenerator();
		ArrayList<Integer> conditions = new ArrayList<Integer>();
		conditions.add(1);

		Double exploitability = Double.MAX_VALUE;
		try {
			/* set target to optimize for */
			if (elementToEvaluate == null) {
				throw new Exception("elementToEvaluate is not set");
			}

			try {
				rho = obj.getSingleObjectRho(gen.getStates(), gen.getTransitionObjects(), elementToEvaluate, conditions);
				this.setRho(rho);
			} catch (NullPointerException e) {
				throw new Exception("elementToEvaluate does not exist");
			}
			if (debugOutput) {
				System.out.println();
				System.out.print("rho: {");
				for (int i = 0; i < rho.length; i++) {
					System.out.print(rho[i] + ",");
				}
				System.out.println("}");
			}

			ModelCheckerRaw checker = new ModelCheckerRaw();
			long start = System.nanoTime();
			exploitability = checker.checkModel(Q, rho, time, accuracy);
			long stop = System.nanoTime();
			setModelCheckingTime_ns(stop-start);
//			System.out.println("Model checking: " + exploitability+ " in "+((stop - start) / (double) 1000000000)+"s");
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		if(doSteadyState)
		{
			SteadyStateAnalyzer a = new SteadyStateAnalyzer();
			try {
				System.out.println("SteadyState Analysis:" + a.analyze(Q, rho));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
		if (Settings.PDF_OUTPUT) {
			gen.toPDF(new File("graphs/graph1.dot"));
		}

		if (Settings.PRISM_EXPORT) {
			gen.exportPRISMFiles(new File("PRISM/" + Settings.architecture.getName() + ".sta"), new File("PRISM/" + Settings.architecture.getName() + ".tra"));
			gen.exportRewards(new File("PRISM/" + Settings.architecture.getName() + ".rews"), rho);
		}

		objectives.add("exploitability", Sign.MIN, exploitability);

		return null;
	}

	public int getPriority() {
		if (debugOutput) {
			System.out.println("getPriority()");
		}
		return 0;
	}

	public static String getElementToEvaluate() {
		return elementToEvaluate;
	}

	public static void setElementToEvaluate(String elementToEvaluate) {
		RawEvaluator.elementToEvaluate = elementToEvaluate;
	}

	public static Double getTime() {
		return time;
	}

	public static void setTime(Double time) {
		RawEvaluator.time = time;
	}

	public static void doSteadyState(boolean b) {
		RawEvaluator.doSteadyState = b;		
	}

	public Double[][] getQ() {
		return Q;
	}

	public void setQ(Double[][] q) {
		Q = q;
	}

	public Double[] getRho() {
		return rho;
	}

	public void setRho(Double[] rho) {
		this.rho = rho;
	}

	public Long getModelCheckingTime_ns() {
		return modelCheckingTime_ns;
	}

	public void setModelCheckingTime_ns(Long modelCheckingTime_ns) {
		this.modelCheckingTime_ns = modelCheckingTime_ns;
	}

}
