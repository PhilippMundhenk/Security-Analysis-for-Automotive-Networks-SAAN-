package sas;

import net.sf.opendse.model.Specification;
import net.sf.opendse.optimization.encoding.SingleImplementation;

import org.opt4j.core.Objectives;

import architectures.RandomArch;

import com.google.inject.Guice;
import com.google.inject.Injector;

import evaluators.RawEvaluator;


public class SAS {
	
	public static void main(String[] args) throws Exception {
		Injector injector = Guice.createInjector(new BindingModule());
		
		ArchitectureTemplate arch = null;
		for (int i = 1; i <= 12; i++) {
			System.out.println("ECUs: "+i);
			RandomArch.setNumberECUs(i);
			injector = Guice.createInjector(new BindingModule());
			arch = injector.getInstance(RandomArch.class);
		
			RawEvaluator.setElementToEvaluate("target");
			RawEvaluator.setTime(1.0);
			RawEvaluator.doSteadyState(false);
		
			System.out.println("Model Checking SAAN");
			Specification specification = arch.getTemplate();
			
			SingleImplementation singleImplementation = new SingleImplementation();
			Specification implementation = singleImplementation.get(specification);
			
			RawEvaluator eval = new RawEvaluator();
			Objectives o = new Objectives();
			long start = System.nanoTime();
			eval.evaluate(implementation, o);
			long stop = System.nanoTime();
			System.out.println("Q.length: "+eval.getQ().length);
			System.out.println("model checking time[s]: "+eval.getModelCheckingTime_ns()/(double)1000000000);
			System.out.println("time[s]: "+((stop-start)/(double)1000000000));
			System.out.println("value: "+o.getValues().iterator().next().getDouble());
		}
		
//		OptimizationModule o = new OptimizationModule();
//		EvolutionaryAlgorithmModule ea = new EvolutionaryAlgorithmModule();
//		ea.setGenerations(1);
//		InputModule i = new InputModule();
//		i.setFilename(specFile);
//		ViewerModule v = new ViewerModule();
//		ClassEvaluatorModule c = new ClassEvaluatorModule();
//		c.setClassname(RawEvaluator.class.getCanonicalName());
//		
//		Opt4JTask task = new Opt4JTask(false);
//		task.init(o,ea,i,v,c);
//		try {
//		        task.execute();
////		        Archive archive = task.getInstance(Archive.class);
////		        for (Individual individual : archive) {
////		                // obtain the phenotype and objective, etc. of each individual
////		        }
//		} catch (Exception e) {
//		        e.printStackTrace();
//		} finally {
//		        task.close();
//		} 
	}

}
