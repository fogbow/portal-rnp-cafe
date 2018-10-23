package org.fogbowcloud.shipapp.core.models;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.fogbowcloud.shipapp.core.ShibController;
import org.fogbowcloud.shipapp.core.models.ShibToken;
import org.fogbowcloud.shipapp.core.saml.SAMLAssertionHolder;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;

import junit.framework.Assert;

public class ShibTokenTest {

	// test case : success case
	@Test
	public void testGenerateTokenStr() {
		// set up
		String secret = "";
		String assertionUrl = "";
		String userId = "";
		String userName = "";
		Map<String, String> samlAttributes = new HashMap<String, String>();
		String tokenStrExcepected = createTokenStr(secret, assertionUrl, userId, userName, samlAttributes);
		
		// exercise		
		ShibToken shibToken = new ShibToken(secret, assertionUrl, userId, userName, samlAttributes);
		
		// verify
		Assert.assertEquals(tokenStrExcepected, shibToken.generateTokenStr());
	}
	
	// case : one attribute too large that pass of limit
	@Test
	public void testGenerateTokenStrOneAttributeToLarge() {
		// set up
		String secret = "";
		String assertionUrl = "";
		String userId = "";
		String userName = "";
		Map<String, String> samlAttributesExpected = new HashMap<String, String>();
		String tokenStrExcepected = createTokenStr(secret, assertionUrl, userId, userName, samlAttributesExpected);
		
		Map<String, String> samlAttributes = new HashMap<String, String>();
		String stringToLarge = StringUtils.repeat("*", ShibToken.MAXIMUM_TOKEN_STRING_SIZE + 10);
		samlAttributes.put("", stringToLarge);
		// exercise		
		ShibToken shibToken = new ShibToken(secret, assertionUrl, userId, userName, samlAttributes);
		
		// verify
		Assert.assertEquals(tokenStrExcepected, shibToken.generateTokenStr());
	}
	
	// test case : many attributes that pass of limit and one without priority is removed
	@Test
	public void testGenerateTokenStrThreeAttributeToLarge() {
		// set up
		String secret = "";
		String assertionUrl = "";
		String userId = "";
		String userName = "";
		
		Map<String, String> samlAttributes = new HashMap<String, String>();
		int oneThirdOfMaximumSize = ShibToken.MAXIMUM_TOKEN_STRING_SIZE / 3;
		String oneThird = StringUtils.repeat("*", oneThirdOfMaximumSize);
		samlAttributes.put(SAMLAssertionHolder.EDU_PERSON_PRINCIPAL_NAME_ASSERTION_ATTRIBUTE, "");
		samlAttributes.put(SAMLAssertionHolder.SN_ASSERTION_ATTRIBUTES, "");
		samlAttributes.put(SAMLAssertionHolder.CN_ASSERTION_ATTRIBUTES, "");
		String elementRemoved = "1";
		samlAttributes.put(elementRemoved, oneThird);
		samlAttributes.put("2", oneThird);
		samlAttributes.put("3", oneThird);
		
		Map<String, String> samlAttributesExpected = new HashMap<String, String>(samlAttributes);
		samlAttributesExpected.remove(elementRemoved);
		String tokenStrExcepected = createTokenStr(secret, assertionUrl, userId, userName, samlAttributesExpected);
		
		// exercise		
		ShibToken shibToken = new ShibToken(secret, assertionUrl, userId, userName, samlAttributes);
		
		// verify
		Assert.assertEquals(tokenStrExcepected, shibToken.generateTokenStr());
	}	
	
	// test case : main ShibToken attributes biggest than maximum 
	@SuppressWarnings("unchecked")
	@Test
	public void testGetSamlAttributesTooLagerShibTokenAttrs() {
		// set up
		HashMap<String, String> samlAttributes = new HashMap<String, String>();
		ShibToken shibToken = Mockito.spy(new ShibToken("", "", "", "", samlAttributes));		
		String mainShibTokenAttributes = StringUtils.repeat("*", ShibToken.MAXIMUM_TOKEN_STRING_SIZE + 1);
		
		// exercise
		String samlAttributesStr = shibToken.getSamlAttributes(mainShibTokenAttributes);
		
		// verify		
		Mockito.verify(shibToken, Mockito.times(1)).createEmptySamlAttribute();
		Mockito.verify(shibToken, Mockito.never()).prioritizeSamlAttributes(Mockito.anyMap(), Mockito.anyInt());
		Assert.assertEquals(new JSONObject(samlAttributes).toString(), samlAttributesStr);
	}
	
	// test case : main ShibToken attributes smallest than maximum and try to priorize the saml attrs 
	@SuppressWarnings("unchecked")
	@Test
	public void testGetSamlAttributes() {
		// set up
		String string50PercentMaximumSize = StringUtils.repeat("*", ShibToken.MAXIMUM_TOKEN_STRING_SIZE / 2);
		String string25PercentMaximumSize = StringUtils.repeat("*", ShibToken.MAXIMUM_TOKEN_STRING_SIZE / 4);
				
		String keyOneWillBeRemoved = "1";
		String keyTwoWillBeRemoved = "2";
		String keyNeverRemoved = SAMLAssertionHolder.SN_ASSERTION_ATTRIBUTES;
		HashMap<String, String> samlAttributes = new HashMap<String, String>();
		samlAttributes.put(keyNeverRemoved, string50PercentMaximumSize);
		samlAttributes.put(keyOneWillBeRemoved, string50PercentMaximumSize);
		samlAttributes.put(keyTwoWillBeRemoved, string50PercentMaximumSize);
		samlAttributes.put("3", string50PercentMaximumSize);
		
		ShibToken shibToken = Mockito.spy(new ShibToken("", "", "", "", samlAttributes));		
		String mainShibTokenAttributes = string25PercentMaximumSize;;
		
		// exercise
		String samlAttributesStr = shibToken.getSamlAttributes(mainShibTokenAttributes);
		
		// verify		
		Mockito.verify(shibToken, Mockito.never()).createEmptySamlAttribute();
		Mockito.verify(shibToken, Mockito.times(4)).prioritizeSamlAttributes(Mockito.anyMap(), Mockito.anyInt());
		
		// keys removed in process
		samlAttributes.remove(keyOneWillBeRemoved);
		samlAttributes.remove(keyTwoWillBeRemoved);
		Assert.assertEquals(new JSONObject(samlAttributes).toString(), samlAttributesStr);
	}	
	
	// test case : main ShibToken attributes smallest than maximum and return empty saml attributes because the size of the important saml attribute
	@SuppressWarnings("unchecked")
	@Test
	public void testGetSamlAttributesEmptySamlAttributeBecause() {
		// set up
		String string200PercentMaximumSize = StringUtils.repeat("*", ShibToken.MAXIMUM_TOKEN_STRING_SIZE * 2);
				
		String keyNeverRemoved = SAMLAssertionHolder.SN_ASSERTION_ATTRIBUTES;
		HashMap<String, String> samlAttributes = new HashMap<String, String>();
		samlAttributes.put(keyNeverRemoved, string200PercentMaximumSize);
		
		ShibToken shibToken = Mockito.spy(new ShibToken("", "", "", "", samlAttributes));		
		
		// exercise
		String samlAttributesStr = shibToken.getSamlAttributes("");
		
		// verify		
		Mockito.verify(shibToken, Mockito.times(1)).createEmptySamlAttribute();
		Mockito.verify(shibToken, Mockito.times(2)).prioritizeSamlAttributes(Mockito.anyMap(), Mockito.anyInt());
		
		// keys removed in process
		Assert.assertEquals(new JSONObject(new HashMap<String, String>()).toString(), samlAttributesStr);
	}		
	
	private String createTokenStr(String secret, String assertionUrl, String userId, String userName, Map<String, String> samlAttributes) {
		String samlAttributeStr = new JSONObject(samlAttributes).toString();
		samlAttributeStr.toString();
		String[] attributes = new String[] {
				secret, 
				assertionUrl, 
				userId, 
				userName,
				samlAttributeStr};

		return StringUtils.join(attributes, ShibController.SHIB_RAS_TOKEN_STRING_SEPARATOR);
	}
	
}
