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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */

package com.sun.enterprise.admin.verifier.tests;

/* Test Case which validates the Mail Resource Fields
 * Author : srini@sun.com
 **/

import java.net.*;

// 8.0 XML Verifier
//import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.config.ConfigException;
import  com.sun.enterprise.config.serverbeans.*;

import com.sun.enterprise.admin.verifier.*;
// Logging
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;



public class MailResourceTest extends ServerXmlTest implements ServerCheck {
    // Logging
    static Logger _logger = LogDomains.getLogger(LogDomains.APPVERIFY_LOGGER);
    
    public MailResourceTest() {
    }

    // check method called by command line verifier
    public Result check(ConfigContext context)
    {
        Result result;
        result = super.getInitializedResult();
        // 8.0 XML Verifier
        /*try {
            Server server = (Server)context.getRootConfigBean();
            Resources res = server.getResources();
            MailResource[] mailResource = res.getMailResource();
            for(int i=0;i < mailResource.length;i++) {
                String ip = mailResource[i].getHost();
                // Bug 4711404
                if(ip != null && ip.equals(""))
                    result.failed("Mail Resource Host Cannot be null String");
                try{
                   InetAddress.getByName(ip).getHostName();
                   result.passed("Valid IIOP Listener IP " + ip);
                }catch(UnknownHostException e){
                    result.failed("Host name not resolvable - " + ip);
                }
            }
        }
        catch(Exception ex) {
            //<addition author="irfan@sun.com" [bug/rfe]-id="logging" >
            /*ex.printStackTrace();
            result.failed("Exception : " + ex.getMessage());*/
            /*_logger.log(Level.FINE, "serverxmlverifier.exception", ex);
            result.failed("Exception : " + ex.getMessage());
            //</addition>
        }*/
        return result;
    }
    
     // check method called by iasadmin and admin GUI
     public Result check(ConfigContextEvent ccce) {
        Result result = new Result();
        Object value = ccce.getObject();
        String beanName = ccce.getBeanName();
        if(beanName!=null) {
            String name = ccce.getName();
            return testSave(name,(String)value);
        }
        
        MailResource res = (MailResource)value;
        String ip = res.getHost();
        
        // check if jndi-name is valid object name Bug : 4698687 : start
        String id = res.getJndiName();
        
        if(StaticTest.checkObjectName(id, result)) 
            result.passed("Valid Object Name");
        else {
            result.failed("Mail Resource Jndi-Name Invalid ");
            return result;
        }
        // End Bug : 4698687
        try{
            InetAddress.getByName(ip).getHostName();
            result.passed("Valid Mail Resource IP");
        }catch(UnknownHostException e){
            result.failed("Host name not resolvable - " + ip);
            return result;
        }
        return result;
    }
     
     public Result testSave(String name, String value) {
         Result result = new Result();
         result.passed("Passed **");
         if(name.equals(ServerTags.HOST)){
            try{
                InetAddress.getByName(value).getHostName();
                result.passed("Valid Mail Resource IP");
            }catch(UnknownHostException e){
                result.failed("Host name not resolvable - " + value);
            }
         }
	 return result;
     }
    
}
