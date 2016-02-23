package architectures;

import java.util.HashSet;

import net.sf.opendse.model.Application;
import net.sf.opendse.model.Architecture;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Link;
import net.sf.opendse.model.Mappings;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Specification;
import net.sf.opendse.model.Task;
import sas.ArchitectureTemplate;
import sas.components.architecture.Rates;
import sas.components.architecture.bus.builders.CANBuilder;
import sas.components.architecture.bus.builders.FlexRayBuilder;
import sas.components.architecture.bus.builders.ThreeGBuilder;
import sas.components.architecture.bus.items.Bus;
import sas.components.architecture.ecu.ECU;
import sas.components.architecture.ecu.ECUBuilder_3GExploitable;
import sas.util.DummyMessages;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class RandomArch implements ArchitectureTemplate {

	private Boolean initialized = false;
	private Specification spec;
	private static Integer numberECUs;
	
	public Specification getTemplate() {
		if (!initialized) {
			initialize();
		}

		return spec;
	}

	Architecture<Resource, Link> arch;

	CANBuilder canBuilder;
	ThreeGBuilder tgBuilder;
	Provider<FlexRayBuilder> flexRayBuilderProvider;
	Provider<ECUBuilder_3GExploitable> ecuBuilderProvider;
	
	@Inject
	public RandomArch(Architecture<Resource, Link> arch,CANBuilder canBuilder,ThreeGBuilder tgBuilder,Provider<FlexRayBuilder> flexRayBuilderProvider,Provider<ECUBuilder_3GExploitable> ecuBuilderProvider){
		this.arch = arch;
		this.canBuilder= canBuilder;
		this.tgBuilder = tgBuilder;
		this.flexRayBuilderProvider = flexRayBuilderProvider;
		this.ecuBuilderProvider = ecuBuilderProvider;
		initialize();
	}
	
	public static void setNumberECUs(Integer ecus)
	{
		numberECUs = ecus;
	}
	
	public void initialize() {
		/* create application */
		Application<Task, Dependency> application = new Application<Task, Dependency>();

		/* create architecture */
		Bus tg = tgBuilder.getNew("3g1");
		Bus can1 = canBuilder.getNew("can1");
		
		ECU e = ecuBuilderProvider.get().getNewECU("e", tg);
		ECU target = ecuBuilderProvider.get().getNewECU("target", can1);
		
		ecuBuilderProvider.get().addECUToBus(e, can1);
		
		/* add attributes to architecture */
		Rates.initialize(arch, application);
		
		int r_inf = 10; //0
		int r_expl = 2;
		int r_patch = 1;
		
		Rates.addRates(new Rates("e", r_inf, r_inf));
		Rates.addRates(new Rates("e_i_out_3g1", r_inf, r_inf));
		Rates.addRates(new Rates("e_i_in_3g1", r_expl, r_patch));
		Rates.addRates(new Rates("e_i_out_can1", 0, r_inf));
		Rates.addRates(new Rates("e_i_in_can1", r_expl, r_patch));
		Rates.addRates(new Rates("target", r_inf, r_inf));
		Rates.addRates(new Rates("target_i_out_can1", 0, r_inf));
		Rates.addRates(new Rates("target_i_in_can1", r_expl, r_patch));
		
		/* create mapping */
		Mappings<Task, Resource> mappings = new Mappings<Task, Resource>();
		
		HashSet<ECU> mandatoryECUs = new HashSet<ECU>();
		mandatoryECUs.add(e);
		mandatoryECUs.add(target);
		
		for (int i = 1; i <= numberECUs; i++) {
			ECU ecu = ecuBuilderProvider.get().getNewECU("ecu"+i, can1);
			
			Rates.addRates(new Rates("ecu"+i, r_inf, r_inf));
			Rates.addRates(new Rates("ecu"+i+"_i_out_"+can1.getId(), 0, r_inf));
			Rates.addRates(new Rates("ecu"+i+"_i_in_"+can1.getId(), r_expl, r_patch));
			
			mandatoryECUs.add(ecu);
		}
		
		DummyMessages.createDummyMessages(mandatoryECUs, arch, application, mappings);
		
		spec = new Specification(application, arch, mappings);
		
		initialized = true;
	}
}