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
package org.glassfish.admin.amx.mbean;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanInfo;
import javax.management.AttributeList;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.ReflectionException;

import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;


import org.jvnet.hk2.config.ConfigBean;

/**
	Delegate which delegates to another MBean.
 */
public final class DelegateToConfigBeanDelegate extends DelegateBase
{
	private final ConfigBean mConfigBean;
    
    private static void debug( final String s ) { System.out.println(s); }
	
		public
	DelegateToConfigBeanDelegate(
        final ConfigBean configBean )
	{
		super( "DelegateToConfigBeanDelegate." + configBean.toString() );
		
		mConfigBean	= configBean;
	}
	
     
    /**
        Get the XML attribute name corresponding to the AMX attribute name.
     */
        private final String
    getXMLName( final String amxName )
    {
        String xmlName = NameMapping.getXMLName( amxName );
        if ( xmlName == null )
        {
            final Set<String> xmlNames = mConfigBean.getAttributeNames();
            //debug( "Attribute names: " + CollectionUtil.toString( xmlNames ) );
            
            xmlName = NameMapping.matchAMXName( amxName, xmlNames );
            debug( "Matched: " + amxName + " => " + xmlName );
        }
        
        //debug( "amxAttrNameToConfigBeanName: resolved as : " + xmlName );
        return xmlName;
    }
    
            
		public final Object
	getAttribute( final String attrName )
		throws AttributeNotFoundException
	{
        //debug( "DelegateToConfigBeanDelegate.getAttribute: " + attrName );
        final String xmlName = getXMLName(attrName);
        
        return mConfigBean.rawAttribute( xmlName );
	}
    
    private static final String[]   SINGLE_STRING_SIG   = new String[] { String.class.getName() };
    
    @Override
        protected final String
    _getDefaultValue( final String name )
        throws AttributeNotFoundException
    {
        throw new AttributeNotFoundException( name );
    }
	
		public void
	setAttribute( final Attribute attr )
		throws AttributeNotFoundException, InvalidAttributeValueException
	{
        mConfigBean.attribute( getXMLName(attr.getName()), "" + attr.getValue() );
	}
	
		public MBeanInfo
	getMBeanInfo()
	{
		return( null );
	}
    
		private void
	delegateFailed( final Throwable t )
	{
		if ( getOwner() != null )
		{
			getOwner().delegateFailed( t );
		}
	}

	/**
	 */
		public final Object
	invoke(
		String 		operationName,
		Object[]	args,
		String[]	types )
	{
        throw new RuntimeException( "invoke() not yet implemented" );
	}
}








