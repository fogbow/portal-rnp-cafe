package org.fogbowcloud.cafe.core;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
import org.fogbowcloud.cafe.core.saml.SAMLAssertionHolder;
import org.fogbowcloud.cafe.utils.RSAUtils;
import org.json.JSONArray;
import org.json.JSONException;

public class ShibController {

	private static final Logger LOGGER = Logger.getLogger(ShibController.class);
	
	protected static final String SIGNATURE_URL_PARAMETER = "signature";
	protected static final String TOKEN_URL_PARAMETER = "token";
	protected static final String DEFAULT_DOMAIN_ASSERTION_URL = "localhost";
	private static final String SHIB_RAS_TOKEN_STRING_SEPARATOR = "!#!";

	// TODO implement tests
	public String createToken(String assertionUrl) throws Exception {
		String assertionResponse = getAssertionResponse(assertionUrl);
		
		Map<String, String> assertionAttrs = getAssertionAttr(assertionResponse);
		
		String eduPersonPrincipalName = SAMLAssertionHolder.getEduPersonPrincipalName(assertionAttrs);
		
		String commonName = SAMLAssertionHolder.getCommonName(assertionAttrs);
		
		String secret = createSecret();
				
		String identityProvider = SAMLAssertionHolder.getIdentityProvider(assertionResponse);
		
		String assertionUrlNormalized = normalizeAssertionUrl(assertionUrl);		
		
		String assertionAttrsStr = normalizeAssertionAttrs(assertionAttrs);
		
		String token = normalizeToken(assertionUrlNormalized, assertionAttrsStr, eduPersonPrincipalName, 
				commonName, secret, identityProvider);
		
		return token;
	}

	protected String normalizeAssertionAttrs(Map<String, String> assertionAttrs) throws JSONException {
		// TODO check token size. Limit attrs size. 
		JSONArray jsonArray = new JSONArray(assertionAttrs);
		return jsonArray.toString();
	}

	// TODO understand better about this assertion url because this one will be used by RAS
	// TODO check this default domain asserion url. Check if "localhost" is everytime present in the URL
	protected String normalizeAssertionUrl(String assertionUrl) {
		String shibIp = PropertiesHolder.getShibIp();
		String assertionUrlNormalized = assertionUrl.replace(DEFAULT_DOMAIN_ASSERTION_URL, shibIp);
		return assertionUrlNormalized;
	}

	protected String normalizeToken(String assertionUrl, String assertionAttrsStr,
			String userId, String userName, String secret, String identityProvider) {
		String[] parameters = new String[] { 
				secret, 
				assertionUrl,
				identityProvider, 
				userId, 
				userName, 
				assertionAttrsStr };
		String token = StringUtils.join(parameters, SHIB_RAS_TOKEN_STRING_SEPARATOR);
		return token;
	}

	protected Map<String, String> getAssertionAttr(String assertionResponse) throws Exception {
		if (assertionResponse == null) {
			String errorMsg = "The assertionResponse is null.";
			LOGGER.error(errorMsg);
			throw new Exception(errorMsg);
		}
		
		Map<String, String> assertionAttrs = SAMLAssertionHolder.getAssertionAttrs(assertionResponse);
		return assertionAttrs;
	}

	protected String getAssertionResponse(String assertionUrl) throws Exception {
		String assertionResponse = null;
		try {
			assertionResponse = SAMLAssertionHolder.getAssertionResponse(assertionUrl);
		} catch (Exception e) {
			String errorMsg = "Is not possible get assertions.";
			LOGGER.error(errorMsg, e);
			throw new Exception(errorMsg);
		}
		return assertionResponse;
	}
	
	protected String createSecret() throws Exception {		
		return String.valueOf(new Random().nextInt());
	}
	
	// TODO implement tests
	public String encrypToken(String rasToken) throws IOException, GeneralSecurityException {
		String rasPublicKeyPath = PropertiesHolder.getRasPublicKey();
		RSAPublicKey publicKey = getPublicKey(rasPublicKeyPath);
		return RSAUtils.encrypt(rasToken, publicKey);
	}
	
	// TODO implement tests	
	public String signToken(String rasToken) throws IOException, GeneralSecurityException {
		String shibPrivateKeyPath = PropertiesHolder.getShibPrivateKey();
		RSAPrivateKey privateKey = getPrivateKey(shibPrivateKeyPath);
		return RSAUtils.sign(privateKey, rasToken);
	}	

	// TODO implement tests
	public String createTargetUrl(String rasTokenEncrypted, String rasTokenSigned) throws URISyntaxException {
		String urlDashboard = PropertiesHolder.getDashboardUrl();
		URIBuilder uriBuilder = new URIBuilder(urlDashboard);
		uriBuilder.addParameter(TOKEN_URL_PARAMETER, rasTokenEncrypted);
		uriBuilder.addParameter(SIGNATURE_URL_PARAMETER, rasTokenSigned);
		return uriBuilder.toString();
	}

	protected RSAPublicKey getPublicKey(String publicKeyPath) {
		try {
			return RSAUtils.getPublicKey(publicKeyPath.trim());			
		} catch (Exception e) {
			LOGGER.warn("There is not possible get public key.", e);	
		}
		return null;
	}

	protected RSAPrivateKey getPrivateKey(String privateKeyPath) {	
		try {
			return RSAUtils.getPrivateKey(privateKeyPath.trim());
		} catch (Exception e) {
			LOGGER.warn("There is not possible get private key.", e);
		}
		return null;
	}

	
}
