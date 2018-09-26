package org.fogbowcloud.cafe.api.http;

import java.util.Random;

import org.apache.log4j.Logger;
import org.fogbowcloud.cafe.core.ShibController;
import org.fogbowcloud.cafe.core.saml.SAMLAssertionHolder;
import org.restlet.engine.adapter.HttpRequest;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class ShibResource extends ServerResource {
		
	private static final Logger LOGGER = Logger.getLogger(ShibResource.class);
	
	@Get
	public String fetch() throws Exception {
		String requestIdentifier = getRequestIdentifier();
		HttpRequest httpRequest = (HttpRequest) getRequest();
		
		LOGGER.info(String.format("Request(%s) - Starting new request.", requestIdentifier));		
		String assertionUrl = getAssertionUrl(httpRequest);
		
		String rasToken = ShibController.createToken(assertionUrl);		
		String rasTokenEncrypted = ShibController.encrypToken(rasToken);
		String rasTokenSigned = ShibController.signToken(rasToken);  
		
		String targetURL = ShibController.createTargetUrl(rasTokenEncrypted, rasTokenSigned);		
		LOGGER.info(String.format("Request(%s) - Redirecting to: %s", requestIdentifier, targetURL));
		getResponse().redirectPermanent(targetURL);
		return new String();
	}

	private String getAssertionUrl(HttpRequest httpRequest) {
		return httpRequest.getHeaders().getFirstValue(SAMLAssertionHolder.SHIB_ASSERTION_HEADER);
	}

	private String getRequestIdentifier() {
		return String.valueOf(new Random().nextInt());
	}

}
