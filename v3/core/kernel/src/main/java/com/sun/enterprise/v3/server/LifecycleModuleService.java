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

package com.sun.enterprise.v3.server;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.List;

import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.ServerTags;

import com.sun.appserv.server.ServerLifecycleException;
import com.sun.appserv.server.LifecycleListener;

import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.api.ServerContext;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;
import org.glassfish.api.Startup;

/**
 * Support class to assist in firing LifecycleEvent notifications to
 * registered LifecycleListeners.
 */
@Service
public class LifecycleModuleService implements Startup, PreDestroy, PostConstruct, EventListener {

    @Inject
    ServerContext context;

    @Inject 
    Applications apps;

    @Inject
    Events events;

    // the property to indicate lifecycle module
    private final static String IS_LIFECYCLE = "isLifecycle";

    /**
     * The set of registered LifecycleListeners for event notifications.
     */
    private ArrayList listeners = new ArrayList();
    
    public void postConstruct() {
        events.register(this);
        try {
            onInitialization();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void preDestroy() {
        try {
            onTermination();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Startup.Lifecycle getLifecycle() {
        return Startup.Lifecycle.SERVER;
    }

    public void event(Event event) {
        try {
            if (event.is(EventTypes.SERVER_STARTUP)) {
                onStartup();
            } else if (event.is(EventTypes.SERVER_READY)) {
                onReady();
            } else if (event.is(EventTypes.PREPARE_SHUTDOWN)) {
                onShutdown();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void onInitialization() throws ServerLifecycleException {
        try {
            List<Application> applications = apps.getApplications();
            List<Application> lcms = new ArrayList<Application>();;
            for (Application app : applications) {
                if (Boolean.valueOf(app.getDeployProperties().getProperty
                    (IS_LIFECYCLE))) {
                    lcms.add(app);
                }
            }

            HashSet listenerSet = new HashSet();
            for (Application next : lcms) {
                Properties props = next.getDeployProperties();
                String enabled = next.getEnabled();
                if ( isEnabled(next.getName(), enabled) ) {
                    String strOrder = (String)props.remove(
                        ServerTags.LOAD_ORDER); 

                    int order = Integer.MAX_VALUE;
                    if (strOrder != null) {
                        try {
                            order = Integer.parseInt(strOrder);
                        } catch(NumberFormatException nfe) {
                            nfe.printStackTrace();
                        }
                    }

                    String className = (String)props.remove(
                        ServerTags.CLASS_NAME);
                    ServerLifecycleModule slcm = 
                        new ServerLifecycleModule(context, 
                                    next.getName(), className);

                    slcm.setLoadOrder(order);

                    String classpath = (String)props.remove(
                        ServerTags.CLASSPATH);
                    slcm.setClasspath(classpath);

                    String isFailureFatal = (String)props.remove(
                        ServerTags.IS_FAILURE_FATAL);
                    slcm.setIsFatal(Boolean.valueOf(isFailureFatal));

                    props.remove(IS_LIFECYCLE);
                    props.remove(ServerTags.OBJECT_TYPE);
                        
                    for (String propName : props.stringPropertyNames()) {
                        slcm.setProperty(propName, props.getProperty(propName));
                    }

                    LifecycleListener listener = slcm.loadServerLifecycle();
                    listenerSet.add(slcm);
                }
            }
            sortModules(listenerSet);
        } catch(Exception ce1) {
            // FIXME eat it?
            ce1.printStackTrace();
        }

        initialize();
    }

    /**
     * Returns true if life cycle module is enabled in the application
     * level and in the application ref level.
     *
     * @return  true if life cycle module is enabled
     */
    private boolean isEnabled(String name, String enabled) {

        // true if enabled in both lifecyle module and in the ref
        return (Boolean.valueOf(enabled) && 
            Boolean.valueOf(ConfigBeansUtilities.getEnabled(
                "server", name)));
    }

    private void resetClassLoader(final ClassLoader c) {
         // set the common class loader as the thread context class loader
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
                public Object run() {
                    Thread.currentThread().setContextClassLoader(c);
                    return null;
                }
            }
        );
    }
    
    private void sortModules(HashSet listenerSet) {
        // FIXME: use a better sorting algo
        for(Iterator iter = listenerSet.iterator(); iter.hasNext();) {
            ServerLifecycleModule next = (ServerLifecycleModule) iter.next();
            int order = next.getLoadOrder();
            int i=0;
            for(;i<this.listeners.size();i++) {
                if(((ServerLifecycleModule)listeners.get(i)).getLoadOrder() > order) {
                    break;
                }
            }
            this.listeners.add(i,next);
        }
    }
    
    private void initialize() 
                            throws ServerLifecycleException {

        if (listeners.isEmpty())
            return;

        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        for(Iterator iter = listeners.iterator(); iter.hasNext();) {
            ServerLifecycleModule next = (ServerLifecycleModule) iter.next();
            next.onInitialization();
        }
        // set it back
        resetClassLoader(cl);
    }
    
    private void onStartup() throws ServerLifecycleException {

        if (listeners.isEmpty())
            return;

        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        for(Iterator iter = listeners.iterator(); iter.hasNext();) {
            ServerLifecycleModule next = (ServerLifecycleModule) iter.next();
            next.onStartup();
        }
        // set it back
        resetClassLoader(cl);
    }
    
    private void onReady() throws ServerLifecycleException {

        if (listeners.isEmpty())
            return;

        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        for(Iterator iter = listeners.iterator(); iter.hasNext();) {
            ServerLifecycleModule next = (ServerLifecycleModule) iter.next();
            next.onReady();
        }
        // set it back
        resetClassLoader(cl);
    }

    private void onShutdown() throws ServerLifecycleException {

        if (listeners.isEmpty())
            return;

        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        for(Iterator iter = listeners.iterator(); iter.hasNext();) {
            ServerLifecycleModule next = (ServerLifecycleModule) iter.next();
            next.onShutdown();
        }
        // set it back
        resetClassLoader(cl);
    }
    
    private void onTermination() throws ServerLifecycleException {

        if (listeners.isEmpty())
            return;

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        for(Iterator iter = listeners.iterator(); iter.hasNext();) {
            ServerLifecycleModule next = (ServerLifecycleModule) iter.next();
            next.onTermination();
        }
        // set it back
        resetClassLoader(cl);
    }
}
