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
 * $Header: /cvs/glassfish/appserv-api/src/java/com/sun/appserv/management/config/DeployedItemRefConfig.java,v 1.1 2006/12/02 06:03:04 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2006/12/02 06:03:04 $
 */

package com.sun.appserv.management.config;

import com.sun.appserv.management.base.XTypes;


/**
	Represents an &lt;application-ref&gt; element
	found within a &lt;server&gt element.
*/
public interface DeployedItemRefConfig extends RefConfig
{
/** The j2eeType as returned by {@link com.sun.appserv.management.base.AMX#getJ2EEType}. */
	public static final String	J2EE_TYPE	= XTypes.DEPLOYED_ITEM_REF_CONFIG;

    /**
        @return comma-delimited list of virtual servers
     */
    String getVirtualServers();
    
    /**
        See {@link #getVirtualServers}.
     */
    void setVirtualServers(final String virtualServers);

    /**
        <b>EE only</b>
        Return the boolean flag that causes any and all load-balancers using
        this application to consider this application unavailable to
        them. Defaults to unavailable (false).
     */    
    public boolean getLBEnabled();

    /**
        <b>EE only</b>
        Set the boolean flag that causes any and all load-balancers using
        this application to consider this application unavailable to
        them. 
     */    
    public void setLBEnabled(final boolean lbEnabled);
    
    /**
        The time, in minutes, that it takes this application to reach 
        a quiescent state after having been disabled.
        @since AppServer 9.0
     */
    public String   getDisableTimeoutInMinutes();
    
    /**
        @see #getDisableTimeoutInMinutes
        @since AppServer 9.0
     */
    public void     setDisableTimeoutInMinutes( final String timeout );
}
