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
package com.sun.enterprise.repository;

import java.io.Serializable;

/**
 * Resource info for ExternalJndiResource.
 * IASRI #4626188
 * <p><b>NOT THREAD SAFE: mutable instance variables</b>
 * @author Sridatta Viswanath
 */
public class ExternalJndiResource extends J2EEResourceBase implements Serializable {
    
    private String jndiLookupName_;
    private String resType_;
    private String factoryClass_;
    
    public ExternalJndiResource(String name) {
        super(name);
    }
    
    protected J2EEResource doClone(String name) {
        ExternalJndiResource clone = new ExternalJndiResource(name);
        clone.setJndiLookupName(getJndiLookupName());
        clone.setResType(getResType());
        clone.setFactoryClass(getFactoryClass());
        return clone;
    }
    
    public int getType() {
        return J2EEResource.EXTERNAL_JNDI_RESOURCE;
    }
    
    public String getJndiLookupName() {
        return jndiLookupName_;
    }
    
    public void setJndiLookupName(String jndiLookupName) {
        jndiLookupName_ = jndiLookupName;
    }
    
    public String getResType() {
        return resType_;
    }
    
    public void setResType(String resType) {
        resType_ = resType;
    }
    
    public String getFactoryClass() {
        return factoryClass_;
    }
    
    public void setFactoryClass(String factoryClass) {
        factoryClass_ = factoryClass;
    }
    
    //START OF IASRI 4660565
    public boolean isJMSConnectionFactory() {
        if (resType_ == null) return false;

        return (IASJ2EEResourceFactoryImpl.JMS_QUEUE_CONNECTION_FACTORY.equals(resType_) ||
                IASJ2EEResourceFactoryImpl.JMS_TOPIC_CONNECTION_FACTORY.equals(resType_)); 
    }
    //END OF IASRI 4660565

    public String toString() {
        return "< External Jndi Resource : " + getName() + " , " + getJndiLookupName() + "... >";    
    }
}
