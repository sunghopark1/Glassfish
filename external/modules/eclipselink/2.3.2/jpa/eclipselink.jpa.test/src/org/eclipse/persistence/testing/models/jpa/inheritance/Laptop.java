/*******************************************************************************
 * Copyright (c) 1998, 2011 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.models.jpa.inheritance;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumns;
import javax.persistence.Table;
import javax.persistence.PrimaryKeyJoinColumn;

@Entity
@Table(name="CMP3_LAPTOP")
@PrimaryKeyJoinColumns({
    @PrimaryKeyJoinColumn(name="MFR", referencedColumnName="MFR"),
    @PrimaryKeyJoinColumn(name="SNO", referencedColumnName="SNO")
})
public class Laptop extends Computer {

    public Laptop() {
    }

    public Laptop(ComputerPK computerPK) {
        super(computerPK);
    }
    
}
