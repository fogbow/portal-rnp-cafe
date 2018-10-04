package org.fogbowcloud.cafe.core.models;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.fogbowcloud.cafe.core.ShibController;
import org.fogbowcloud.cafe.core.saml.SAMLAssertionHolder;
import org.json.JSONObject;
import org.junit.Test;

import junit.framework.Assert;

public class ShibTokenTest {

	// case : success case
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
	
	// case : many attributes that pass of limit and one without priority is removed
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
