package org.fogbowcloud.cafe.core;

import java.net.URISyntaxException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Random;

import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
import org.fogbowcloud.cafe.core.models.ShibToken;
import org.fogbowcloud.cafe.core.saml.SAMLAssertionHolder;
import org.fogbowcloud.cafe.utils.RSAUtils;

public class ShibController {

	private static final Logger LOGGER = Logger.getLogger(ShibController.class);
	
	protected static final String UTF_8 = "UTF-8";
	
	protected static final String KEY_URL_PARAMETER = "key";
	protected static final String KEY_SIGNATURE_URL_PARAMETER = "keySignature";
	protected static final String TOKEN_URL_PARAMETER = "token";
	
	protected static final String DEFAULT_DOMAIN_ASSERTION_URL = "localhost";
	public static final String SHIB_RAS_TOKEN_STRING_SEPARATOR = "!#!";

	public String createToken(String assertionUrl) throws Exception {
		String assertionResponse = getAssertionResponse(assertionUrl);		
		Map<String, String> assertionAttrs = getAssertionAttr(assertionResponse);		
		String eduPersonPrincipalName = SAMLAssertionHolder.getEduPersonPrincipalName(assertionAttrs);		
		String commonName = SAMLAssertionHolder.getCommonName(assertionAttrs);		
		String secret = createSecret();			
		String assertionUrlNormalized = normalizeAssertionUrl(assertionUrl);		
		
		ShibToken shibToken = new ShibToken(secret, assertionUrlNormalized, eduPersonPrincipalName, commonName, assertionAttrs);		
		return shibToken.generateTokenStr();
	}

	// TODO understand better about this assertion url because this one will be used by RAS
	// TODO adding shib ip when the assertion url is a localhost domain
	protected String normalizeAssertionUrl(String assertionUrl) {
		String shibIp = PropertiesHolder.getShibIp();
		String assertionUrlNormalized = assertionUrl.replace(DEFAULT_DOMAIN_ASSERTION_URL, shibIp);
		return assertionUrlNormalized;
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
	
	public String encrypRSAKey(String key) throws Exception {
		try {
			String rasPublicKeyPath = PropertiesHolder.getRasPublicKey();
			RSAPublicKey publicKey = getPublicKey(rasPublicKeyPath);
			return RSAUtils.encrypt(key, publicKey);
		} catch (Exception e) {
			String errorMsg = "Is not possible encryp(RAS) the message.";
			LOGGER.error(errorMsg, e);
			throw new Exception(errorMsg);
		}		
	}
	
	public String encrypAESRasToken(String rasToken, String aesKey) throws Exception {
		try {
			return RSAUtils.encryptAES(aesKey.getBytes(UTF_8), rasToken);
		} catch (Exception e) {
			String errorMsg = "Is not possible encryp(AES) the message.";
			LOGGER.error(errorMsg, e);
			throw new Exception(errorMsg);
		}
	}
	
	public String createAESkey() {
		return RSAUtils.generateAESKey();
	}
	
	public String signKey(String key) throws Exception {
		try {
			String shibPrivateKeyPath = PropertiesHolder.getShibPrivateKey();
			RSAPrivateKey privateKey = getPrivateKey(shibPrivateKeyPath);
			return RSAUtils.sign(privateKey, key);
		} catch (Exception e) {
			String errorMsg = "Is not possible sign the message.";
			LOGGER.error(errorMsg, e);
			throw new Exception(errorMsg);
		}		
	}	

	public String createTargetUrl(String rasTokenEncrypted, String keyEncrypted, String keySigned) throws URISyntaxException {
		String urlDashboard = PropertiesHolder.getDashboardUrl();
		URIBuilder uriBuilder = new URIBuilder(urlDashboard);
		uriBuilder.addParameter(TOKEN_URL_PARAMETER, rasTokenEncrypted);
		uriBuilder.addParameter(KEY_URL_PARAMETER, keyEncrypted);
		uriBuilder.addParameter(KEY_SIGNATURE_URL_PARAMETER, keySigned);
		return uriBuilder.toString();
	}

	protected RSAPublicKey getPublicKey(String publicKeyPath) {
		try {
			return RSAUtils.getPublicKey(publicKeyPath.trim());			
		} catch (Exception e) {
			LOGGER.error("There is not possible get public key.", e);	
		}
		return null;
	}

	protected RSAPrivateKey getPrivateKey(String privateKeyPath) {	
		try {
			return RSAUtils.getPrivateKey(privateKeyPath.trim());
		} catch (Exception e) {
			LOGGER.error("There is not possible get private key.", e);
		}
		return null;
	}
	
}
