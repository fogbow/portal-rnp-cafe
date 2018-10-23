package org.fogbowcloud.shipapp.core.models;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.fogbowcloud.shipapp.core.ShibController;
import org.fogbowcloud.shipapp.core.saml.SAMLAssertionHolder;
import org.json.JSONObject;

public class ShibToken {

	protected final static int MAXIMUM_TOKEN_STRING_SIZE = 1000;
	private static final String SHIB_RAS_TOKEN_STRING_SEPARATOR = ShibController.SHIB_RAS_TOKEN_STRING_SEPARATOR;
	
	// The secret is the creation time
	private String secret;
	private String assertionUrl;
	private String userId;
	private String userName;
	private Map<String, String> samlAttributes;

	public ShibToken(String secret, String assertionUrl, String userId, String userName,
			Map<String, String> samlAttributes) {
		super();
		this.secret = secret;
		this.assertionUrl = assertionUrl;
		this.userId = userId;
		this.userName = userName;
		this.samlAttributes = samlAttributes;
	}

	public String generateTokenStr() {
		String mainShibTokenAttributes = mainShibTokenAttributesSize();
		
		String samlAttributes = getSamlAttributes(mainShibTokenAttributes);
		
		return generateTokenStr(mainShibTokenAttributes, samlAttributes);
	}

	protected String getSamlAttributes(String mainShibTokenAttributes) {
		int maximumTokenStr = getMaximumTokenStr(mainShibTokenAttributes);
		
		String samlAttributes = null;
		boolean hasMoreSpace = maximumTokenStr >= 0;
		if (!hasMoreSpace) {
			samlAttributes = createEmptySamlAttribute(); 
		} else {
			samlAttributes = prioritizeSamlAttributes(this.samlAttributes, maximumTokenStr);
		}
		
		return samlAttributes;
	}

	protected String prioritizeSamlAttributes(Map<String, String> samlAttributes, int maximumTokenStr) {
		if (samlAttributes == null || maximumTokenStr <= 0) {
			return createEmptySamlAttribute();
		}
		
		String samlAttributesStr = new JSONObject(samlAttributes).toString();
		if (samlAttributesStr.length() > maximumTokenStr) {
			samlAttributes = reduceSamlAttribute(samlAttributes);			
			return prioritizeSamlAttributes(samlAttributes, maximumTokenStr);
		}

		return samlAttributesStr;
	}

	private Map<String, String> reduceSamlAttribute(Map<String, String> samlAttributes) {
		for (String key : samlAttributes.keySet()) {
			if (SAMLAssertionHolder.isPriorityAssertionAttribute(key)) {
				continue;
			}
			
			samlAttributes.remove(key);
			return samlAttributes;
		}
		return null;
		
	}

	protected String createEmptySamlAttribute() {
		return new JSONObject(new HashMap<String, String>()).toString();
	}

	private int getMaximumTokenStr(String samlAttributes) {
		return MAXIMUM_TOKEN_STRING_SIZE - samlAttributes.length(); 
	}

	private String mainShibTokenAttributesSize() {
		String[] mainAttributes = new String[] {
				this.secret, 
				this.assertionUrl, 
				this.userId, 
				this.userName };

		return StringUtils.join(mainAttributes, SHIB_RAS_TOKEN_STRING_SEPARATOR);
 	}
	
	private String generateTokenStr(String mainShibTokenAttributes, String samlAttributes) {
		String[] tokenStr = new String[] {
				mainShibTokenAttributes, 
				samlAttributes };
		
		return StringUtils.join(tokenStr, SHIB_RAS_TOKEN_STRING_SEPARATOR);
	}
	
}
