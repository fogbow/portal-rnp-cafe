package org.fogbowcloud.cafe.utils;

import org.apache.commons.codec.Charsets;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class HttpClientWrapper {

	private static final Logger LOGGER = Logger.getLogger(HttpClientWrapper.class);

	private static final String LOCATION_HEADER_KEY = "Location";
	private static final String GET = "get";
	
	private HttpClient client;

	private HttpResponseWrapper doRequest(String url, String method,
			HttpEntity entity, SSLConnectionSocketFactory sslSocketFactory) throws Exception {
		HttpRequestBase request = new HttpGet(url);
		
		HttpResponse response = null;
		String responseStr = null;
		try {
			response = getClient(sslSocketFactory).execute(request);
			responseStr = EntityUtils.toString(response.getEntity(),
					Charsets.UTF_8);
		} catch (Exception e) {
			LOGGER.error("Could not perform HTTP request.", e);
			throw e;
		} finally {
			try {
				response.getEntity().getContent().close();
			} catch (Exception e) {
				// Best effort
			}
		}
		Header headerLocation = response.getFirstHeader(LOCATION_HEADER_KEY);
		StatusLine statusLine = response.getStatusLine();
		return new HttpResponseWrapper(statusLine, responseStr, headerLocation.getValue());
	}

	public HttpResponseWrapper doGet(String url) throws Exception {
		return doRequest(url, GET, null, null);
	}

	private HttpClient getClient(SSLConnectionSocketFactory sslSocketFactory) {
		if (this.client == null) {
			if (sslSocketFactory == null) {
				this.client = HttpClients.createMinimal();
			} else {
				this.client = HttpClients.custom().setSSLSocketFactory(sslSocketFactory).build();
			}
		}
		return this.client;
	}
	
	public void setClient(HttpClient client) {
		this.client = client;
	}
}
