package org.fogbowcloud.shipapp;

import org.apache.log4j.Logger;
import org.fogbowcloud.shipapp.core.PropertiesHolder;
import org.fogbowcloud.shipapp.core.saml.SAMLAssertionHolder;
import org.opensaml.xml.ConfigurationException;
import org.restlet.Component;
import org.restlet.data.Protocol;

public class Main {
	
	private static final Logger LOGGER = Logger.getLogger(Main.class);
	private static final int EXIT = 1;
	
	public static void main(String[] args) {
		String propertiesPath = args[0];
		
		initProperties(propertiesPath);		
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
			String msgError = "Was not possible start HTTP server";
			LOGGER.fatal(msgError, e);
			System.exit(EXIT);
		}
	}

	private static void initProperties(String propertiePath) {
		try {
			PropertiesHolder.init(propertiePath);
		} catch (Exception e) {
			String msgError = "Was not possible get properties";
			LOGGER.fatal(msgError, e);
			System.exit(EXIT);
		}
	}

	private static void initSAMLAssertion() {
		try {
			SAMLAssertionHolder.init();
		} catch (ConfigurationException e) {
			String msgError = "Was not possible initialize SAMLAssertionHolder";
			LOGGER.fatal(msgError, e);
			System.exit(EXIT);
		}
	}

}
