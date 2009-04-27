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
package org.glassfish.admin.amx.impl.path;

import org.glassfish.admin.amx.base.*;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.core.Util;

import org.glassfish.admin.amx.util.jmx.JMXUtil;

import javax.management.ObjectName;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import org.glassfish.admin.amx.core.AMXConstants;
import org.glassfish.admin.amx.core.PathnameParser;
import org.glassfish.admin.amx.core.proxy.AMXProxyHandler;
import org.glassfish.admin.amx.impl.mbean.AMXImplBase;
import org.glassfish.admin.amx.util.CollectionUtil;
import org.glassfish.admin.amx.util.ListUtil;

/**
  GlassFish V3 dotted names implementation (MBean).
 */
 
public final class PathnamesImpl  extends AMXImplBase
	// implements Pathnames  (can't directly implement the interface)
{
    private static final int MAX_CACHE_SIZE = 1024;
    private final ConcurrentMap<String,ObjectName> mPathnameCache = new ConcurrentHashMap<String,ObjectName>();
    
		public
	PathnamesImpl( final ObjectName parentObjectName )
	{
        super( parentObjectName, Pathnames.class );
	}

        public ObjectName
    resolvePath( final String path )
    {
        ObjectName result = mPathnameCache.get(path);
        if ( result != null ) return result;
        
        if ( path.equals( DomainRoot.PATH ) )
        {
            return getDomainRoot();
        }
        
        final PathnameParser parser = new PathnameParser(path);
        
        final String parentPath = parser.parentPath();
        
        //cdebug( "resolvePath: " + parser.toString() + ", parentPath = " + parentPath );
        
        // fixed query based on the path, which will find all MBeans with that parent path
        final String props = Util.makeProp( AMXConstants.PARENT_PATH_KEY, parentPath );
        final ObjectName pattern = JMXUtil.newObjectNamePattern( getObjectName().getDomain(), props );
        final Set<ObjectName> s = getMBeanServer().queryNames( pattern, null);
        
        //cdebug( "resolvePath: " + path + " = query for parent " + pattern );
        
        ObjectName objectName = null;
        final String type = parser.type();
        final String name = parser.name();
        // find the matching child
        for( final ObjectName child : s )
        {
            if ( type.equals(  Util.getTypeProp(child) ) )
            {
                final String nameProp = Util.getNameProp(child);
                
                //cdebug( "type match for " + path + ", objectName = " + child);
                    
                if ( nameProp == null )
                {
                    if ( name == null )
                    {
                        // no name, we matched on type alone
                        objectName = child;
                        break;
                    }
                    // badly formed: a name is specified, but none is present for this type
                    cdebug( "A name is specified in path, but the type has none: path = " + path + ", objectName = " + child);
                    continue;
                }
                
                // there is a name in the ObjectName (nameProp)
                // the nameProp could exist, but the item might be a singleton
                if ( name != null && name.equals( nameProp ) )
                {
                    objectName = child;
                    break;
                }
                                
                // careful: name should be used only if it's not a singleton
                final MBeanInfo mbeanInfo = getProxyFactory().getMBeanInfo(child);
                final boolean singleton = AMXProxyHandler.singleton( mbeanInfo );
                // match only if it's a singleton with no name; the check above handled the other cases
                if ( singleton && name == null )
                {
                    objectName = child;
                    break;
                }
            }
            else
            {
                //cdebug( "No match on type: " + type + " != " + Util.getTypeProp(child) );
            }
        }
        
        // limit the memory use; non-existent paths could otherwise build up
        if ( mPathnameCache.keySet().size() > MAX_CACHE_SIZE )
        {
            mPathnameCache.clear();
        }
        
        if ( objectName != null )
        {
            mPathnameCache.put( path, objectName );
        }
        
        return objectName;
    }
    
    private AMXProxy resolveToProxy( final String path )
    {
        final ObjectName objectName = resolvePath(path);
        if ( objectName == null ) return null;
        
        return getProxyFactory().getProxy(objectName, AMXProxy.class);
    }
    
        public ObjectName[]
    resolvePaths( final String[] paths )
    {
        final ObjectName[] objectNames = new ObjectName[paths.length];
        
        int i = 0;
        for( final String path : paths )
        {
            try
            {
                objectNames[i] = resolvePath(path);
            }
            catch( final Exception e )
            {
                objectNames[i] = null;
            }
            ++i;
        }
        return objectNames;
    }

        
        public ObjectName[]
    ancestors( final String path )
    {
        final ObjectName objectName = resolvePath(path);
        if ( objectName == null )
        {
            return null;
        }
        return ancestors(objectName);
    }
        
        public ObjectName[]
    ancestors( final ObjectName mbean )
    {
        final List<ObjectName> objectNames = new ArrayList<ObjectName>();
        final MBeanServer server = getMBeanServer();

        ObjectName cur = mbean;
        while ( cur != null )
        {
            objectNames.add(cur);
            cur = (ObjectName)JMXUtil.getAttribute(server, cur, AMXConstants.ATTR_PARENT);
        }
        
        final List<ObjectName> reversed = ListUtil.reverse(objectNames);
        final ObjectName[] ancestors = new ObjectName[reversed.size()];
        reversed.toArray(ancestors);
        return ancestors;
    }
    
    private List<AMXProxy> listChildren( final AMXProxy top, final List<AMXProxy> list, boolean recursive)
    {
        final Set<AMXProxy> children = top.childrenSet();
        if  ( children == null ) return null;
        
        for( final AMXProxy child : children )
        {
            list.add( child );
            if ( recursive )
            {
                listChildren( child, list, true );
            }
        }
        return list;
    }
    
    private List<AMXProxy> listChildren( final String path, boolean recursive)
    {
        final AMXProxy topProxy = resolveToProxy(path);
        if ( topProxy == null ) return null;
        
        return listChildren(topProxy,  new ArrayList<AMXProxy>(), recursive);
    }
    
    public String[] getAllPathnames()
    {
        final String[] allButRoot = listPaths( DomainRoot.PATH, true );
        
        final String[] all = new String[ allButRoot.length + 1 ];
        all[0] = DomainRoot.PATH;
		System.arraycopy( allButRoot, 0, all, 1, allButRoot.length );
        
        return all;
    }
    
    public ObjectName[]  listObjectNames( final String path, final boolean recursive)
    {
        final List<AMXProxy> list  = listChildren(path,  recursive);
        final List<ObjectName> objectNames = Util.toObjectNames(list);
        return CollectionUtil.toArray( objectNames, ObjectName.class);
    }
    
    public String[] listPaths( final String path, final boolean recursive )
    {
        final List<AMXProxy> list  = listChildren(path,  recursive);
        
        final List<String> paths = new ArrayList<String>();
        for ( final AMXProxy amx: list )
        {
            paths.add( amx.path() );
        }
        
        return CollectionUtil.toArray(paths, String.class);
    }
   
    public String dump( final String path )
    {
        final ObjectName top = resolvePath(path);
        
        final AMXProxy topProxy = getProxyFactory().getProxy(top, AMXProxy.class);
        final List<AMXProxy> list  = new ArrayList<AMXProxy>();
        list.add( topProxy);
        listChildren(topProxy, list, true);
        
        final String NL = "\n";
        final StringBuffer buf = new StringBuffer();
        for( final AMXProxy amx : list )
        {
            final String p = amx.path();
            buf.append(p);
            buf.append(NL);
            
            final Map<String,Object> attributesMap = amx.attributesMap();
            for( final String name : attributesMap.keySet() )
            {
                buf.append("\t");
                buf.append(name);
                buf.append(" = ");
                buf.append( "" + attributesMap.get(name));
                buf.append(NL);
            }
            buf.append(NL);
        }
        return buf.toString();
    }
}

















