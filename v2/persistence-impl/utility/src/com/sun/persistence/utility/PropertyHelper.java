/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */


package com.sun.persistence.utility;

import com.sun.persistence.utility.logging.Logger;
import com.sun.org.apache.jdo.util.I18NHelper;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * @author Mitesh Meswani This class provides helper to load reources into
 *         property object.
 */
public class PropertyHelper {

    /**
     * The logger.
     */
    private static final Logger logger = LogHelperUtility.getLogger();

    /**
     * I18N message handler.
     */
    private final static I18NHelper messages = I18NHelper.getInstance(
            "com.sun.persistence.utility.Bundle", // NOI18N
            PropertyHelper.class.getClassLoader());

    /**
     * Loads properties list from the specified resource into specified
     * Properties object.
     * @param properties Properties object to load
     * @param resourceName Name of resource.
     * @param classLoader The class loader that should be used to load the
     * resource. If null,primordial class loader is used.
     */
    public static void loadFromResource(Properties properties,
            String resourceName, ClassLoader classLoader) throws IOException {
        load(properties, resourceName, false, classLoader);
    }

    /**
     * Loads properties list from the specified file into specified Properties
     * object.
     * @param properties Properties object to load
     * @param fileName Fully qualified path name to the file.
     */
    public static void loadFromFile(Properties properties, String fileName)
            throws IOException {
        load(properties, fileName, true, null);
    }

    /**
     * Loads properties list from the specified resource into specified
     * Properties object.
     * @param resourceName Name of resource. If loadFromFile  is true, this is
     * fully qualified path name to a file. param classLoader is ignored. If
     * loadFromFile  is false,this is resource name.
     * @param classLoader The class loader that should be used to load the
     * resource. If null,primordial class loader is used.
     * @param properties Properties object to load
     * @param loadFromFile true if resourcename is to be treated as file name.
     */
    private static void load(Properties properties, final String resourceName,
            final boolean loadFromFile, final ClassLoader classLoader)
            throws IOException {

        InputStream bin = null;
        InputStream in = null;
        boolean debug = logger.isLoggable();

        if (debug) {
            Object[] items = new Object[]{resourceName,
                                          Boolean.valueOf(loadFromFile)};
            logger.fine("utility.PropertyHelper.load", items); // NOI18N
        }

        in =
                loadFromFile
                ? openFileInputStream(resourceName)
                : openResourceInputStream(resourceName, classLoader);
        if (in == null) {
            throw new IOException(
                    messages.msg(
                            "utility.PropertyHelper.failedToLoadResource", // NOI18N
                            resourceName));
        }
        bin = new BufferedInputStream(in);
        try {
            properties.load(bin);
        } finally {
            try {
                bin.close();
            } catch (Exception e) {
                // no action
            }
        }
    }

    /**
     * Open fileName as input stream inside doPriviledged block
     */
    private static InputStream openFileInputStream(final String fileName)
            throws java.io.FileNotFoundException {
        try {
            return (InputStream) AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws FileNotFoundException {
                            return new FileInputStream(fileName);
                        }
                    });
        } catch (PrivilegedActionException e) {
            // e.getException() should be an instance of FileNotFoundException,
            // as only "checked" exceptions will be "wrapped" in a
            // PrivilegedActionException.
            throw (FileNotFoundException) e.getException();
        }

    }

    /**
     * Open resourcenName as input stream inside doPriviledged block
     */
    private static InputStream openResourceInputStream(
            final String resourceName, final ClassLoader classLoader)
            throws java.io.FileNotFoundException {
        return (InputStream) AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        if (classLoader != null) {
                            return classLoader.getResourceAsStream(
                                    resourceName);
                        } else {
                            return ClassLoader.getSystemResourceAsStream(
                                    resourceName);
                        }
                    }
                });
    }

}
