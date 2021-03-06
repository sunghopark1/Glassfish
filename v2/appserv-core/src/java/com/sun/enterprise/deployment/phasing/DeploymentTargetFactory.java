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
 * DeploymentTargetFactory.java
 *
 * Created on April 26, 2003, 3:51 PM
 * @author  sandhyae
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/enterprise/deployment/phasing/DeploymentTargetFactory.java,v $
 *
 */

package com.sun.enterprise.deployment.phasing;

import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.deployment.backend.IASDeploymentException;
import com.sun.enterprise.deployment.pluggable.DeploymentFactory;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.pluggable.PluggableFeatureFactory;

/**
 * Interface that will be implemented by different targetfactories implemented
 * for PE/EE
 * @author  Sandhya E
 */
public abstract class DeploymentTargetFactory {

    private static DeploymentTargetFactory targetFactory;

    /**
     * This is a singleton factory for the DeploymentService implementation.  We should revisit
     * this to make sure this will work in multi-threaded environment, i.e. concurrent deployment.
     */
    public static DeploymentTargetFactory getDeploymentTargetFactory() {

        if (targetFactory != null) {
            return targetFactory;
        }

        PluggableFeatureFactory featureFactory =
            ApplicationServer.getServerContext().getPluggableFeatureFactory();
        DeploymentFactory dFactory = featureFactory.getDeploymentFactory();
        targetFactory = dFactory.createDeploymentTargetFactory();
        return targetFactory;
    }
          
    /**
     * Returns the DeploymentTarget that represents a targetName
     * @param configContext config context
     * @param targetName name of the target
     * @return DeploymentTarget
     * @throws IASDeploymentException
     */
    public abstract DeploymentTarget getTarget(ConfigContext configContext , 
            String domainName, String targetName) throws IASDeploymentException;
    
    /**
     * Returns the default target
     * @param configContext config context
     * @return DeploymentTarget
     * @throws IASDeploymentException
     */
    public abstract DeploymentTarget getTarget(ConfigContext configContext, 
            String domainName) throws IASDeploymentException; 
    
    public abstract TargetType[] getValidDeploymentTargetTypes();
}
