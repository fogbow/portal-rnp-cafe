package org.fogbowcloud.cafe;

import org.apache.log4j.Logger;
import org.fogbowcloud.cafe.core.PropertiesHolder;
import org.fogbowcloud.cafe.core.saml.SAMLAssertionHolder;
import org.opensaml.xml.ConfigurationException;
import org.restlet.Component;
import org.restlet.data.Protocol;

public class Main {
	
	private static final Logger LOGGER = Logger.getLogger(Main.class);
	private static final int EXIT = 1;
	
	// TODO improve this implementation
	public static void main(String[] args) {
		String propertiePath = args[0];
		initProperties(propertiePath);
		
		initSAMLAssertion();
		
		startHTTPServer();
	}

	private static void startHTTPServer() {
		try {
			Component http = new Component();		
			int httpPort = PropertiesHolder.getShipHttpPort();
			http.getServers().add(Protocol.HTTP, httpPort);
			http.getDefaultHost().attach(new ShibApplication());
			http.start();
		} catch (Exception e) {
			// TODO add message
			String msgError = "";
			LOGGER.fatal(msgError, e);
			System.exit(1);
		}
	}

	private static void initProperties(String propertiePath) {
		try {
			PropertiesHolder.init(propertiePath);
		} catch (Exception e) {
			// TODO add message
			String msgError = "";
			LOGGER.fatal(msgError, e);
			System.exit(1);
		}
	}

	private static void initSAMLAssertion() {
		try {
			SAMLAssertionHolder.init();
		} catch (ConfigurationException e) {
			// TODO add message			
			String msgError = "";
			LOGGER.fatal(msgError, e);
			System.exit(EXIT);
		}
	}

}
