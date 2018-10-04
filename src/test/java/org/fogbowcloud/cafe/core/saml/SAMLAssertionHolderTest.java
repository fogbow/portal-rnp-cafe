package org.fogbowcloud.cafe.core.saml;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.fogbowcloud.cafe.core.TestHolder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.xml.ConfigurationException;

public class SAMLAssertionHolderTest {

	private static final String RESOURCES_PATH = TestHolder.RESOURCES_PATH;
	private static final String WRONG_ASSERTION_RESPONSE_XML_PATH = RESOURCES_PATH + "/wrong_saml_assertion_response.xml";
	private static final String ASSERTION_RESPONSE_XML_PATH = RESOURCES_PATH + "/saml_assertion_response.xml";
	
	private String assertionResponse;
	private String wrongAssertionResponse;
	private HashMap<String, String> assertionAttrsExcepted;

	@Before
	public void setUp() throws ConfigurationException, IOException {
		SAMLAssertionHolder.init();
		this.assertionResponse = TestHolder.readFile(ASSERTION_RESPONSE_XML_PATH, Charset.forName(TestHolder.UTF_8));
		this.wrongAssertionResponse = TestHolder.readFile(WRONG_ASSERTION_RESPONSE_XML_PATH, Charset.forName(TestHolder.UTF_8));
		
		// Attributes in the saml_assertion_response.xml
		this.assertionAttrsExcepted = new HashMap<String, String>();
		this.assertionAttrsExcepted.put(TestHolder.EDU_PERTON_ENTITLEMENT_KEY, TestHolder.EDU_PERTON_ENTITLEMENT_VALUE);
		this.assertionAttrsExcepted.put(TestHolder.CN_KEY, TestHolder.CN_VALUE);
		this.assertionAttrsExcepted.put(TestHolder.MAIL_KEY, TestHolder.MAIL_VALUE);
		this.assertionAttrsExcepted.put(TestHolder.SN_KEY, TestHolder.SN_VALUE);
		this.assertionAttrsExcepted.put(TestHolder.EDU_PERTON_TARGET_KEY, TestHolder.EDU_PERTON_TARGET_VALUE);
		this.assertionAttrsExcepted.put(TestHolder.GIVEN_NAME_KEY, TestHolder.GIVEN_NAME_VALUE);
		this.assertionAttrsExcepted.put(TestHolder.EDU_PERTON_PRINCIPAL_NAME_KEY, TestHolder.EDU_PERTON_PRINCIPAL_NAME_VALUE);
		this.assertionAttrsExcepted.put(TestHolder.ISSUER_KEY, TestHolder.ISSUER_VALUE);
		
	}
	
	//test case: get assertion attributes is a success
	@Test
	public void testGetAssertionAttrs() throws Exception {
		// exercise
		Map<String, String> assertionAttrs = SAMLAssertionHolder.getAssertionAttrs(this.assertionResponse);
		
		// verify
		Assert.assertTrue(this.assertionAttrsExcepted.equals(assertionAttrs));
	}
	
	//test case: get assertion attributes is a success with out issuer
	@Test
	public void testGetAssertionAttrsWithoutIssuer() throws Exception {
		// set up
		this.assertionAttrsExcepted.remove(TestHolder.ISSUER_KEY);
		
		// exercise
		Map<String, String> assertionAttrs = SAMLAssertionHolder.getAssertionAttrs(this.wrongAssertionResponse);
		
		// verify
		Assert.assertTrue(this.assertionAttrsExcepted.equals(assertionAttrs));
	}	
	
	//test case: success case
	@Test
	public void testIsMainAssertionAttribute() {
		// exercise and verify
		Assert.assertTrue(SAMLAssertionHolder
				.isMainAssertionAttribute(SAMLAssertionHolder.EDU_PERSON_PRINCIPAL_NAME_ASSERTION_ATTRIBUTE));
		Assert.assertTrue(SAMLAssertionHolder
				.isMainAssertionAttribute(SAMLAssertionHolder.SN_ASSERTION_ATTRIBUTES));
		Assert.assertTrue(SAMLAssertionHolder
				.isMainAssertionAttribute(SAMLAssertionHolder.CN_ASSERTION_ATTRIBUTES));
		Assert.assertTrue(SAMLAssertionHolder
				.isMainAssertionAttribute(SAMLAssertionHolder.IDENTITY_PROVIDER_ASSERTION_ATTRIBUTES));
	}
	
	//test case: is not a main assertion attribute
	@Test
	public void testIsNotMainAssertionAttribute() {
		// exercise and verify
		Assert.assertFalse(SAMLAssertionHolder.isMainAssertionAttribute("anything"));
	}
	
}
