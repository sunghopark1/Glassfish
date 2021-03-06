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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */
/*
 * HAOnDemandPersistentManager.java
 *
 * Created on January 23, 2006, 10:59 AM
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

//START OF 6364900
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
//END OF 6364900

/**
 *
 * @author lwhite
 */
public class HAOnDemandPersistentManager extends HAWebEventPersistentManager {
    
    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "HAOnDemandPersistentManager/1.0";


    /**
     * The descriptive name of this Manager implementation (for logging).
     */
    private static final String name = "HAOnDemandPersistentManager"; 
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    private static final Logger _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);    
    
    /** Creates a new instance of HAOnDemandPersistentManager */
    public HAOnDemandPersistentManager() {
        super();
        /*
        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
        }
         */         
    }
    
    //START OF 6364900
    public void postRequestDispatcherProcess(ServletRequest request, ServletResponse response) {
        return;
    }
    //END OF 6364900   
    
}
