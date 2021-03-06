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

package com.sun.enterprise.connectors;

import java.util.*;
import java.util.logging.*;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.server.*;
import com.sun.enterprise.connectors.system.ActiveJmsResourceAdapter;
import com.sun.enterprise.connectors.util.*;

/**
 * This is configuration parser service. Retrieves various configuration 
 * information from ra.xml
 * @author    Srikanth P 
 */


public class ConnectorConfigurationParserServiceImpl extends 
                        ConnectorServiceImpl {

     
    /**
     * Default constructor
     */

     public ConnectorConfigurationParserServiceImpl() {
         super();
     }

    /**
     *  Obtains the Permission string that needs to be added to the
     *  to the security policy files. These are the security permissions needed
     *  by the resource adapter implementation classes.
     *  These strings are obtained by parsing the ra.xml
     *  @param moduleName rar module Name
     *  @ConnectorRuntimeException If rar.xml parsing fails.
     */

    public String getSecurityPermissionSpec(String moduleName)
                         throws ConnectorRuntimeException 
    {

        if(moduleName == null) {
            return null;
        }
        ConnectorDescriptor connectorDescriptor = getConnectorDescriptor(
                                 moduleName);
        Set securityPermissions = connectorDescriptor.getSecurityPermissions();
        Iterator it = securityPermissions.iterator();
        String policyString = null;
        SecurityPermission secPerm = null; 
        String permissionString=null;
        while(it.hasNext()){
            secPerm = (SecurityPermission) it.next();
            permissionString = secPerm.getPermission();
            if(permissionString != null) {
                policyString = policyString+"\n \n"+permissionString;
            }
        }
        if(policyString != null) {
            policyString= CAUTION_MESSAGE+policyString;
        } 
        return policyString;
    }

    /** Obtains all the Connection definition names of a rar
     *  @param rarName rar moduleName 
     *  @return Array of connection definition names.
     */

    public String[] getConnectionDefinitionNames(String rarName)
               throws ConnectorRuntimeException 
    {

        ConnectorDescriptor desc = getConnectorDescriptor(rarName);
        if(desc != null) {
            MCFConfigParser mcfConfigParser = (MCFConfigParser) 
              ConnectorConfigParserFactory.getParser(ConnectorConfigParser.MCF);
            return mcfConfigParser.getConnectionDefinitionNames(desc);
        } else {
            return null;
        } 
    }
  
    /**
     *  Retrieves the Resource adapter javabean properties with default values.
     *  The default values will the values present in the ra.xml. If the 
     *  value is not present in ra.xxml, javabean is introspected to obtain 
     *  the default value present, if any. If intrspection fails or null is the
     *  default value, empty string is returned. 
     *  If ra.xml has only the property and no value, empty string is the value 
     *  returned.
     *  @param rarName rar module name 
     *  @return Resource adapter javabean properties with default values.
     *  @throws ConnectorRuntimeException if property retrieval fails.
     */

    public Properties getResourceAdapterConfigProps(String rarName) 
                throws ConnectorRuntimeException 
    {
        return getConnectorConfigJavaBeans(
               rarName,null,ConnectorConfigParser.RA);
    }

    /**
     *  Retrieves the MCF javabean properties with default values.
     *  The default values will the values present in the ra.xml. If the 
     *  value is not present in ra.xxml, javabean is introspected to obtain 
     *  the default value present, if any. If intrspection fails or null is the
     *  default value, empty string is returned. 
     *  If ra.xml has only the property and no value, empty string is the value 
     *  returned.
     *  @param rarName rar module name 
     *  @return managed connection factory javabean properties with 
     *          default values.
     *  @throws ConnectorRuntimeException if property retrieval fails.
     */

    public Properties getMCFConfigProps(
     String rarName,String connectionDefName) throws ConnectorRuntimeException 
    {
        Properties props = getConnectorConfigJavaBeans(
               rarName,connectionDefName,ConnectorConfigParser.MCF);
	if (rarName.equals(ConnectorConstants.DEFAULT_JMS_ADAPTER)) {
            props.remove(ActiveJmsResourceAdapter.ADDRESSLIST);
	}
        return props;
    }

    /**
     *  Retrieves the admin object javabean properties with default values.
     *  The default values will the values present in the ra.xml. If the 
     *  value is not present in ra.xxml, javabean is introspected to obtain 
     *  the default value present, if any. If intrspection fails or null is the
     *  default value, empty string is returned. 
     *  If ra.xml has only the property and no value, empty string is the value 
     *  returned.
     *  @param rarName rar module name 
     *  @return admin object javabean properties with 
     *          default values.
     *  @throws ConnectorRuntimeException if property retrieval fails.
     */

    public Properties getAdminObjectConfigProps(
      String rarName,String adminObjectIntf) throws ConnectorRuntimeException 
    {
        return getConnectorConfigJavaBeans(
               rarName,adminObjectIntf,ConnectorConfigParser.AOR);
    }

    /**
     *  Retrieves the XXX javabean properties with default values.
     *  The javabean to introspect/retrieve is specified by the type. 
     *  The default values will be the values present in the ra.xml. If the 
     *  value is not present in ra.xxml, javabean is introspected to obtain 
     *  the default value present, if any. If intrspection fails or null is the
     *  default value, empty string is returned. 
     *  If ra.xml has only the property and no value, empty string is the value 
     *  returned.
     *  @param rarName rar module name 
     *  @return admin object javabean properties with 
     *          default values.
     *  @throws ConnectorRuntimeException if property retrieval fails.
     */

    public Properties getConnectorConfigJavaBeans(String rarName,
        String connectionDefName,String type) throws ConnectorRuntimeException
    {

        ConnectorDescriptor desc = getConnectorDescriptor(rarName);
        if(desc != null) {
            ConnectorConfigParser ccParser = 
                     ConnectorConfigParserFactory.getParser(type);
            return ccParser.getJavaBeanProps(desc,connectionDefName, rarName);
        } else {
            return null;
        }
    }

    /**
     * Return the ActivationSpecClass name for given rar and messageListenerType
     * @param moduleDir The directory where rar is exploded.
     * @param messageListenerType MessageListener type
     * @throws  ConnectorRuntimeException If moduleDir is null.
     *          If corresponding rar is not deployed.
     */

    public String getActivationSpecClass( String rarName,
             String messageListenerType) throws ConnectorRuntimeException
    {
        ConnectorDescriptor desc = getConnectorDescriptor(rarName);
        if(desc != null) {
            MessageListenerConfigParser messagelistenerConfigParser =
                 (MessageListenerConfigParser) 
                 ConnectorConfigParserFactory.getParser(
                 ConnectorConfigParser.MSL);
            return messagelistenerConfigParser.getActivationSpecClass(
                       desc,messageListenerType);
        } else {
            return null;
        }
    }

    /* Parses the ra.xml and returns all the Message listener types.
     *
     * @param  rarName name of the rar module.
     * @return Array of message listener types as strings.
     * @throws  ConnectorRuntimeException If moduleDir is null.
     *          If corresponding rar is not deployed.
     *
     */

    public String[] getMessageListenerTypes(String rarName)
               throws ConnectorRuntimeException
    {
        ConnectorDescriptor desc = getConnectorDescriptor(rarName);
        if(desc != null) {
            MessageListenerConfigParser messagelistenerConfigParser =
                (MessageListenerConfigParser) 
                ConnectorConfigParserFactory.getParser(
                ConnectorConfigParser.MSL);
            return messagelistenerConfigParser.getMessageListenerTypes(desc);
        } else {
            return null;
        }
    }

    /** Parses the ra.xml for the ActivationSpec javabean
     *  properties. The ActivationSpec to be parsed is
     *  identified by the moduleDir where ra.xml is present and the
     *  message listener type.
     *
     *  message listener type will be unique in a given ra.xml.
     *
     *  It throws ConnectorRuntimeException if either or both the
     *  parameters are null, if corresponding rar is not deployed,
     *  if message listener type mentioned as parameter is not found in ra.xml.
     *  If rar is deployed and message listener (type mentioned) is present
     *  but no properties are present for the corresponding message listener,
     *  null is returned.
     *
     *  @param  rarName name of the rar module.
     *  @param  messageListenerType message listener type.It is uniqie
     *          across all <messagelistener> sub-elements in <messageadapter>
     *          element in a given rar.
     *  @return Javabean properties with the property names and values
     *          of properties. The property values will be the values
     *          mentioned in ra.xml if present. Otherwise it will be the
     *          default values obtained by introspecting the javabean.
     *          In both the case if no value is present, empty String is
     *          returned as the value.
     *  @throws  ConnectorRuntimeException if either of the parameters are null.
     *           If corresponding rar is not deployed i.e moduleDir is invalid.
     *           If messagelistener type is not found in ra.xml
     */

    public Properties getMessageListenerConfigProps(String rarName,
         String messageListenerType)throws ConnectorRuntimeException
    {
        return getConnectorConfigJavaBeans(
               rarName,messageListenerType,ConnectorConfigParser.MSL);
    }

    /** Returns the Properties object consisting of propertyname as the
     *  key and datatype as the value.
     *  @param  rarName name of the rar module.
     *  @param  messageListenerType message listener type.It is uniqie
     *          across all <messagelistener> sub-elements in <messageadapter>
     *          element in a given rar.
     *  @return Properties object with the property names(key) and datatype
     *          of property(as value).
     *  @throws  ConnectorRuntimeException if either of the parameters are null.
     *           If corresponding rar is not deployed i.e moduleDir is invalid.
     *           If messagelistener type is not found in ra.xml
     */

    public Properties getMessageListenerConfigPropTypes(String rarName,
               String messageListenerType) throws ConnectorRuntimeException
    {
        ConnectorDescriptor desc = getConnectorDescriptor(rarName);
        if(desc != null) {
            MessageListenerConfigParser messagelistenerConfigParser =
                (MessageListenerConfigParser) 
                ConnectorConfigParserFactory.getParser(
                ConnectorConfigParser.MSL);
            return messagelistenerConfigParser.getJavaBeanReturnTypes(
                    desc, messageListenerType);
        } else {
            return null;
        }
    }

    /** Obtains all the Connection definition names of a rar
     *  @param rarName rar moduleName 
     *  @return Array of connection definition names.
     */

    public String[] getAdminObjectInterfaceNames(String rarName)
               throws ConnectorRuntimeException 
    {

        ConnectorDescriptor desc = getConnectorDescriptor(rarName);
        if(desc != null) {
            AdminObjectConfigParser adminObjectConfigParser = 
                 (AdminObjectConfigParser) 
                 ConnectorConfigParserFactory.getParser(
                 ConnectorConfigParser.AOR);
            return adminObjectConfigParser.getAdminObjectInterfaceNames(desc);
        } else {
            return null;
        }
    }

    /**
     * Finds the properties of a RA JavaBean bundled in a RAR
     * without exploding the RAR
     * 
     * @param pathToDeployableUnit a physical,accessible location of the connector module.
     * [either a RAR for RAR-based deployments or a directory for Directory based deployments] 
     * @return A Map that is of <String RAJavaBeanPropertyName, String defaultPropertyValue>
     * An empty map is returned in the case of a 1.0 RAR 
     */
    public Map getRABeanProperties(String pathToDeployableUnit) throws ConnectorRuntimeException{
        return RARUtils.getRABeanProperties(pathToDeployableUnit);
    }
}
