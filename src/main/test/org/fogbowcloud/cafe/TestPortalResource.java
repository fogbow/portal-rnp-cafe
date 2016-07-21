package org.fogbowcloud.cafe;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicStatusLine;
import org.fogbowcloud.cafe.PortalApplication.DateUtils;
import org.fogbowcloud.cafe.saml.HttpClientWrapper;
import org.fogbowcloud.cafe.saml.HttpResponseWrapper;
import org.fogbowcloud.cafe.saml.SAMLAssertionRetriever;
import org.fogbowcloud.cafe.utils.RSAUtils;
import org.fogbowcloud.cafe.utils.ResourceUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.restlet.Component;
import org.restlet.data.Protocol;

public class TestPortalResource {
	
	private static final String DEFAULT_NONCE = "123";
	private static final String NAME = "fulano";
	private static final int DEFAULT_HTTP_PORT = 8123;
	private static final String DEFAULT_INSTITUTION_UFCG_EDU_BR = "ufcg.edu.br";
	private static final String DEFAULT_FILE_PUBLIC_KEY_PATH = "/tmp/public";
	private static final String DEFAULT_FILE_PRIVATE_KEY_PATH = "/tmp/private";
	private KeyPair keyPair;
	
	private SAMLAssertionRetriever samlAssertionRetriever;
	private HttpClientWrapper httpClientWrapper;
	private PortalApplication portalApplication;
	private Properties properties;
	private DateUtils dateUtils;
	
	@Before
	public void setUp() throws Exception {
		this.properties = new Properties();
		properties.put(DEFAULT_INSTITUTION_UFCG_EDU_BR, "dashboard.ufcg.edu.br");
		properties.put(ResourceUtil.PUBLIC_KEY_PATH_CONF, DEFAULT_FILE_PUBLIC_KEY_PATH);
		properties.put(ResourceUtil.PRIVATE_KEY_PATH_CONF, DEFAULT_FILE_PRIVATE_KEY_PATH);
		
		try {
			this.keyPair = RSAUtils.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			Assert.fail();
		}				
		
		writeKeyToFile(RSAUtils.savePublicKey(this.keyPair.getPublic()), DEFAULT_FILE_PUBLIC_KEY_PATH);			
		writeKeyToFile(RSAUtils.savePrivateKey(this.keyPair.getPrivate()), DEFAULT_FILE_PRIVATE_KEY_PATH);			
		
		this.samlAssertionRetriever = Mockito.mock(SAMLAssertionRetriever.class);
		this.httpClientWrapper = Mockito.mock(HttpClientWrapper.class);
		
		this.dateUtils = Mockito.mock(PortalApplication.DateUtils.class);
		Mockito.when(this.dateUtils.getCurrentTimeMillis()).thenReturn(System.currentTimeMillis());
		
		Component http = new Component();
		http.getServers().add(Protocol.HTTP, DEFAULT_HTTP_PORT);
		this.portalApplication = new PortalApplication(
				properties, samlAssertionRetriever, httpClientWrapper);
		this.portalApplication.setDateUtils(this.dateUtils);
		http.getDefaultHost().attach(this.portalApplication);
		
		http.start();		
	}
	
	@After
	public void tearDown() throws IOException{
		File dbFile = new File(DEFAULT_FILE_PUBLIC_KEY_PATH);
		if (dbFile.exists()) {
			dbFile.delete();
		}
		dbFile = new File(DEFAULT_FILE_PRIVATE_KEY_PATH);
		if (dbFile.exists()) {
			dbFile.delete();
		}		
	}	
	
	@Test
	public void testWorkflowOk() throws Exception {
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put(ResourceUtil.EDU_PERSON_PRINCIPAL_NAME_ASSERTION_ATTRIBUTE, "fulano@" + DEFAULT_INSTITUTION_UFCG_EDU_BR);
		attributes.put(ResourceUtil.SN_ASSERTION_ATTRIBUTES, NAME);
		Mockito.when(this.samlAssertionRetriever.retrieve(Mockito.anyString())).thenReturn(attributes);
		
		HttpResponseWrapper httpResponseWrapper = new HttpResponseWrapper(
				new BasicStatusLine(new ProtocolVersion("", 0, 0), 200, ""), DEFAULT_NONCE);
		Mockito.when(this.httpClientWrapper.doGet(Mockito.anyString())).thenReturn(httpResponseWrapper);
		
		HttpResponseWrapper doGet = new HttpClientWrapper().doGet("http://localhost:" + DEFAULT_HTTP_PORT);

		Map<String, List<String>> queryParams = getQueryParams(doGet.getUrl());
		
		Assert.assertEquals(DEFAULT_NONCE,
				queryParams.get(ResourceUtil.NONCE_PARAMETER).get(0));
		String secretKeyEncrypted = queryParams.get(
				ResourceUtil.SECRET_KEY_ENCRYPTED_PARAMETER).get(0);
		String secretKeyEncryptedSignature = queryParams.get(
				ResourceUtil.SECRET_KEY_SIGNATURE_PARAMETER).get(0);
		Assert.assertTrue(RSAUtils.verify(keyPair.getPublic(),
				secretKeyEncrypted, secretKeyEncryptedSignature));
		Assert.assertTrue(RSAUtils.verify(keyPair.getPublic(), 
				queryParams.get(ResourceUtil.NONCE_PARAMETER).get(0), 
				queryParams.get(ResourceUtil.NONCE_SIGNATURE_PARAMETER).get(0)));
		
		String tokenEncryptedWithSimetric = queryParams.get(
				ResourceUtil.TOKEN_ENCRYPTED_PARAMETER).get(0);		
		
		String secretkey = RSAUtils.decrypt(secretKeyEncrypted, this.keyPair.getPrivate());
		
		String token = RSAUtils.decryptAES(secretkey.getBytes("UTF-8"), tokenEncryptedWithSimetric);
		String createToken = ResourceUtil.createToken(this.portalApplication, attributes);
		
		Assert.assertEquals(token.substring(16), createToken);		
	}
	
	public void writeKeyToFile(String content, String path) throws IOException {
		File file = new File(path);

		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(content);
		bw.close();
	}
	
	public static Map<String, List<String>> getQueryParams(String url) {
	    try {
	        Map<String, List<String>> params = new HashMap<String, List<String>>();
	        String[] urlParts = url.split("\\?");
	        if (urlParts.length > 1) {
	            String query = urlParts[1];
	            for (String param : query.split("&")) {
	                String[] pair = param.split("=");
	                String key = URLDecoder.decode(pair[0], "UTF-8");
	                String value = "";
	                if (pair.length > 1) {
	                    value = URLDecoder.decode(pair[1], "UTF-8");
	                }

	                List<String> values = params.get(key);
	                if (values == null) {
	                    values = new ArrayList<String>();
	                    params.put(key, values);
	                }
	                values.add(value);
	            }
	        }

	        return params;
	    } catch (UnsupportedEncodingException ex) {
	        throw new AssertionError(ex);
	    }
	}	
	
}
