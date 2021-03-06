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

package com.sun.enterprise.admin.server.core.mbean.config;

//JMX imports
import javax.management.*;

//Config imports
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.config.serverbeans.Profiler;


//Admin imports
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.common.exception.MBeanConfigException;
import com.sun.enterprise.admin.common.constant.ConfigAttributeName;

/**
    This Config MBean represents a Profiler element.
    It extends ConfigMBeanBase class which provides get/set attribute(s) and getMBeanInfo services according to text descriptions.
    ObjectName of this MBean is:
        ias: type=auth-db, instance-name=<instance-name>, class=<virtualServerClassId>, server=<virtualServerId> name=<AuthDb-id>
*/
public class ManagedProfiler extends ConfigMBeanBase implements ConfigAttributeName.Profiler
{
    /**
     * MAPLIST array defines mapping between "external" name and its location in XML relatively base node
     */
    private static final String[][] MAPLIST  =
    {
        {kName              , ATTRIBUTE + ServerTags.NAME},
        {kClasspath         , ATTRIBUTE + ServerTags.CLASSPATH},
        {kNativeLibraryPath , ATTRIBUTE + ServerTags.NATIVE_LIBRARY_PATH},
        {kEnabled           , ATTRIBUTE + ServerTags.ENABLED},
    };
    /** 
     * ATTRIBUTES array specifies attributes descriptions in format defined for MBeanEasyConfig
     */
    private static final String[]   ATTRIBUTES  =
    {
        kName               + ", String,   R" ,
        kClasspath          + ", String,   RW" ,
        kNativeLibraryPath  + ", String,   RW" ,
        kEnabled            + ", boolean,  RW" ,
    };
    /** 
     * OPERATIONS array specifies operations descriptions in format defined for MBeanEasyConfig
     */
    private static final String[]   OPERATIONS  =
    {
        "getJvmOptions(), INFO",
        "setJvmOptions(String[] options), ACTION",
    };
    
    /**
        Default constructor sets MBean description tables
    */
    public ManagedProfiler() throws MBeanConfigException
    {
        this.setDescriptions(MAPLIST, ATTRIBUTES, OPERATIONS);
    }

    /**
        Constructs Config MBean for Profiler.
        @param instanceName The server instance name.
    */
    public ManagedProfiler(String instanceName) throws MBeanConfigException
    {
        this(); //set description tables
        initialize(ObjectNames.kProfiler, new String[]{instanceName});
    }
    /**
    This operation returns list of JvmOptions  connected to this class.
     */
    public String[] getJvmOptions() throws ConfigException
    {
        Profiler  profiler  = (Profiler)getBaseConfigBean();
        return profiler.getJvmOptions();
    }

    /**
    This operation returns list of JvmOptions  connected to this class.
     */
    public void setJvmOptions(String[] options) throws ConfigException
    {
        Profiler  profiler  = (Profiler)getBaseConfigBean();
        profiler.setJvmOptions(options);
        getConfigContext().flush();
    }
}
