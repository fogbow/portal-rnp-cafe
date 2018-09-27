package org.fogbowcloud.cafe.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.xml.bind.PropertyException;

public class PropertiesHolder {

	private static final int DEFAULT_HTTP_PORT = 8000;
	
	public static final String DASHBOARD_URL_CONF = "dashboard_url";
	private static final String RAS_PUBLIC_KEY_PATH_CONF = "ras_public_key_path";
	private static final String SHIP_PRIVATE_KEY_PATH_CONF = "ship_private_key_path";
	private static final String SHIB_HTTP_PORT_CONF = "shib_http_port";
	public static final String SHIB_IP_CONF = "shib_ip";
	
	public static Properties properties;
	
	public static void init(String propertiePath) throws IOException, PropertyException {
		File file = new File(propertiePath);
		FileInputStream fileInputStream = new FileInputStream(file);
		properties.load(fileInputStream);
		
		checkProperties(properties);
	}
	
	public static void setProperties(Properties properties) {
		PropertiesHolder.properties = properties;
	}

	// TODO implement tests
	// TODO check other properties - check real properties ?
	protected static void checkProperties(Properties properties) throws PropertyException {
		if (getShibPrivateKey() == null) {
			throw new PropertyException("Ship App private key not especified in the properties.");
		}
		
		if (getRasPublicKey() == null) {
			throw new PropertyException("RAS public key not especified in the properties.");
		}
		
		if (getDashboardUrl() == null) {
			throw new PropertyException("Dashboard RAS url not especified in the properties.");
		}
		
		if (getShibIp() == null) {
			throw new PropertyException("Shib machine ip not especified in the properties.");
		}
	}

	// TODO implement tests
	public static int getShipHttpPort() {
		String httpPortStr = properties.getProperty(SHIB_HTTP_PORT_CONF);
		int port = httpPortStr == null ? DEFAULT_HTTP_PORT : Integer.parseInt(httpPortStr);
		return port;
	}
	
	public static String getDashboardUrl() {
		return properties.getProperty(DASHBOARD_URL_CONF);
	}
	
	public static String getRasPublicKey() {
		return properties.getProperty(RAS_PUBLIC_KEY_PATH_CONF);
	}
	
	public static String getShibPrivateKey() {
		return properties.getProperty(SHIP_PRIVATE_KEY_PATH_CONF);
	}
	
	public static String getShibIp() {
		return properties.getProperty(SHIB_IP_CONF);
	}	
	
}
