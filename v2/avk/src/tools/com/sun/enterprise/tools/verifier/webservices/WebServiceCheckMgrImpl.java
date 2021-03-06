/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.enterprise.tools.verifier.webservices;

import javax.enterprise.deploy.shared.ModuleType;
import java.util.Iterator;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.CheckMgr;
import com.sun.enterprise.tools.verifier.FrameworkContext;
import com.sun.enterprise.tools.verifier.JarCheck;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;

/**
 * WebServices harness
 */
public class WebServiceCheckMgrImpl extends CheckMgr implements JarCheck {

    /**
     * name of the file containing the list of tests for the webservice
     * architecture
     */
    private final String testsListFileName = "TestNamesWebServices.xml"; // NOI18N
    private final String sunONETestsListFileName = getSunPrefix().concat(
            testsListFileName);
    private String moduleName;

    public WebServiceCheckMgrImpl(FrameworkContext frameworkContext) {
        this.frameworkContext = frameworkContext;
    }

    /**
     * Check Ejb for spec. conformance
     *
     * @param descriptor WebServices descriptor
     */
    public void check(Descriptor descriptor) throws Exception {
        WebServicesDescriptor rootDescriptor = (WebServicesDescriptor) descriptor;
        ModuleType moduleType = rootDescriptor.getBundleDescriptor()
                .getModuleType();
        if (moduleType == ModuleType.EJB)
            moduleName = Result.EJB;
        else if (moduleType == ModuleType.WAR)
            moduleName = Result.WEB;
        for (Iterator itr = rootDescriptor.getWebServices().iterator();
             itr.hasNext();) {
            WebService wsDescriptor = (WebService) itr.next();  
            // need to pass WebServiceEndpoint's to check
            for (Iterator endPtItr = wsDescriptor.getEndpoints().iterator();
                 endPtItr.hasNext();) {
                super.check((WebServiceEndpoint) endPtItr.next());
            }
        }
    }

    /**
     * return the configuration file name for the list of tests pertinent to the
     * connector architecture
     *
     * @return <code>String</code> filename containing the list of tests
     */
    protected String getTestsListFileName() {
        return testsListFileName;
    }

    /**
     * @return <code>String</code> filename containing sunone tests
     */
    protected String getSunONETestsListFileName() {
        return sunONETestsListFileName;
    }

    protected String getSchemaVersion(Descriptor descriptor) {
        return ((WebServiceEndpoint) descriptor).getWebService().
                getWebServicesDescriptor().getSpecVersion();
    }

    protected void setModuleName(Result r) {
        r.setModuleName(moduleName);
    }
    
    protected BundleDescriptor getBundleDescriptor(Descriptor descriptor) {
        return ((WebServiceEndpoint) descriptor).getBundleDescriptor();
    }

    protected ComponentNameConstructor getComponentNameConstructor(
            Descriptor descriptor) {
        return new ComponentNameConstructor((WebServiceEndpoint)descriptor);
    }

}
