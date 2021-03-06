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
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.tests.jpql;

import org.eclipse.persistence.testing.framework.TestErrorException;

class SimpleFromFailed extends org.eclipse.persistence.testing.tests.jpql.JPQLTestCase {
    String errorMessage = null;

    public void setup() {
        String ejbqlString = "SELECT OBJECT(emp) Frow Employee emp";
        setEjbqlString(ejbqlString);

        super.setup();
    }

    public void test() throws Exception {
        errorMessage = null;
        try {
            getSession().logMessage("Running EJBQL -> " + getEjbqlString());
            setReturnedObjects(getSession().executeQuery(getQuery()));
        } catch (Exception e) {
            errorMessage = e.getMessage();
        }
    }

    /**
     * verify(): Make sure the error message is the right one
     **/
    public void verify() throws Exception {
        if (errorMessage == null) {
            setTestException(new TestErrorException(getName() + " Verify Failed: " + "No error thrown"));
        }
        if (errorMessage.indexOf("syntax error at [Employee]") == -1) {
            setTestException(new TestErrorException("Frow should have thrown exception"));
        }
    }
}
