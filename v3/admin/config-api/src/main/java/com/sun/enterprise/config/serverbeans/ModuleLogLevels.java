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



package com.sun.enterprise.config.serverbeans;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.component.Injectable;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

import java.beans.PropertyVetoException;
import java.util.List;


/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "property"
}) */
@org.glassfish.admin.amx.AMXConfigInfo( amxInterface=com.sun.appserv.management.config.ModuleLogLevelsConfig.class, singleton=true)
@Configured
public interface ModuleLogLevels extends ConfigBeanProxy, Injectable  {

    /**
     * Gets the value of the root property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getRoot();

    /**
     * Sets the value of the root property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRoot(String value) throws PropertyVetoException;

    /**
     * Gets the value of the server property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getServer();

    /**
     * Sets the value of the server property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setServer(String value) throws PropertyVetoException;

    /**
     * Gets the value of the ejbContainer property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getEjbContainer();

    /**
     * Sets the value of the ejbContainer property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEjbContainer(String value) throws PropertyVetoException;

    /**
     * Gets the value of the cmpContainer property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getCmpContainer();

    /**
     * Sets the value of the cmpContainer property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCmpContainer(String value) throws PropertyVetoException;

    /**
     * Gets the value of the mdbContainer property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getMdbContainer();

    /**
     * Sets the value of the mdbContainer property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMdbContainer(String value) throws PropertyVetoException;

    /**
     * Gets the value of the webContainer property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getWebContainer();

    /**
     * Sets the value of the webContainer property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setWebContainer(String value) throws PropertyVetoException;

    /**
     * Gets the value of the classloader property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getClassloader();

    /**
     * Sets the value of the classloader property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setClassloader(String value) throws PropertyVetoException;

    /**
     * Gets the value of the configuration property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getConfiguration();

    /**
     * Sets the value of the configuration property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setConfiguration(String value) throws PropertyVetoException;

    /**
     * Gets the value of the naming property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getNaming();

    /**
     * Sets the value of the naming property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setNaming(String value) throws PropertyVetoException;

    /**
     * Gets the value of the security property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getSecurity();

    /**
     * Sets the value of the security property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSecurity(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jts property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getJts();

    /**
     * Sets the value of the jts property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJts(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jta property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getJta();

    /**
     * Sets the value of the jta property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJta(String value) throws PropertyVetoException;

    /**
     * Gets the value of the admin property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getAdmin();

    /**
     * Sets the value of the admin property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAdmin(String value) throws PropertyVetoException;

    /**
     * Gets the value of the deployment property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getDeployment();

    /**
     * Sets the value of the deployment property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDeployment(String value) throws PropertyVetoException;

    /**
     * Gets the value of the verifier property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getVerifier();

    /**
     * Sets the value of the verifier property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setVerifier(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jaxr property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getJaxr();

    /**
     * Sets the value of the jaxr property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJaxr(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jaxrpc property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getJaxrpc();

    /**
     * Sets the value of the jaxrpc property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJaxrpc(String value) throws PropertyVetoException;

    /**
     * Gets the value of the saaj property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getSaaj();

    /**
     * Sets the value of the saaj property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSaaj(String value) throws PropertyVetoException;

    /**
     * Gets the value of the corba property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getCorba();

    /**
     * Sets the value of the corba property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCorba(String value) throws PropertyVetoException;

    /**
     * Gets the value of the javamail property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getJavamail();

    /**
     * Sets the value of the javamail property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJavamail(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jms property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getJms();

    /**
     * Sets the value of the jms property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJms(String value) throws PropertyVetoException;

    /**
     * Gets the value of the connector property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getConnector();

    /**
     * Sets the value of the connector property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setConnector(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jdo property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getJdo();

    /**
     * Sets the value of the jdo property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJdo(String value) throws PropertyVetoException;

    /**
     * Gets the value of the cmp property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getCmp();

    /**
     * Sets the value of the cmp property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCmp(String value) throws PropertyVetoException;

    /**
     * Gets the value of the util property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getUtil();

    /**
     * Sets the value of the util property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setUtil(String value) throws PropertyVetoException;

    /**
     * Gets the value of the resourceAdapter property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getResourceAdapter();

    /**
     * Sets the value of the resourceAdapter property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setResourceAdapter(String value) throws PropertyVetoException;

    /**
     * Gets the value of the synchronization property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getSynchronization();

    /**
     * Sets the value of the synchronization property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSynchronization(String value) throws PropertyVetoException;

    /**
     * Gets the value of the nodeAgent property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getNodeAgent();

    /**
     * Sets the value of the nodeAgent property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setNodeAgent(String value) throws PropertyVetoException;

    /**
     * Gets the value of the selfManagement property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getSelfManagement();

    /**
     * Sets the value of the selfManagement property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSelfManagement(String value) throws PropertyVetoException;

    /**
     * Gets the value of the groupManagementService property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getGroupManagementService();

    /**
     * Sets the value of the groupManagementService property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setGroupManagementService(String value) throws PropertyVetoException;

    /**
     * Gets the value of the managementEvent property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getManagementEvent();

    /**
     * Sets the value of the managementEvent property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setManagementEvent(String value) throws PropertyVetoException;

    /**
     * Gets the value of the property property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the property property.
     * <p/>
     * <p/>
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProperty().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link Property }
     */
    @Element("property")
    public List<Property> getProperty();





}
