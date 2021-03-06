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
package com.sun.enterprise.webservice;

import com.sun.xml.ws.api.server.Adapter;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registry of JAXWS Adapter of endpoints.
 */
public class JAXWSAdapterRegistry {
    
    private static JAXWSAdapterRegistry registry = null;
    private Map store;
    private static final StringManager localStrings =
        StringManager.getManager(JAXWSAdapterRegistry.class);
    final Logger logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
    
    /** Creates a new instance of JAXWSServletUtil */
    private JAXWSAdapterRegistry() {
        store = new HashMap();
    }
    
    public static synchronized JAXWSAdapterRegistry getInstance() {
        if(registry == null)
            registry = new JAXWSAdapterRegistry();
        return registry;
    }
    
    public void addAdapter(String contextRoot, String urlPattern, 
            Adapter info) {
        if (contextRoot == null)
            contextRoot = "";
        ContextAdapter contextRtInfo = 
                (ContextAdapter)store.get(contextRoot);
        if(contextRtInfo == null) {
            contextRtInfo = new ContextAdapter(contextRoot);
        }        
        contextRtInfo.addAdapter(urlPattern, info);
        store.put(contextRoot, contextRtInfo);
    }
    
     public Adapter getAdapter(String contextRoot,
             String path, String urlPattern ) {
         ContextAdapter serviceInfo = 
                (ContextAdapter)store.get(contextRoot);        
        if(serviceInfo == null)
             return null;        
         return serviceInfo.getAdapter(path, urlPattern);
     }
  
     public void removeAdapter(String contextRoot) {
         if(contextRoot == null)
             contextRoot = "";
         ContextAdapter serviceInfo = 
                (ContextAdapter)store.get(contextRoot);
        if(serviceInfo == null)
             return ;
        store.remove(contextRoot);
     }
     
    class  ContextAdapter {
        String contextRoot;
        Map fixedUrlPatternEndpoints;
        List<Adapter> pathUrlPatternEndpoints;

        ContextAdapter(String contextRoot) {
            this.contextRoot = contextRoot;
            fixedUrlPatternEndpoints = new HashMap();
            pathUrlPatternEndpoints = new ArrayList();
        }

        void addAdapter(String urlPattern, Adapter info) {
            if (urlPattern.indexOf("*.") != -1) {
                // cannot deal with implicit mapping right now
                logger.log(Level.SEVERE, 
                        localStrings.getString("enterprise.webservice.implicitMappingNotSupported"));
            } else if (urlPattern.endsWith("/*")) {
                pathUrlPatternEndpoints.add(info);
            } else {
                if (fixedUrlPatternEndpoints.containsKey(urlPattern)) {
                    logger.log(Level.SEVERE, 
                            localStrings.getString("enterprise.webservice.duplicateService", 
                            new Object[]{urlPattern}));
                }
                fixedUrlPatternEndpoints.put(urlPattern, info);
            }
        }

        Adapter getAdapter(String path, String urlPattern) {
            Adapter result = (Adapter) fixedUrlPatternEndpoints.get(path);
            if (result == null) {                
                // This loop is unnecessary.Essentially what it is doing to always
                // return the first element from pathUrlPatternEndpoints
                // TO DO clean up after SCF required
                for (Iterator iter = pathUrlPatternEndpoints.iterator(); iter.hasNext();) {
                    Adapter candidate = (Adapter) iter.next();
                    if (path.startsWith(getValidPathForEndpoint(urlPattern))) {
                        result = candidate;
                        break;
                    }
                }
            }
            return result;
        }

         private String getValidPathForEndpoint(String s) {
            if (s.endsWith("/*")) {
                return s.substring(0, s.length() - 2);
            } else {
                return s;
            }
        }
    }
}
