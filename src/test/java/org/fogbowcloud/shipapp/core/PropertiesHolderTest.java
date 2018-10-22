package org.fogbowcloud.shipapp.core;

import java.io.IOException;

import javax.xml.bind.PropertyException;

import org.junit.Test;

import junit.framework.Assert;

public class PropertiesHolderTest {

	private static final String RESOURCES_PATH = TestHolder.RESOURCES_PATH;
	private static final String PROPERTIES_CONF_PATH = RESOURCES_PATH + "/properties.conf";
	private static final String PROPERTIES_CONF_WITHOUT_HTTPPORT_PATH = RESOURCES_PATH + "/properties-without-http-port.conf";
	
	// case test: success case. Initializing the properties holder class with success
	@Test
	public void testInit() throws PropertyException, IOException {
		// exercise
		PropertiesHolder.init(PROPERTIES_CONF_PATH);

		// verify 		
		Assert.assertEquals(TestHolder.SHIB_IP, PropertiesHolder.getShibIp());
		Assert.assertEquals(TestHolder.DASHBOARD_URL, PropertiesHolder.getDashboardUrl());
		Assert.assertEquals(TestHolder.SHIB_PRIVATE_KEY_PATH, PropertiesHolder.getShibPrivateKey());
		Assert.assertEquals(TestHolder.RAS_PUBLIC_KEY_PATH, PropertiesHolder.getRasPublicKey());
	}
	
	// case test: get shib HTTP port in the properties
	@Test
	public void testGetShipHttpPort() throws PropertyException, IOException {
		PropertiesHolder.init(PROPERTIES_CONF_PATH);
		
		// exercise
		int shipHttpPort = PropertiesHolder.getShipHttpPort();
		
		// verify
		Assert.assertEquals(TestHolder.SHIB_HTTP_PORT, shipHttpPort);
	}	

	// case test: getting default shib HTTP port, because there is not the propertie setted in the file properties
	@Test
	public void testGetShipHttpPortDefault() throws PropertyException, IOException {
		PropertiesHolder.init(PROPERTIES_CONF_WITHOUT_HTTPPORT_PATH);
		
		// exercise
		int shipHttpPort = PropertiesHolder.getShipHttpPort();
		
		// verify
		Assert.assertEquals(PropertiesHolder.DEFAULT_HTTP_PORT, shipHttpPort);
	}
	
	// TODO implements test to this methods: checkProperties
	
}
