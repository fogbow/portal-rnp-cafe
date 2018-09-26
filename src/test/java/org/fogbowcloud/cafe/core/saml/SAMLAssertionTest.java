package org.fogbowcloud.cafe.core.saml;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.opensaml.xml.ConfigurationException;

import junit.framework.Assert;

public class SAMLAssertionTest {

	private static final String RESOURCES_PATH = "src/test/resources";
	private static final String WRONG_ASSERTION_RESPONSE_XML_PATH = RESOURCES_PATH + "/wrong_saml_assertion_response.xml";
	private static final String ASSERTION_RESPONSE_XML_PATH = RESOURCES_PATH + "/saml_assertion_response.xml";
	private static final String ASSERTION_ISSUER_XML = "https://idp-federation/idp/shibboleth";
	private static final String UTF_8 = "UTF-8";
	
	private String assertionResponse;
	private String wrongAssertionResponse;
	private HashMap<String, String> assertionAttrsExcepted;

	@Before
	public void setUp() throws ConfigurationException, IOException {
		SAMLAssertionHolder.init();
		this.assertionResponse = readFile(ASSERTION_RESPONSE_XML_PATH, Charset.forName(UTF_8));
		this.wrongAssertionResponse = readFile(WRONG_ASSERTION_RESPONSE_XML_PATH, Charset.forName(UTF_8));
		
		// TODO use constants
		// Attributes in the saml_assertion_response.xml
		this.assertionAttrsExcepted = new HashMap<String, String>();
		this.assertionAttrsExcepted.put("eduPersonEntitlement", "wiki:tfemc2");
		this.assertionAttrsExcepted.put("cn", "FulanoN");
		this.assertionAttrsExcepted.put("mail", "fulano@lsd.ufcg.edu.br");
		this.assertionAttrsExcepted.put("sn", "Nick");
		this.assertionAttrsExcepted.put("eduPersonTargetedID", "jFHk=");
		this.assertionAttrsExcepted.put("givenName", "Fulano");
		this.assertionAttrsExcepted.put("eduPersonPrincipalName", "fulano@lsd.ufcg.edu.br");
	}
	
	//test case: get assertion attributes is a success
	@Test
	public void testGetAssertionAttrs() throws Exception {
		// exercise
		Map<String, String> assertionAttrs = SAMLAssertionHolder.getAssertionAttrs(this.assertionResponse);
		
		// verify
		Assert.assertTrue(this.assertionAttrsExcepted.equals(assertionAttrs));
	}
	
	//test case: get issuer is a success
	@Test
	public void testGetIssuer() throws Exception {
		// exercise
		String issuer = SAMLAssertionHolder.getIssuer(this.assertionResponse);
		
		// verify
		Assert.assertEquals(ASSERTION_ISSUER_XML, issuer);
	}
	
	//test case: the assertion does not have the issuer
	@Test(expected=Exception.class)
	public void testGetIssuerAndNotFound() throws Exception {
		// exercise
		SAMLAssertionHolder.getIssuer(this.wrongAssertionResponse);
	}	

	private static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
	
}
