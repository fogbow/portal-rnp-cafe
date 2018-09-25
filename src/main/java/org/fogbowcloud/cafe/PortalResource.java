package org.fogbowcloud.cafe;

import java.net.URLEncoder;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.fogbowcloud.cafe.utils.RSAUtils;
import org.fogbowcloud.cafe.utils.ResourceUtil;
import org.restlet.engine.adapter.HttpRequest;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class PortalResource extends ServerResource {
	
	private static final String UTF_8 = "UTF-8";
	private static final Logger LOGGER = Logger.getLogger(PortalResource.class);

	@Get
	public String fetch() throws Exception {
		String requestIdentifier = String.valueOf(new Random().nextInt());
		LOGGER.debug("Request(" + requestIdentifier + ") - Starting new request.");
		PortalApplication app = (PortalApplication) getApplication();
		HttpRequest req = (HttpRequest) getRequest();
		Map<String, String> attributes = ResourceUtil.getSAMLAttributes(app, req);

		String parametersURL = "?" + ResourceUtil.EDU_PERSON_PRINCIPAL_NAME_ASSERTION_ATTRIBUTE + "="
				+ ResourceUtil.getEduPersonName(attributes);
		String institutionDashboardURL = app.getProperties().getProperty(ResourceUtil.IGUASSU_DASHBOARD_URL_CONF);
		String targetURL = institutionDashboardURL + parametersURL;
		LOGGER.debug("Request(" + requestIdentifier + ") - Redirecting to: " + targetURL);
		getResponse().redirectPermanent(targetURL);
		
		return new String();
	}

}
