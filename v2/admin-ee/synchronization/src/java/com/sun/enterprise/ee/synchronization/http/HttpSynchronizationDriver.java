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
package com.sun.enterprise.ee.synchronization.http;

import com.sun.enterprise.ee.synchronization.Ping;
import com.sun.enterprise.ee.synchronization.SynchronizationRequest;
import com.sun.enterprise.ee.synchronization.RequestMediator;
import com.sun.enterprise.ee.synchronization.BaseSynchronizationDriver;
import com.sun.enterprise.ee.admin.servermgmt.DASPropertyReader;
import com.sun.enterprise.ee.synchronization.tx.Transaction;

/**
 * This synchronization driver implementation based on HTTP.
 * 
 * @author Nazrul Islam
 * @since  JDK1.4
 */
public class HttpSynchronizationDriver extends BaseSynchronizationDriver {
    
    /**
     * Initializes the variables.
     * 
     * @param   root  server instance root
     * @param   xml   meta file name that describes the synchronization requests
     * @param   dpr   DAS property reader
     * @param   url   synchronization servlet url
     */
    public HttpSynchronizationDriver(String root, String xml, 
            DASPropertyReader dpr, String url) {

        _instanceRoot = root;
        _metaFile     = xml;
        _dpr          = dpr;
        _url          = url;
    }

    /**
     * Returns a concrete implementation of Ping interface that 
     * can determine if DAS is alive.
     *
     * @return concrete implementation of Ping interface
     */
    protected Ping getPingCommand() {
        return new HttpPingCommand(_dpr, _url);
    }

    /**
     * Returns a concrete implementation of RequestMediator interface.
     * 
     * @param  req  synchronization request object
     * @param  tx   a synch transaction 
     */
    protected RequestMediator getRequestMediator(
            SynchronizationRequest req, Transaction tx) {

        return new HttpRequestMediator(_dpr, req, tx, _url); 
    }

    // ---- PRIVATE - VARIABLES ----------------------------
    private String _url = null;
}
