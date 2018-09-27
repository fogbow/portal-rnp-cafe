package org.fogbowcloud.cafe.core;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Properties;

import org.apache.http.client.utils.URIBuilder;
import org.fogbowcloud.cafe.core.saml.SAMLAssertionHolder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opensaml.xml.ConfigurationException;


public class ShibControllerTest {

	private static final String RESOURCES_PATH = TestHolder.RESOURCES_PATH;
	private static final String ASSERTION_RESPONSE_XML_PATH = RESOURCES_PATH + "/saml_assertion_response.xml";
	
	private String assertionResponse;
	private ShibController shibController;
	
	@Before
	public void setup() throws IOException, ConfigurationException {
		SAMLAssertionHolder.init();
		
		this.assertionResponse = TestHolder.readFile(ASSERTION_RESPONSE_XML_PATH, Charset.forName(TestHolder.UTF_8));
		this.shibController = Mockito.spy(new ShibController());		
	}
	
	// case test: success case
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateToken() throws Exception {
		// set up
		String shipIp = "10.10.0.10";
		Properties properties = new Properties();
		properties.put(PropertiesHolder.SHIB_IP_CONF, shipIp);
		PropertiesHolder.setProperties(properties);
		
		String assertionUrl = "http://" + ShibController.DEFAULT_DOMAIN_ASSERTION_URL;
		String assertionUrlExpected = assertionUrl.replace(ShibController.DEFAULT_DOMAIN_ASSERTION_URL, shipIp);
		String assertionAttrsExpected = "[]";
		String userId = TestHolder.EDU_PERTON_PRINCIPAL_NAME_VALUE;
		String userName = TestHolder.CN_VALUE;
		String secretExpected = "213567543";
		String identityProvider = TestHolder.ISSUER_VALUE;
		String tokenExpected = this.shibController.normalizeToken(
				assertionUrlExpected, assertionAttrsExpected, userId, userName, secretExpected, identityProvider);
		
		Mockito.doReturn(this.assertionResponse).when(this.shibController).getAssertionResponse(Mockito.eq(assertionUrl));
		Mockito.doReturn(assertionAttrsExpected).when(this.shibController).normalizeAssertionAttrs(Mockito.any(HashMap.class));
		Mockito.doReturn(secretExpected).when(this.shibController).createSecret();
		
		// exercise
		String createToken = this.shibController.createToken(assertionUrl);
		
		// verify
		Assert.assertTrue(tokenExpected.equals(createToken));
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
		String rasTokenSigned = "rasTokenSigned";
		
		URIBuilder uriBuilder = new URIBuilder(dashboardUrl);
		uriBuilder.addParameter(ShibController.TOKEN_URL_PARAMETER, rasTokenEncrypted);
		uriBuilder.addParameter(ShibController.SIGNATURE_URL_PARAMETER, rasTokenSigned);
		String urlExpected = uriBuilder.toString();
		
		// exercise
		String targetUrl = this.shibController.createTargetUrl(rasTokenEncrypted, rasTokenSigned);
			
		// verify		
		Assert.assertEquals(urlExpected, targetUrl);
	}
	
}
