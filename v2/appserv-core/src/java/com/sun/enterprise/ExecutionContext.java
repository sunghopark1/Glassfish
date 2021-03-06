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
package com.sun.enterprise;

import java.lang.InheritableThreadLocal;
import java.util.Hashtable;

/**
 * This class that implements ExecutionContext that gets 
 * stored in Thread Local Storage. If the current thread creates
 * child threads, the context info that is  stored in the current 
 * thread is automatically propogated to the child threads.
 * 
 * Two class methods serve as a convinient way to set/get the 
 * Context information within the current thread.   
 *
 * Thread Local Storage is a concept introduced in JDK1.2. So, it
 * will not work on earlier releases of JDK.
 *
 * @see java.lang.ThreadLocal
 * @see java.lang.InheritableThreadLocal
 * 
 */
public class ExecutionContext {
    private static InheritableThreadLocal current= new InheritableThreadLocal();

    /** 
     * This method can be used to add a new hashtable for storing the 
     * Thread specific context information. This method is useful to add a 
     * deserialized Context information that arrived over the wire.
     * @param A hashtable that stores the current thread's context
     * information.
     */
    public static void setContext(Hashtable ctxTable) {
	if (ctxTable != null) {
	    current.set(ctxTable);
	} else {
	    current.set(new Hashtable());
	}
    }

    /**
     * This method returns the hashtable that stores the thread specific
     * Context information.
     * @return The Context object stored in the current TLS. It always 
     * returns a non null value;
     */
    public static Hashtable getContext() {
	 if (current.get() == null) {
	     setContext(null); // Create a new one...
	 } 
	 return (Hashtable) current.get();
    }
}





