package org.fogbowcloud.cafe.saml;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.Charsets;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class HttpClientWrapper {

	private static final String GET = "get";
	private static final String POST = "post";
	private static final String DELETE = "delete";
	private static final Logger LOGGER = Logger
			.getLogger(HttpClientWrapper.class);
	private HttpClient client;

	private HttpResponseWrapper doRequest(String url, String method,
			HttpEntity entity, SSLConnectionSocketFactory sslSocketFactory,
			Map<String, String> headers) throws Exception {
		HttpRequestBase request = null;
		if (method.equals(POST)) {
			request = new HttpPost(url);
			if (entity != null) {
				((HttpPost) request).setEntity(entity);
			}
		} else if (method.equals(GET)) {
			request = new HttpGet(url);
		} else if (method.equals(DELETE)) {
			request = new HttpDelete(url);
		}
		if (headers != null) {
			for (Entry<String, String> header : headers.entrySet()) {
				request.setHeader(header.getKey(), header.getValue());
			}
		}
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
		return new HttpResponseWrapper(response.getStatusLine(), responseStr);
	}

	public HttpResponseWrapper doPost(String url) throws Exception {
		return doPost(url, null);
	}

	public HttpResponseWrapper doGet(String url, Map<String, String> headers) throws Exception {
		return doRequest(url, GET, null, null, headers);
	}

	public HttpResponseWrapper doGet(String url) throws Exception {
		return doGet(url, null);
	}

	public HttpResponseWrapper doPost(String url, StringEntity entity) throws Exception {
		return doPost(url, entity, null);
	}

	public HttpResponseWrapper doPost(String url, StringEntity entity,
			Map<String, String> headers) throws Exception {
		return doRequest(url, POST, entity, null, headers);
	}

	public HttpResponseWrapper doPostSSL(String url, StringEntity entity,
			SSLConnectionSocketFactory sslSocketFactory,
			Map<String, String> headers) throws Exception {
		return doRequest(url, POST, entity, sslSocketFactory, headers);
	}

	public HttpResponseWrapper doPostSSL(String url,
			SSLConnectionSocketFactory sslSocketFactory) throws Exception {
		return doPostSSL(url, null, sslSocketFactory, null);
	}

	public HttpResponseWrapper doGetSSL(String url,
			SSLConnectionSocketFactory sslSocketFactory) throws Exception {
		return doGetSSL(url, sslSocketFactory, null);
	}

	public HttpResponseWrapper doGetSSL(String url,
			SSLConnectionSocketFactory sslSocketFactory,
			Map<String, String> headers) throws Exception {
		return doRequest(url, GET, null, sslSocketFactory, headers);
	}
	
	public HttpResponseWrapper doDeleteSSL(String url, SSLConnectionSocketFactory sslSocketFactory, 
			Map<String, String> headers) throws Exception {
		return doRequest(url, DELETE, null, sslSocketFactory, headers);
	}

	private HttpClient getClient(SSLConnectionSocketFactory sslSocketFactory) {
		if (client == null) {
			if (sslSocketFactory == null) {
				client = HttpClients.createMinimal();
			} else {
				client = HttpClients.custom()
						.setSSLSocketFactory(sslSocketFactory).build();
			}
		}
		return client;
	}
}
