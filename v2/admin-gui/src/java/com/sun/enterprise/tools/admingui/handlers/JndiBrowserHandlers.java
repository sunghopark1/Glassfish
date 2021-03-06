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

/*
 * JndiBrowserHandlers.java
 *
 * Created on December 16, 2006, 8:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.tools.admingui.handlers;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.sun.enterprise.tools.admingui.util.JMXUtil;
import com.sun.enterprise.tools.admingui.util.TargetUtil;
import com.sun.enterprise.connectors.ConnectorRuntime;

import javax.management.ObjectName;

/**
 *
 * @author anac
 */


public class JndiBrowserHandlers {
    
    /** Creates a new instance of JndiBrowserHandlers */
    public JndiBrowserHandlers() {
    }
    
    

    
    /**
     *	<p> This handler returns the list of tagets for JNDI Names.
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getJndiTargets",
        input={
            @HandlerInput(name="jndiName", type=String.class)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
     public static void getJndiTargets(HandlerContext handlerCtx){
        List result = new ArrayList();
        try{
            String jndiName = (String)handlerCtx.getInputValue("jndiName");
            
            String[] params = new String[] {jndiName};
            String[] types = new String[] {"java.lang.String"};
            ObjectName[] refs = (ObjectName[]) JMXUtil.invoke("com.sun.appserv:type=resources,category=config" , "listReferencees", params, types);
            for(int i=0; i<refs.length; i++){
                HashMap oneRow = new HashMap();
                String target = refs[i].getKeyProperty("name");
                oneRow.put("name", target);
                boolean isCluster = TargetUtil.isCluster(target);
                if (isCluster){
                    oneRow.put("url", "/cluster/clusterGeneral.jsf?clusterName="+target);
                }else{
                    oneRow.put("url", "/standalone/standaloneInstanceGeneral.jsf?instanceName="+target);
                }        
                result.add(oneRow);
            }
        }catch(Exception ex){
            handlerCtx.setOutputValue("result", result);
            
        }
        handlerCtx.setOutputValue("result", result);
    }
    

    /**
     *	<p> This handler is used to load all the system RA resoures.
     */
    @Handler(id="loadAllSystemRAResources")
     public static void loadAllSystemRAResources(HandlerContext handlerCtx){
        try{
            //refer to Issue Tracker# 1996.  Ensure all system resource is loaded.
            ConnectorRuntime.getRuntime().loadAllSystemRAResources();
        }catch (Exception ex){
            //Ignoring NullPointerException 
            //ex.printStackTrace();
        }
    }
}
