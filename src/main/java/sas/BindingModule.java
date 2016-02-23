package sas;

import net.sf.opendse.model.Architecture;
import net.sf.opendse.model.Link;
import net.sf.opendse.model.Resource;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class BindingModule extends AbstractModule {

	@Override
	protected void configure() {

	}

	Architecture<Resource, Link> architecture;

	@Provides
	Architecture<Resource, Link> provideArchitecture() {
		if (null == architecture) {
			architecture = new Architecture<Resource, Link>();
		}
		return architecture;
	}

}
