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
 * ClientContainerCallbackHandler.java
 *
 * Created on September 14, 2004, 12:20 PM
 */

package com.sun.enterprise.security.jmac.callback;

import java.io.IOException;
import java.util.logging.Level;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.ChoiceCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.CertStoreCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import javax.security.auth.message.callback.PasswordValidationCallback;
import javax.security.auth.message.callback.PrivateKeyCallback;
import javax.security.auth.message.callback.SecretKeyCallback;
import javax.security.auth.message.callback.TrustStoreCallback;

import com.sun.enterprise.appclient.AppContainer;
import com.sun.enterprise.security.UsernamePasswordStore;

/**
 * Appclient side Callback Handler for WSS.
 * @author  Harpreet Singh
 * @author  Shing Wai Chan
 */
final class ClientContainerCallbackHandler
        extends BaseContainerCallbackHandler {

    private static final String LOGIN_NAME = "j2eelogin.name";
    private static final String LOGIN_PASSWORD = "j2eelogin.password";

    ClientContainerCallbackHandler() {
    }

    protected void handleSupportedCallbacks(Callback[] callbacks) 
            throws IOException, UnsupportedCallbackException {

        // this variable is set to true if we have used the older jaas
        // mechanisms to process the callbacks - and we will not need 
        // to process further as the inside loop, just takes care
        // of processing all callbacks
        boolean processedSomeAppclientCallbacks = false;
        
        for (int i=0; i < callbacks.length; i++) {
            if (!processedSomeAppclientCallbacks) {
                if ((callbacks[i] instanceof NameCallback) ||
                        (callbacks[i] instanceof PasswordCallback) ||
                        (callbacks[i] instanceof ChoiceCallback)) {

                    String loginName = UsernamePasswordStore.getUsername();
                    String password = UsernamePasswordStore.getPassword();
                    boolean doSet = false;
                    if (loginName == null) {
                        loginName = System.getProperty(LOGIN_NAME);
                        doSet = true;
                    }
                    if (password == null) {
                        password = System.getProperty(LOGIN_PASSWORD);
                        doSet = true;
                    }
                    if (doSet) {
                        UsernamePasswordStore.set(loginName, password);
                    }
                    CallbackHandler callbackHandler = AppContainer.getCallbackHandler();
                    if(loginName != null && password != null){
                        // username/password set already
                        for(int j = 0; j < callbacks.length; j++){
                            if(callbacks[j] instanceof NameCallback){
                                NameCallback nc = (NameCallback)callbacks[j];
                                nc.setName(loginName);
                            } else if (callbacks[j] instanceof PasswordCallback){
                                PasswordCallback pc = (PasswordCallback)callbacks[j];
                                pc.setPassword(password.toCharArray());
                            }
                        }
                    } else{
                        // once this is called all callbacks will be handled by
                        // callbackHandler and then we dont have to check for
                        // NameCallback PasswordCallback and ChoiceCallback
                        // again.
                        // Let control flow to the callback processors
                        callbackHandler.handle(callbacks);
                    }
                    processedSomeAppclientCallbacks = true;
                    break;
                }
            }
            processCallback(callbacks[i]);
        }
    }

    protected boolean isSupportedCallback(Callback callback) 
    {
       boolean supported = false;
        if (callback instanceof NameCallback ||
                callback instanceof PasswordCallback ||
                callback instanceof ChoiceCallback ||
                callback instanceof CallerPrincipalCallback ||
                callback instanceof GroupPrincipalCallback ||
                callback instanceof CertStoreCallback ||
                callback instanceof PasswordValidationCallback ||
                callback instanceof SecretKeyCallback ||
                callback instanceof PrivateKeyCallback ||
                callback instanceof TrustStoreCallback) {
            supported = true;
        }
        return supported;
    }
    
}
