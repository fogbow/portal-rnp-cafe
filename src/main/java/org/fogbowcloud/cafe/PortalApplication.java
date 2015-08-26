package org.fogbowcloud.cafe;

import java.util.Properties;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class PortalApplication extends Application {

	private Properties properties;

	public PortalApplication(Properties properties) {
		this.properties = properties;
	}

	public Properties getProperties() {
		return properties;
	}
	
	@Override
	public Restlet createInboundRoot() {
		Router router = new Router(getContext());
		router.attach("/", PortalResource.class);
		return router;
	}
	
}
