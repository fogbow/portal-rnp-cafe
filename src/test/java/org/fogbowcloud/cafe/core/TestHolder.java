package org.fogbowcloud.cafe.core;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.fogbowcloud.cafe.core.saml.SAMLAssertionHolder;

public class TestHolder {

	public static final String EDU_PERTON_PRINCIPAL_NAME_KEY = SAMLAssertionHolder.EDU_PERSON_PRINCIPAL_NAME_ASSERTION_ATTRIBUTE;
	public static final String EDU_PERTON_PRINCIPAL_NAME_VALUE = "fulano@lsd.ufcg.edu.br";		
	public static final String EDU_PERTON_ENTITLEMENT_KEY = "eduPersonEntitlement";
	public static final String EDU_PERTON_ENTITLEMENT_VALUE = "wiki:tfemc2";
	public static final String EDU_PERTON_TARGET_KEY = "eduPersonTargetedID";
	public static final String EDU_PERTON_TARGET_VALUE = "jFHk=";
	public static final String GIVEN_NAME_KEY = "givenName";
	public static final String GIVEN_NAME_VALUE = "Fulano";	
	public static final String CN_KEY = SAMLAssertionHolder.CN_ASSERTION_ATTRIBUTES;
	public static final String CN_VALUE = "FulanoN";
	public static final String MAIL_KEY = "mail";
	public static final String MAIL_VALUE = "fulano@lsd.ufcg.edu.br";
	public static final String SN_KEY = SAMLAssertionHolder.SN_ASSERTION_ATTRIBUTES;
	public static final String SN_VALUE = "Nick";
	
	public static final String ISSUER_KEY = SAMLAssertionHolder.IDENTITY_PROVIDER_ASSERTION_ATTRIBUTES;	
	public static final String ISSUER_VALUE = "https://idp-federation/idp/shibboleth";
	
	public static final String UTF_8 = "UTF-8";
	public static final String RESOURCES_PATH = "src/test/resources";
	
	public static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
	
}
