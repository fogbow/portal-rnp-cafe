package org.fogbowcloud.cafe;

import java.util.Properties;

import org.fogbowcloud.cafe.saml.HttpClientWrapper;
import org.fogbowcloud.cafe.saml.SAMLAssertionRetriever;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class PortalApplication extends Application {

	private Properties properties;
	private SAMLAssertionRetriever samlAssertionRetriever;
	private HttpClientWrapper httpClientWrapper;
	private DateUtils dateUtils;

	public PortalApplication(Properties properties) {
		this.dateUtils = new DateUtils();
		this.properties = properties;
	}

	public PortalApplication(Properties properties, SAMLAssertionRetriever samlAssertionRetriever, 
			HttpClientWrapper httpClientWrapper) {
		this(properties);
		this.samlAssertionRetriever = samlAssertionRetriever;
		this.httpClientWrapper = httpClientWrapper;
	}

	public Properties getProperties() {
		return properties;
	}
	
	public SAMLAssertionRetriever getSamlAssertionRetriever() {
		return samlAssertionRetriever;
	}
	
	public HttpClientWrapper getHttpClientWrapper() {
		return httpClientWrapper;
	}
	
	@Override
	public Restlet createInboundRoot() {
		Router router = new Router(getContext());
		router.attach("/", PortalResource.class);
		return router;
	}
	
	public DateUtils getDateUtils() {
		return dateUtils;
	}
	
	public void setDateUtils(DateUtils dateUtils) {
		this.dateUtils = dateUtils;
	}
	
	public class DateUtils {
		
		public long getCurrentTimeMillis() {
			return System.currentTimeMillis();
		}
		
	}	
	
}
