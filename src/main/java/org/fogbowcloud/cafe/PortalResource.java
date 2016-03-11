package org.fogbowcloud.cafe;

import java.util.Map;

import org.fogbowcloud.cafe.saml.SAMLAssertionRetriever;
import org.restlet.engine.adapter.HttpRequest;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class PortalResource extends ServerResource {

	@Get
	public String fetch() throws Exception {
		PortalApplication app = (PortalApplication) getApplication();
		HttpRequest req = (HttpRequest) getRequest();
		String getAssertionURL = req.getHeaders().getFirstValue("Shib-assertion");
		SAMLAssertionRetriever assertionRetriever = new SAMLAssertionRetriever();
		Map<String, String> attributes = assertionRetriever.retrieve(getAssertionURL);
		String identifier = attributes.get("eduPersonPrincipalName");
		
		String institutionIdp = identifier.split("@")[1];
		String institutionDashboardURL = app.getProperties().getProperty(institutionIdp);
		
		if (institutionDashboardURL == null || institutionDashboardURL.isEmpty()) {
			institutionDashboardURL = app.getProperties().getProperty("default_dashboard");
		}
		
		getResponse().redirectPermanent("http://" + institutionDashboardURL);
		
		return new String();
	}
	
}
