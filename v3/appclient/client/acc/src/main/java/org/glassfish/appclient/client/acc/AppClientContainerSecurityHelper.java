/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.appclient.client.acc;

import com.sun.enterprise.container.common.spi.util.InjectionException;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Authenticator;
import java.util.Properties;
import java.util.logging.Logger;
import javax.security.auth.callback.CallbackHandler;
import org.glassfish.appclient.client.acc.callbackhandler.DefaultGUICallbackHandler;
import org.glassfish.appclient.client.acc.config.ClientCredential;
import org.glassfish.appclient.client.acc.config.TargetServer;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;

/**
 *
 * @author tjquinn
 */
@Service
@Scoped(Singleton.class)
class AppClientContainerSecurityHelper {

    private static final String ORB_INITIAL_HOST_PROPERTYNAME = "org.omg.CORBA.ORBInitialHost";
    private static final String ORB_INITIAL_PORT_PROPERTYNAME = "org.omg.CORBA.ORBInitialPort";

    @Inject
    private InjectionManager injectionManager;

    private final Logger logger = Logger.getLogger(getClass().getName());

    private final Properties iiopProperties;

    private final ClassLoader classLoader;

    private CallbackHandler userCallbackHandler = null;

    AppClientContainerSecurityHelper(
            final TargetServer[] targetServers, 
            final Properties containerProperties,
            final ClientCredential clientCredential,
            final CallbackHandler callerSuppliedCallbackHandler,
            final ClassLoader classLoader,
            final ApplicationClientDescriptor acDesc) throws InstantiationException, IllegalAccessException, InjectionException, ClassNotFoundException {

        this.classLoader = (classLoader == null) ? Thread.currentThread().getContextClassLoader() : classLoader;
        iiopProperties = prepareIIOP(targetServers);

        setClientCredentials(clientCredential);

        CallbackHandler callbackHandler = 
                initSecurity(callerSuppliedCallbackHandler, acDesc);

        initHttpAuthenticator(SomeSecurityThing.USERNAME_PASSWORD, callbackHandler);
    }

//    /**
//     * Returns the earlier-specified callback handler or a default one if
//     * none has been set.
//     * @return the callback handler to use
//     */
//    CallbackHandler getCallbackHandler() {
//        return callbackHandler;
//    }

    /**
     * Sets the callback handler for future use.
     *
     * @param callbackHandler the callback handler to be used
     */
    private CallbackHandler initSecurity(
            final CallbackHandler callerSuppliedCallbackHandler,
            final ApplicationClientDescriptor acDesc) throws InstantiationException, IllegalAccessException, InjectionException, ClassNotFoundException {

        /*
         * Choose a callback handler in this order:
         * 1. callback handler class set by the program that created the AppClientContainerConfigurator.
         * 2. callback handler class name set in the app client descriptor
         * 3. our default GUI callback handler
         *
         * Our default handler uses no injection, but a user-provided one might.
         */
        CallbackHandler callbackHandler = callerSuppliedCallbackHandler;
        if (callerSuppliedCallbackHandler == null) {
            final String descriptorCallbackHandlerClassName;
            if (acDesc != null && ((descriptorCallbackHandlerClassName = acDesc.getCallbackHandler()) != null)) {
                callbackHandler = newCallbackHandlerInstance(descriptorCallbackHandlerClassName, acDesc, classLoader);
            } else {
                callbackHandler = chooseDefaultCallbackHandler();
            }
        }
        return callbackHandler;
    }

    private CallbackHandler chooseDefaultCallbackHandler() {
        // XXX generalize this in case of textauth with missing user and/or password on cmd line
        return new DefaultGUICallbackHandler();
    }

    private CallbackHandler newCallbackHandlerInstance(final String callbackHandlerClassName,
            final ApplicationClientDescriptor acDesc,
            final ClassLoader loader) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InjectionException {

        Class callbackHandlerClass =
                Class.forName(callbackHandlerClassName, true, loader);

        return newCallbackHandlerInstance(callbackHandlerClass, acDesc);
    }

    private CallbackHandler newCallbackHandlerInstance(final Class<? extends CallbackHandler> callbackHandlerClass,
            final ApplicationClientDescriptor acDesc) throws InstantiationException, IllegalAccessException, InjectionException {

        CallbackHandlerInvocationHandler invHandler = new CallbackHandlerInvocationHandler(
                chooseDefaultCallbackHandler());
        
        CallbackHandler handlerProxy = (CallbackHandler) Proxy.newProxyInstance(
                classLoader, 
                new Class[] {CallbackHandler.class}, 
                invHandler);
        
        
        CallbackHandler userHandler = callbackHandlerClass.newInstance();
        injectionManager.injectInstance(userHandler, acDesc);
        invHandler.setDelegate(userHandler);
        return handlerProxy;
    }


    /**
     * Returns the already-initialized IIOP properties object.
     * @return Properties object containing IIOP property settings
     */
    Properties getIIOPProperties() {
        return iiopProperties;
    }

    private void initHttpAuthenticator(final int loginType, CallbackHandler callbackHandler) {
        Authenticator.setDefault(
                new HttpAuthenticator(loginType, callbackHandler));
    }
    
//    /**
//     * Creates a Properties object containing the ORB settings and, possibly,
//     * as a side-effect may assign some system property settings because that
//     * is how the ORB reads certain settings.
//     * <p>
//     * If there are multiple endpoints configured then the ACC chooses a
//     * default load balancing setting.
//     * The ACC assembled the full list of ORB settings in this order:
//     * <ol>
//     * <li>From Property objects in the ClientContainer configuration (this
//     * usage is deprecated and will be logged as such but for historical
//     * reasons is given priority)
//     * <li>From TargetServer object(s) in the ClientContainer configuration
//     * </ol>
//     * Note that the calling program should normally provide at least one
//     * TargetServer object.
//     *
//     * @return Properties object suitable as the argument to InitialContext
//     */
//    private Properties prepareIIOP(final TargetServer[] targetServers,
//            final Properties containerProperties) {
//
//        boolean isEndpointPropertySpecifiedByUser = false;
//        String loadBalancingPolicy = null;
//
//        Properties iiopProperties = new Properties();
//
//        boolean isLBEnabled = false;
//        boolean isSSLSpecifiedForATargetServer = false;
//
//	    /*
//         * Although targetServerEndpoints is for user-friendly logging
//         * we need to compute lb_enabled and also to note if any target-server
//         * specifies ssl, so the loop is multi-purpose.
//         */
//	    StringBuilder targetServerEndpoints = new StringBuilder();
//        for (TargetServer tServer : targetServers) {
//            addEndpoint(targetServerEndpoints, formatEndpoint(tServer.getAddress(), tServer.getport()));
//            isLBEnabled = true;
//		    /*
//             * In the configuration the ssl sub-part is required if the
//             * security part is present.  So for speed just look for the
//             * security part under this target server.  That will ensure that
//             * the ssl part is there also, and that's all we're concerned with
//             * at this point.
//             */
//            isSSLSpecifiedForATargetServer |= (tServer.getSecurity() != null);
//        }
//
//		if (isSSLRequired(targetServers, containerProperties)) {
//            // XXX ORBManager needed
////            ORBManager.getCSIv2Props().put(ORBManager.ORB_SSL_CLIENT_REQUIRED, "true");
//        }
//
//        /*
//         * Find and use (if it exists) the container-level property that specifies a load balancing policy.
//         */
//        // XXX S1ASCtxFactory needed
////        loadBalancingPolicy = containerProperties.getProperty(S1ASCtxFactory.LOAD_BALANCING_PROPERTY);
//        isLBEnabled |= loadBalancingPolicy != null;
//
//		logger.fine("targetServerEndpoints = " + targetServerEndpoints.toString());
//
//        if (isLBEnabled) {
//        // XXX S1ASCtxFactory needed
////            System.setProperty(S1ASCtxFactory.IIOP_ENDPOINTS_PROPERTY, targetServerEndpoints.toString());
//            /*
//             * Honor any explicit setting of the load-balancing policy.
//             * Otherwise just defer to whatever default the ORB uses.
//             */
//            if (loadBalancingPolicy != null) {
//        // XXX S1ASCtxFactory needed
////                System.setProperty(S1ASCtxFactory.LOAD_BALANCING_PROPERTY, loadBalancingPolicy);
//            }
//            /*
//             * For load-balancing the Properties object is not used to convey
//             * the LB information.  Rather,
//             * the ORB detects the system property settings.  So return a
//             * null for the LB case.
//             */
//            iiopProperties = null;
//        } else {
//            /*
//             * For the non-load-balancing case, the Properties object must
//             * contain the initial host and port settings for the ORB.
//             */
//            iiopProperties.setProperty(ORB_INITIAL_HOST_PROPERTYNAME, targetServers[0].getAddress());
//            iiopProperties.setProperty(ORB_INITIAL_PORT_PROPERTYNAME, targetServers[0].getport().toString());
//        }
//        return iiopProperties;
//    }
//    private StringBuilder addEndpoint(final StringBuilder endpointSB, final String endpoint) {
//        if (endpointSB.length() > 0) {
//            endpointSB.append(",");
//        }
//        endpointSB.append(endpoint);
//        return endpointSB;
//    }
//
//    private String formatEndpoint(final String host, final int port) {
//        return host + ":" + port;
//    }

    /**
     * Temporarily for early v3, use the FIRST host and port from the
     * target servers already prepared using the appclient command line and
     * from the properties and target server elements in the sun-acc.xml file.
     * @return ORB-related properties to define host and port for bootstrapping
     */
    private Properties prepareIIOP(final TargetServer[] targetServers) {
        Properties props = new Properties();

        props.setProperty(ORB_INITIAL_HOST_PROPERTYNAME, targetServers[0].getAddress());
        props.setProperty(ORB_INITIAL_PORT_PROPERTYNAME, Integer.toString(targetServers[0].getPort()));
        return props;

    }

    /**
     * Reports whether the ORB should be requested to use SSL.
     * <p>
     * If any TargetServer specifies SSL or the container-level properties
     * specify SSL then report "true."
     * @param targetServers configured TargetServer(s)
     * @param containerProperties configured container-level properties
     * @return whether the target servers or the properties implies the use of SSL
     */
    private boolean isSSLRequired(final TargetServer[] targetServers, final Properties containerProperties) {
        if (containerProperties != null) {
            String sslPropertyValue = containerProperties.getProperty("ssl");
            if ("required".equals(sslPropertyValue)) {
                return true;
            }
        }
        for (TargetServer ts : targetServers) {
            /*
             * If this target server has the optional security sub-item then
             * the security sub-item must have an ssl sub-item.  So we can just
             * look for the security sub-item.
             */
            if (ts.getSecurity() != null) {
                return true;
            }
        }
        return false;
    }

    private void setClientCredentials(final ClientCredential clientCredential) {

        /// XXX From original v2 code - what to do about realm?
        //UsernamePasswordStore.set(clientCredential.getUserName(), clientCredential.getPassword());
        SomeSecurityThing.setCredential(clientCredential);
    }

    /**
     * Proxy either for our default callback handler (used if needed during callbacks
     * while injecting the user's callback handler) or for the user's callback
     * handler (if the developer specified one).
     */
    private class CallbackHandlerInvocationHandler implements InvocationHandler {

        private CallbackHandler delegate;

        CallbackHandlerInvocationHandler(final CallbackHandler handler) {
            delegate = handler;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return method.invoke(delegate, args);
        }

        void setDelegate(final CallbackHandler handler) {
            delegate = handler;
        }
    }
}
