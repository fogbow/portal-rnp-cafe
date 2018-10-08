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
		
		ShibController shibController = new ShibController();
		
		String rasToken = shibController.createToken(assertionUrl);
		String aesKey = shibController.createAESkey();
		String rasTokenEncryptedAes = shibController.encrypAESRasToken(rasToken, aesKey);
		String keyEncrypted = shibController.encrypRSAKey(aesKey);
		String keySigned = shibController.signKey(aesKey);  
		
		String targetURL = shibController.createTargetUrl(rasTokenEncryptedAes, keyEncrypted, keySigned);		
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
