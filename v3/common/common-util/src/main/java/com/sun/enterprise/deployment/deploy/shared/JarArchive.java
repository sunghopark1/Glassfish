/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.deployment.deploy.shared;


import java.net.URI;
import org.glassfish.api.deployment.archive.Archive;
import java.util.Enumeration;
import java.util.Vector;

/**
 * This abstract class contains all common implementation of the
 * Archive/WritableArchive interfaces for Jar files
 *
 * @author Jerome Dochez
 */
public abstract class JarArchive implements Archive {


    /**
     * Returns an enumeration of the module file entries with the
     * specified prefix.  All elements in the enumeration are of
     * type String.  Each String represents a file name relative
     * to the root of the module.
     *
     * @param prefix the prefix of entries to be included
     * @return an enumeration of the archive file entries.
     */
    public Enumeration entries(String prefix) {
        Enumeration allEntries = entries();
        Vector entries = new Vector();
        while (allEntries.hasMoreElements()) {
            String name = (String) allEntries.nextElement();
            if (name != null && name.startsWith(prefix)) {
                entries.add(name);
            }
        }
        return entries.elements();
    } 
    
   /**
     * Returns the name portion of the archive's URI.
     * <p>
     * For JarArhive the name is all of the path that follows
     * the last slash up to but not including the last dot.
     * <p>
     * Here are some example archive names for the specified JarArchive paths:
     * <ul>
     * <li>/a/b/c/d.jar -> d
     * <li>/a/b/c/d  -> d
     * <li>/x/y/z.html -> z
     * </ul>
     * @return the name of the archive
     * 
     */
    public String getName() {
         String path = Util.getURIName(getURI());
         int lastDot = path.lastIndexOf('.');
         int endOfName = (lastDot != 1) ? lastDot : path.length();
         String name = path.substring(0, endOfName);
         return name;
    }    
}
