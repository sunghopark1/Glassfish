/*******************************************************************************
* Copyright (c) 1998, 2009 Oracle. All rights reserved.
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
* which accompanies this distribution.
* The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
* and the Eclipse Distribution License is available at
* http://www.eclipse.org/org/documents/edl-v10.php.
*
* Contributors:
*     bdoughan - August 7/2009 - 2.0 - Initial implementation
******************************************************************************/
package org.eclipse.persistence.internal.oxm;

import org.eclipse.persistence.internal.oxm.record.UnmarshalContext;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.oxm.mappings.converters.XMLConverter;
import org.eclipse.persistence.oxm.record.UnmarshalRecord;

/**
 * Allow the unmarshal context to be wrapped.  This is necessary so that choice
 * mappings with a converter can convert the result of the nested mapping.
 */
public class ChoiceUnmarshalContext implements UnmarshalContext {

    private UnmarshalContext unmarshalContext;
    private Converter converter;

    public ChoiceUnmarshalContext(UnmarshalContext unmarshalContext, Converter converter) {
        this.unmarshalContext = unmarshalContext;
        this.converter = converter;
    }

    public void addAttributeValue(UnmarshalRecord unmarshalRecord, ContainerValue containerValue, Object value) {
        this.unmarshalContext.addAttributeValue(unmarshalRecord, containerValue, getValue(value, unmarshalRecord));
    }

    public void addAttributeValue(UnmarshalRecord unmarshalRecord, ContainerValue containerValue, Object value, Object collection) {
        this.unmarshalContext.addAttributeValue(unmarshalRecord, containerValue, getValue(value, unmarshalRecord), collection);
    }

    public void characters(UnmarshalRecord unmarshalRecord) {
        unmarshalContext.characters(unmarshalRecord);
    }

    public void endElement(UnmarshalRecord unmarshalRecord) {
        unmarshalContext.endElement(unmarshalRecord);
    }

    public void reference(Reference reference) {
        unmarshalContext.reference(reference);
    }

    public void setAttributeValue(UnmarshalRecord unmarshalRecord, Object value, DatabaseMapping mapping) {
        unmarshalContext.setAttributeValue(unmarshalRecord, getValue(value, unmarshalRecord), mapping);
    }

    public void startElement(UnmarshalRecord unmarshalRecord) {
        unmarshalContext.startElement(unmarshalRecord);
    }

    public void unmappedContent(UnmarshalRecord unmarshalRecord) {
        unmarshalContext.unmappedContent(unmarshalRecord);
    }

    private Object getValue(Object value, UnmarshalRecord unmarshalRecord) {
        if(converter instanceof XMLConverter) {
            return ((XMLConverter) converter).convertDataValueToObjectValue(value, unmarshalRecord.getSession(), unmarshalRecord.getUnmarshaller()); 
        }
        return converter.convertDataValueToObjectValue(value, unmarshalRecord.getSession());
    }

}