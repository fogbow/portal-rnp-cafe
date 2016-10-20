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
		
		LOGGER.debug("Request(" + requestIdentifier + ") - Attributes in Shib-assertion: " + attributes);
		
		String institutionDashboardURL = app.getProperties().getProperty(ResourceUtil.getInstituonIdp(attributes));		
		if (institutionDashboardURL == null || institutionDashboardURL.isEmpty()) {
			institutionDashboardURL = app.getProperties().getProperty("default_dashboard");
		}			
		
		String privateKeyPath = app.getProperties().getProperty(ResourceUtil.PRIVATE_KEY_PATH_CONF); 
		RSAPrivateKey privateKey = ResourceUtil.getPrivateKey(privateKeyPath);
		LOGGER.debug("Request(" + requestIdentifier + ") - Getting private key in " 
				+ privateKeyPath + " and is " + privateKey != null ? "Ok" : "null");		
		
		String publicKeyPath = app.getProperties().getProperty(ResourceUtil.PUBLIC_KEY_PATH_CONF);
		RSAPublicKey publicKey = ResourceUtil.getPublicKey(publicKeyPath);
		LOGGER.debug("Request(" + requestIdentifier + ") - Getting public key in " 
				+ publicKeyPath + " and is " + publicKey != null ? "Ok" : "null");
		
		String nonce = ResourceUtil.createNonce(app, institutionDashboardURL);
		String nonceSignature = RSAUtils.sign(privateKey, nonce);
		String token = ResourceUtil.createToken(app, attributes);
		String secretKey = RSAUtils.generateAESKey();
		String tokenEncrypted = RSAUtils.encryptAES(secretKey.getBytes(UTF_8), secretKey + token);
		String secretKeyEncrypted = RSAUtils.encrypt(secretKey, publicKey);
		String secreteSignature = RSAUtils.sign(privateKey, secretKeyEncrypted);		
		
		String parametersURL = "?" + ResourceUtil.TOKEN_ENCRYPTED_PARAMETER + "=" + URLEncoder.encode(tokenEncrypted, UTF_8)
				+ "&" + ResourceUtil.SECRET_KEY_ENCRYPTED_PARAMETER + "=" + URLEncoder.encode(secretKeyEncrypted, UTF_8)
				+ "&" + ResourceUtil.SECRET_KEY_SIGNATURE_PARAMETER + "=" + URLEncoder.encode(secreteSignature, UTF_8)
				+ "&" + ResourceUtil.NONCE_PARAMETER + "=" + URLEncoder.encode(nonce, UTF_8)
				+ "&" + ResourceUtil.NONCE_SIGNATURE_PARAMETER + "=" + URLEncoder.encode(nonceSignature, UTF_8);
		
		String targetURL = institutionDashboardURL + parametersURL;
		LOGGER.debug("Request(" + requestIdentifier + ") - Redirecting to: " + targetURL);
		getResponse().redirectPermanent(targetURL);
		
		return new String();
	}

}
