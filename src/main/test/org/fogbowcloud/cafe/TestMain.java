package org.fogbowcloud.cafe;

import java.util.Properties;

import org.fogbowcloud.cafe.utils.ResourceUtil;
import org.junit.Test;

public class TestMain {
	
	@Test
	public void testCheckProperties() {
		Properties properties = new Properties();
		properties.put(ResourceUtil.PRIVATE_KEY_PATH_CONF, "/path");
		properties.put(ResourceUtil.PUBLIC_KEY_PATH_CONF, "/path");
		Main.checkProperties(properties);
	}
	
	@Test(expected=RuntimeException.class)
	public void testCheckPropertiesWithoutPrivateKey() {
		Properties properties = new Properties();
		properties.put(ResourceUtil.PUBLIC_KEY_PATH_CONF, "/path");
		Main.checkProperties(properties);
	}
	
	@Test(expected=RuntimeException.class)
	public void testCheckPropertiesWithoutPublicKey() {
		Properties properties = new Properties();
		properties.put(ResourceUtil.PRIVATE_KEY_PATH_CONF, "/path");
		Main.checkProperties(properties);
	}	
	
}
