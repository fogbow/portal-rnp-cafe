package org.fogbowcloud.cafe.saml;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public class SAMLAssertionRetriever {

	public SAMLAssertionRetriever() {
		try {
			DefaultBootstrap.bootstrap();
		} catch (ConfigurationException e) {
		}
	}

	public Map<String, String> retrieve(String aseertionURL) throws Exception {
		
		HttpResponseWrapper responseWrapper = new HttpClientWrapper().doGet(aseertionURL);
		
		Document document = createDocumentBuilder().parse(new InputSource(
					new StringReader(responseWrapper.getContent())));
		
        Element assertionEl = document.getDocumentElement();
        
        Unmarshaller unmarshaller = Configuration.getUnmarshallerFactory().getUnmarshaller(assertionEl);
        Assertion assertion = (Assertion) unmarshaller.unmarshall(assertionEl);
		
        Map<String, String> tokenAttrs = new HashMap<String, String>();
        
        List<AttributeStatement> attributeStatements = assertion.getAttributeStatements();
        for (AttributeStatement attributeStatement : attributeStatements) {
        	List<Attribute> attributes = attributeStatement.getAttributes();
        	for (Attribute attribute : attributes) {
				List<XMLObject> attributeValues = attribute.getAttributeValues();
				if (attributeValues.isEmpty()) {
					continue;
				}
				XMLObject attributeValue = attributeValues.iterator().next();
				String value = attributeValue.getDOM().getTextContent();
				tokenAttrs.put(attribute.getFriendlyName(), value);
			}
		}
		
		return tokenAttrs;
	}

	private DocumentBuilder createDocumentBuilder() throws Exception {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
		return docBuilder;
	}
	
}
