package org.fogbowcloud.cafe.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.fogbowcloud.cafe.core.saml.SAMLAssertionHolder;
import org.fogbowcloud.cafe.utils.RSAUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opensaml.xml.ConfigurationException;

public class ShibControllerTest {

	private final String PRIVATE_KEY_SUFIX_PATH = "private.key";
	private final String PUBLIC_KEY_SUFIX_PATH = "public.key";
	
	private static final String RESOURCES_PATH = TestHolder.RESOURCES_PATH;
	private static final String ASSERTION_RESPONSE_XML_PATH = RESOURCES_PATH + "/saml_assertion_response.xml";
	
	private String assertionResponse;
	private ShibController shibController;
	private RSAPublicKey publicKey;
	
	@Before
	public void setup() throws IOException, ConfigurationException {
		SAMLAssertionHolder.init();
		
		this.assertionResponse = TestHolder.readFile(ASSERTION_RESPONSE_XML_PATH, Charset.forName(TestHolder.UTF_8));
		this.shibController = Mockito.spy(new ShibController());		
	}
	
	// case test: success case
	@Test
	public void testCreateToken() throws Exception {
		// set up
		String shipIp = "10.10.0.10";
		Properties properties = new Properties();
		properties.put(PropertiesHolder.SHIB_IP_CONF, shipIp);
		PropertiesHolder.setProperties(properties);
		
		String assertionUrl = "http://" + ShibController.DEFAULT_DOMAIN_ASSERTION_URL;
		String assertionUrlExpected = assertionUrl.replace(ShibController.DEFAULT_DOMAIN_ASSERTION_URL, shipIp);
		Map<String, String> attrs = new HashMap<String, String>();
		String userId = "userId";
		String userName = "userName";
		attrs.put(TestHolder.EDU_PERTON_PRINCIPAL_NAME_KEY, userId);
		attrs.put(TestHolder.CN_KEY, userName);
		String assertionAttrsExpected = new JSONObject(attrs).toString();
		String secretExpected = "213567543";
		String tokenExpected = normalizeToken(
				assertionUrlExpected, assertionAttrsExpected, userId, userName, secretExpected);
		
		Mockito.doReturn(this.assertionResponse).when(this.shibController).getAssertionResponse(Mockito.eq(assertionUrl));
		Mockito.doReturn(secretExpected).when(this.shibController).createSecret();
		Mockito.doReturn(attrs).when(this.shibController).getAssertionAttr(Mockito.anyString());
		
		// exercise
		String createToken = this.shibController.createToken(assertionUrl);
		
		// verify
		Assert.assertEquals(tokenExpected, createToken);
	}	
	
	// case test: success case	
	@Test
	public void testCreateTarget() throws Exception {
		// set up		
		String dashboardUrl = "http://10.10.0.10";
		Properties properties = new Properties();
		properties.put(PropertiesHolder.DASHBOARD_URL_CONF, dashboardUrl);
		PropertiesHolder.setProperties(properties);
		
		String rasTokenEncrypted = "rasTokenEncrypted";
		String keyEncrypted = "keyEncrypted";
		String keySigned = "keySigned";
		
		URIBuilder uriBuilder = new URIBuilder(dashboardUrl);
		uriBuilder.addParameter(ShibController.TOKEN_URL_PARAMETER, rasTokenEncrypted);
		uriBuilder.addParameter(ShibController.KEY_URL_PARAMETER, keyEncrypted);
		uriBuilder.addParameter(ShibController.KEY_SIGNATURE_URL_PARAMETER, keySigned);
		String urlExpected = uriBuilder.toString();
		
		// exercise
		String targetUrl = this.shibController.createTargetUrl(rasTokenEncrypted, keyEncrypted, keySigned);
			
		// verify		
		Assert.assertEquals(urlExpected, targetUrl);
	}
	
	// case test: replacing "asserition url" when is a localhost domain
	@Test
	public void testNormalizeAssertionUrl() {
		// set up	
		Properties properties = new Properties();
		String shibIp = "10.10.10.10";
		properties.put(PropertiesHolder.SHIB_IP_CONF, shibIp);
		PropertiesHolder.setProperties(properties);
		
		String urlStr = "http://%s/someshing";
		String assertionUrlExpected = String.format(urlStr, shibIp);
		
		String assertionUrl = String.format(urlStr, ShibController.DEFAULT_DOMAIN_ASSERTION_URL);
		
		// exercise
		String assertionUrlNormilized = this.shibController.normalizeAssertionUrl(assertionUrl);
		
		// verify
		Assert.assertEquals(assertionUrlExpected, assertionUrlNormilized);
	}
	
	// case test: none normalization
	@Test
	public void testNormalizeAssertionUrlWithoutLocalHostDomain() {
		// set up	
		String assertionUrl = "http://10.10.10.10/someshing";
		
		// exercise
		String assertionUrlNormilized = this.shibController.normalizeAssertionUrl(assertionUrl);
		
		// verify
		Assert.assertEquals(assertionUrl, assertionUrlNormilized);
	}	
	
	// case test: success case
	@Test
	public void testEncryptMessage() throws Exception {
		// set up	
		String key = "anything";
		Properties properties = new Properties();
		String rasPublicKeyPath = getResourceFilePath(PUBLIC_KEY_SUFIX_PATH);
		properties.put(PropertiesHolder.RAS_PUBLIC_KEY_PATH_CONF, rasPublicKeyPath);
		PropertiesHolder.setProperties(properties);
				
		// exercise
		String tokenEncrypted = this.shibController.encrypRSAKey(key);
		
		// verify
		String rasPrivateKeyPath = getResourceFilePath(PRIVATE_KEY_SUFIX_PATH);
		RSAPrivateKey rasPrivateKey = this.shibController.getPrivateKey(rasPrivateKeyPath);
		String tokenDecrypted = RSAUtils.decrypt(tokenEncrypted, rasPrivateKey);
		Assert.assertEquals(key, tokenDecrypted);
	}
	
	// case test: wrong public key
	@Test(expected=Exception.class)
	public void testEncryptMessageWrongPublicKey() throws Exception {
		// set up	
		String key = "anything";
		Properties properties = new Properties();
		properties.put(PropertiesHolder.RAS_PUBLIC_KEY_PATH_CONF, "");
		PropertiesHolder.setProperties(properties);
				
		// exercise
		this.shibController.encrypRSAKey(key);
	}	
	
	// case test: success case
	@Test
	public void testSignKey() throws Exception {
		// set up
		String key = "any key";
		Properties properties = new Properties();
		String rasPublicKeyPath = getResourceFilePath(PUBLIC_KEY_SUFIX_PATH);
		String rasPrivateKeyPath = getResourceFilePath(PRIVATE_KEY_SUFIX_PATH);
		properties.put(PropertiesHolder.RAS_PUBLIC_KEY_PATH_CONF, rasPublicKeyPath);
		properties.put(PropertiesHolder.SHIP_PRIVATE_KEY_PATH_CONF, rasPrivateKeyPath);
		PropertiesHolder.setProperties(properties);		

		// exercise
		String keySigned = this.shibController.signKey(key);
		
		// verify
		this.publicKey = this.shibController.getPublicKey(rasPublicKeyPath);
		Assert.assertTrue(RSAUtils.verify(this.publicKey, key, keySigned));
	}
	
	// case test: success case
	@Test
	public void testEncryptEASKey() throws Exception {
		// set up
		String rasTokenExpected = "anything";
		String aesKey = this.shibController.createAESkey();

		// exercise		
		String keyEncrypted = this.shibController.encrypAESRasToken(rasTokenExpected, aesKey);
		
		// verify 
		String rasToken = RSAUtils.decryptAES(aesKey.getBytes(ShibController.UTF_8), keyEncrypted);
		Assert.assertEquals(rasTokenExpected, rasToken);
	}
	
	// case test: invalid key
	@Test(expected=Exception.class)
	public void testEncryptInvalidEASKey() throws Exception {
		// set up
		String rasTokenExpected = "anything";
		String aesKey = "wrong";

		// exercise		
		this.shibController.encrypAESRasToken(rasTokenExpected, aesKey);
	}
	
	private String normalizeToken(String assertionUrl, String assertionAttrsStr,
			String userId, String userName, String secret) {
		String[] parameters = new String[] { 
				secret, 
				assertionUrl,
				userId, 
				userName, 
				assertionAttrsStr };
		String token = StringUtils.join(parameters, ShibController.SHIB_RAS_TOKEN_STRING_SEPARATOR);
		return token;
	}
	
	private String getResourceFilePath(String filename) throws FileNotFoundException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(filename).getFile());
		return file.getAbsolutePath();
	}
	
}
