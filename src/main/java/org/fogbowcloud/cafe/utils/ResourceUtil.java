package org.fogbowcloud.cafe.utils;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.log4j.Logger;
import org.fogbowcloud.cafe.PortalApplication;
import org.fogbowcloud.cafe.saml.HttpClientWrapper;
import org.fogbowcloud.cafe.saml.SAMLAssertionRetriever;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.engine.adapter.HttpRequest;

public class ResourceUtil {

	public static final String NONCE_SIGNATURE_PARAMETER = "nonceSignature";
	public static final String NONCE_PARAMETER = "nonce";
	public static final String SECRET_KEY_SIGNATURE_PARAMETER = "secretKeySignature";
	public static final String SECRET_KEY_ENCRYPTED_PARAMETER = "secretKeyEncrypted";
	public static final String TOKEN_ENCRYPTED_PARAMETER = "tokenEncrypted";
	public static final String EDU_PERSON_PRINCIPAL_NAME_ASSERTION_ATTRIBUTE = "eduPersonPrincipalName";
	public static final String SN_ASSERTION_ATTRIBUTES = "sn";
	
	private static final String TOKEN_EXPIRATION_CONF = "token_expiration";
	public static final String PUBLIC_KEY_PATH_CONF = "public_key_path";
	public static final String PRIVATE_KEY_PATH_CONF = "private_key_path";
	
	private static final String SAML_ATTRIBUTES = "saml_attributes";
	private static final String TOKEN_ETIME = "token_etime";
	private static final String NAME = "name";
	private static final String TOKEN_CTIME = "token_ctime";
	
	private static final String DEFAULT_TOKEN_EXPIRATION = "1440"; // 1 day in minutes. 60 x 24
	
	private static final Logger LOGGER = Logger.getLogger(ResourceUtil.class);
	private static final String HTTPS_PREFIX = "https://";
	
	public static RSAPublicKey getPublicKey(String publicKeyPath) {
		try {
			return RSAUtils.getPublicKey(publicKeyPath.trim());			
		} catch (Exception e) {
			LOGGER.warn("There is not possible get public key.", e);	
		}
		return null;
	}

	public static RSAPrivateKey getPrivateKey(String privateKeyPath) {	
		try {
			return RSAUtils.getPrivateKey(privateKeyPath.trim());
		} catch (Exception e) {
			LOGGER.warn("There is not possible get private key.", e);
		}
		return null;
	}

	public static String getInstituonIdp(Map<String, String> attributes) {
		String identifier = null;
		try {
			identifier = attributes.get(EDU_PERSON_PRINCIPAL_NAME_ASSERTION_ATTRIBUTE);
			return identifier.split("@")[1];			
		} catch (NullPointerException e) {
			LOGGER.debug("Without attributes or EduPersonPrincipalName ("
					+ identifier + ") in a diferent format.");
		}
		return null;
	}

	public static Map<String, String> getSAMLAttributes(PortalApplication app, HttpRequest req) throws Exception {
		String getAssertionURL = req.getHeaders().getFirstValue("Shib-assertion");
		SAMLAssertionRetriever assertionRetriever = app.getSamlAssertionRetriever();
		if (app.getSamlAssertionRetriever() == null) {
			assertionRetriever = new SAMLAssertionRetriever();
		}
		Map<String, String> attributes = assertionRetriever.retrieve(getAssertionURL);
		return attributes;
	}

	public static String createNonce(PortalApplication app, String institutionDashboardURL) throws Exception {
		HttpClientWrapper httpClientWrapper = app.getHttpClientWrapper();
		if (httpClientWrapper == null) {
			httpClientWrapper = new HttpClientWrapper();			
		}
		SSLConnectionSocketFactory sslsf = createSSLConnectionSelfSigned();		
		if (institutionDashboardURL.startsWith(HTTPS_PREFIX)) {
			return httpClientWrapper.doGetSSL(institutionDashboardURL + "/nonce", sslsf).getContent();			
		} 
		return httpClientWrapper.doGet(institutionDashboardURL + "/nonce").getContent();
	}

	private static SSLConnectionSocketFactory createSSLConnectionSelfSigned()
			throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		SSLContextBuilder builder = new SSLContextBuilder();
		builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
		@SuppressWarnings("deprecation")
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
				builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		return sslsf;
	}

	public static String createToken(PortalApplication app, Map<String, String> attributes) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(NAME, attributes.get(SN_ASSERTION_ATTRIBUTES));
		long currentTimeMillis = app.getDateUtils().getCurrentTimeMillis();
		jsonObject.put(TOKEN_CTIME, currentTimeMillis);
		String tokenExpiration = app.getProperties().getProperty(TOKEN_EXPIRATION_CONF);
		jsonObject.put(TOKEN_ETIME, currentTimeMillis + Long.parseLong(
				tokenExpiration != null ? tokenExpiration : DEFAULT_TOKEN_EXPIRATION) * 60 * 1000);
		jsonObject.put(SAML_ATTRIBUTES, new JSONObject(attributes));
		return jsonObject.toString();
	}
}
