package org.fogbowcloud.shipapp;

import org.fogbowcloud.shipapp.api.http.ShibResource;
import org.fogbowcloud.shipapp.utils.HttpClientWrapper;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class ShibApplication extends Application {

	private HttpClientWrapper httpClientWrapper;
	private DateUtils dateUtils;

	public ShibApplication() {
		this.dateUtils = new DateUtils();
	}

	@Override
	public Restlet createInboundRoot() {
		Router router = new Router(getContext());
		router.attach("/", ShibResource.class);
		return router;
	}

	public HttpClientWrapper getHttpClientWrapper() {
		return this.httpClientWrapper;
	}
	
	public DateUtils getDateUtils() {
		return this.dateUtils;
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
