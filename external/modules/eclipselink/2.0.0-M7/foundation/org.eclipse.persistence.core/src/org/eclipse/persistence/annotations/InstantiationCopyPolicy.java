/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     tware - March 25/2008 - 1.0M6 - Initial implementation
 ******************************************************************************/  

package org.eclipse.persistence.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An InstantiationCopyPolicy is used to set an 
 * org.eclipse.persistence.descriptors.copying.InstantiationCopyPolicy on an 
 * Entity. InstantiationCopyPolicy is the default CopyPolicy in EclipseLink and 
 * therefore this configuration option is only used to override other types of 
 * copy policies
 * 
 * An InstantiationCopyPolicy should be specified on an Entity,
 * MappedSuperclass or Embeddable.
 * 
 * Example:
 * @Entity
 * @InstantiationCopyPolicy 
 *
 * @see org.eclipse.persistence.descriptors.copying.InstantiationCopyPolicy
 * @see org.eclipse.persistence.annotations.CloneCopyPolicy
 * @see org.eclipse.persistence.annotations.CopyPolicy
 * 
 * @author tware
 */
@Target({TYPE})
@Retention(RUNTIME)
public @interface InstantiationCopyPolicy {
}
