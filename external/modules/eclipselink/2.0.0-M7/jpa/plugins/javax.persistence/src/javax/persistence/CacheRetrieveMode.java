/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     gyorke - Java Persistence 2.0 - Post Proposed Final Draft (March 13, 2009) Updates
 *               Specification available from http://jcp.org/en/jsr/detail?id=317
 *
 * Java(TM) Persistence API, Version 2.0 - EARLY ACCESS
 * This is an implementation of an early-draft specification developed under the 
 * Java Community Process (JCP).  The code is untested and presumed not to be a  
 * compatible implementation of JSR 317: Java(TM) Persistence API, Version 2.0.   
 * We encourage you to migrate to an implementation of the Java(TM) Persistence 
 * API, Version 2.0 Specification that has been tested and verified to be compatible 
 * as soon as such an implementation is available, and we encourage you to retain 
 * this notice in any implementation of Java(TM) Persistence API, Version 2.0 
 * Specification that you distribute.
 ******************************************************************************/
package javax.persistence;

/**
 * The <code>javax.persistence.CacheRetrieveMode</code> property specifies the
 * behavior when data is retrieved by the find methods and by the execution of
 * queries. The cacheRetrieveMode property is ignored for the refresh method, 
 * which always causes data to be retrieved from the database, not the cache.
 * 
 * @since Java Persistence 2.0
 */
public enum CacheRetrieveMode {
    /**
     * Read entity data from the cache: this is the default behavior.
     */
    USE,
    /**
     * Bypass the cache: get data directly from the database.
     */
    BYPASS
}