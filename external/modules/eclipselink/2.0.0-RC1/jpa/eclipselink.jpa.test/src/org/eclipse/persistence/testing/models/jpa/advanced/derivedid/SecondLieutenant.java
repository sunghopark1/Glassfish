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
 *     04/24/2009-2.0 Guy Pelletier 
 *       - 270011: JPA 2.0 MappedById support
 *     10/21/2009-2.0 Guy Pelletier 
 *       - 290567: mappedbyid support incomplete
 ******************************************************************************/  
package org.eclipse.persistence.testing.models.jpa.advanced.derivedid;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * This model tests Example #6 of the mapsId cases.
 * 
 * @author gpelleti
 */
@Entity
@Table(name="JPA_SECOND_LIEUTENANT")
public class SecondLieutenant {
    @EmbeddedId
    LieutenantId id;
    
    @OneToOne 
    @MapsId
    Lieutenant lieutenant;
    
    public LieutenantId getId() {
        return id;
    }
    
    public Lieutenant getLieutenant() {
        return lieutenant;
    }
    
    public void setId(LieutenantId id) {
        this.id = id;
    }

    public void setLieutenant(Lieutenant lieutenant) {
        this.lieutenant = lieutenant;
    }
}
