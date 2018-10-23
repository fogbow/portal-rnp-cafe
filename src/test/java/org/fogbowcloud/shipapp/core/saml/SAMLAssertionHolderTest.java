package org.fogbowcloud.shipapp.core.saml;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicStatusLine;
import org.fogbowcloud.shipapp.core.TestHolder;
import org.fogbowcloud.shipapp.utils.HttpClientWrapper;
import org.fogbowcloud.shipapp.utils.HttpResponseWrapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
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
	
	// test case: get assertion attributes is a success
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
	
	// test case: success case
	@Test
	public void testIsMainAssertionAttribute() {
		// exercise and verify
		Assert.assertTrue(SAMLAssertionHolder
				.isPriorityAssertionAttribute(SAMLAssertionHolder.SN_ASSERTION_ATTRIBUTES));
		Assert.assertTrue(SAMLAssertionHolder
				.isPriorityAssertionAttribute(SAMLAssertionHolder.IDENTITY_PROVIDER_ASSERTION_ATTRIBUTES));
	}
	
	// test case: is not a main assertion attribute
	@Test
	public void testIsNotMainAssertionAttribute() {
		// exercise and verify
		Assert.assertFalse(SAMLAssertionHolder.isPriorityAssertionAttribute("anything"));
	}
	
	// test case: success case
	@Test
	public void testGetAssertionResponse() throws Exception {
		// setup 
		String assertionURL = "";
		HttpClientWrapper httpClientWrapper = Mockito.mock(HttpClientWrapper.class);
		String content = "content";
		BasicStatusLine statusLine = new BasicStatusLine(new ProtocolVersion("", 1, 1), HttpStatus.SC_OK, "");
		HttpResponseWrapper httpResponseWrapper = new HttpResponseWrapper(statusLine, content);
		Mockito.when(httpClientWrapper.doGet(Mockito.eq(assertionURL))).thenReturn(httpResponseWrapper);
		
		// exercise
		String contentReturned = SAMLAssertionHolder.getAssertionResponse(assertionURL, httpClientWrapper);
				
		// verify
		Assert.assertEquals(content, contentReturned);
	}
	
	// test case: the status code returned is diferente of 200 (OK)
	@Test(expected=Exception.class)
	public void testGetAssertionResponseStatusCodeNotOk() throws Exception {
		// setup 
		String assertionURL = "";
		HttpClientWrapper httpClientWrapper = Mockito.mock(HttpClientWrapper.class);
		String content = "content";
		BasicStatusLine statusLine = new BasicStatusLine(new ProtocolVersion("", 1, 1), HttpStatus.SC_BAD_REQUEST, "");
		HttpResponseWrapper httpResponseWrapper = new HttpResponseWrapper(statusLine, content);
		Mockito.when(httpClientWrapper.doGet(Mockito.eq(assertionURL))).thenReturn(httpResponseWrapper);
		
		// exercise
		SAMLAssertionHolder.getAssertionResponse(assertionURL, httpClientWrapper);
	}	
	
}
