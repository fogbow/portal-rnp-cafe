package org.fogbowcloud.cafe;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.restlet.Component;
import org.restlet.data.Protocol;

public class Main {

	private static final int DEFAULT_HTTP_PORT = 8000;

	public static void main(String[] args) throws Exception {
		Properties properties = new Properties();
		properties.load(new FileInputStream(new File(args[0])));
		String httpPortStr = properties.getProperty("http_port");
		
		Component http = new Component();
		http.getServers().add(
				Protocol.HTTP,
				httpPortStr == null ? DEFAULT_HTTP_PORT : Integer
						.parseInt(httpPortStr));
		http.getDefaultHost().attach(new PortalApplication(properties));
		http.start();
	}

}
