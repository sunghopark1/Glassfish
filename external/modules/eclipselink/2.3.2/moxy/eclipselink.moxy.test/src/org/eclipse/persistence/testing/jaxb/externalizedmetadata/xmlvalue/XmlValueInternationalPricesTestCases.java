/*******************************************************************************
 * Copyright (c) 1998, 2011 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * dmccann - October 7/2009 - 2.0 - Initial implementation
 ******************************************************************************/
package org.eclipse.persistence.testing.jaxb.externalizedmetadata.xmlvalue;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.testing.jaxb.JAXBTestCases;

public class XmlValueInternationalPricesTestCases extends JAXBTestCases{
    private static final String XML_RESOURCE = "org/eclipse/persistence/testing/jaxb/externalizedmetadata/xmlvalue/intprices.xml";

	public XmlValueInternationalPricesTestCases(String name) throws Exception {
		super(name);
		setClasses(new Class[] { InternationalPricesNoAnnotation.class });
		setControlDocument(XML_RESOURCE);
	}
	
	   
    public Map getProperties(){
		InputStream inputStream = ClassLoader.getSystemResourceAsStream("org/eclipse/persistence/testing/jaxb/externalizedmetadata/xmlvalue/eclipselink-oxm-intprices.xml");

		HashMap<String, Source> metadataSourceMap = new HashMap<String, Source>();
		metadataSourceMap.put("org.eclipse.persistence.testing.jaxb.externalizedmetadata.xmlvalue", new StreamSource(inputStream));
		Map<String, Map<String, Source>> properties = new HashMap<String, Map<String, Source>>();
		properties.put(JAXBContextFactory.ECLIPSELINK_OXM_XML_KEY, metadataSourceMap);		
	        
	    return properties;
	}
	    
	    
	public void testSchemaGen() throws Exception{
	   	List controlSchemas = new ArrayList();
	   	InputStream is = ClassLoader.getSystemResourceAsStream("org/eclipse/persistence/testing/jaxb/externalizedmetadata/xmlvalue/intprices_schema.xsd");	    	
	   	controlSchemas.add(is);
	   	
	   	super.testSchemaGen(controlSchemas);	  
	   	
	   	InputStream src = ClassLoader.getSystemResourceAsStream(XML_RESOURCE);
	   	InputStream schema = ClassLoader.getSystemResourceAsStream("org/eclipse/persistence/testing/jaxb/externalizedmetadata/xmlvalue/intprices_schema.xsd");
	   	String result = validateAgainstSchema(src, new StreamSource(schema));
        assertTrue("Schema validation failed unxepectedly: " + result, result == null);
  
	}

	protected Object getControlObject() {
		InternationalPricesNoAnnotation price = new InternationalPricesNoAnnotation();
        price.currency ="CDN";              
        
        price.prices = new BigDecimal[]{new BigDecimal("123.45678901234567890"), new BigDecimal("234.45678901234567890"), new BigDecimal("345.45678901234567890")};
        return price;
	}
    
}
