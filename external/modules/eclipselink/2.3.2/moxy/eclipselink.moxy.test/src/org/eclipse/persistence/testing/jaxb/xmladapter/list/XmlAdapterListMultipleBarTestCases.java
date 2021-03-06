/*******************************************************************************
 * Copyright (c) 2011 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * dmccann - December 30/2010 - 2.3 - Initial implementation
 ******************************************************************************/
package org.eclipse.persistence.testing.jaxb.xmladapter.list;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;

import org.eclipse.persistence.testing.jaxb.JAXBTestCases;

import junit.framework.TestCase;

public class XmlAdapterListMultipleBarTestCases extends JAXBTestCases {
    private final static String XML_RESOURCE = "org/eclipse/persistence/testing/jaxb/xmladapter/list/multiplebar.xml";

    public XmlAdapterListMultipleBarTestCases(String name) throws Exception {
        super(name);
        setControlDocument(XML_RESOURCE);
        Class[] classes = new Class[2];
        classes[0] = FooWithBars.class;
        classes[1] = Bar.class;
        setClasses(classes);
    }

    public Object getControlObject() {
        FooWithBars foo = new FooWithBars();
        List<String> itemlist = new ArrayList<String>();
        itemlist.add(MyAdapter.VAL0);
        itemlist.add(MyAdapter.VAL1);
        itemlist.add(MyAdapter.VAL2);
        foo.items = itemlist;
        return foo;
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.main(new String[] { "-c", "org.eclipse.persistence.testing.jaxb.xmladapter.list.XmlAdapterListMultipleBarTestCases" });
    }
}
