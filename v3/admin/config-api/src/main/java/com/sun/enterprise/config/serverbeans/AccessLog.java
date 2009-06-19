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



package com.sun.enterprise.config.serverbeans;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.component.Injectable;
import java.beans.PropertyVetoException;

import org.glassfish.api.amx.AMXConfigInfo;
import org.glassfish.config.support.datatypes.PositiveInteger;
import org.glassfish.api.admin.config.PropertyBag;

import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

/* @XmlType(name = "") */
@AMXConfigInfo( amxInterfaceName="com.sun.appserv.management.config.AccessLogConfig", singleton=true)
@Configured

public interface AccessLog extends ConfigBeanProxy, Injectable, PropertyBag {

    /**
     * Gets the value of the format property.
     * The global format for the access log rotation-policy The
     * policy based on which the log rotation would be done. 
     * At this time only time based rotation is enabled.
     * 
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="%client.name% %auth-user-name% %datetime% %request% %status% %response.length%")
    String getFormat();

    /**
     * Sets the value of the format property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setFormat(String value) throws PropertyVetoException;

    /**
     * Gets the value of the rotationPolicy property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="time")
    String getRotationPolicy();

    /**
     * Sets the value of the rotationPolicy property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setRotationPolicy(String value) throws PropertyVetoException;

    /**
     * Gets the value of the rotationIntervalInMinutes property.
     * The time interval in minutes between two successive rotations of the
     * access logs.
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="1440")
    @Min(value=1)
    @Max(value=Integer.MAX_VALUE)
    String getRotationIntervalInMinutes();

    /**
     * Sets the value of the rotationIntervalInMinutes property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setRotationIntervalInMinutes(String value) throws PropertyVetoException;

    /**
     * Gets the value of the rotationSuffix property.
     * The suffix to be added to the access-log name after rotation.
     * Acceptable values include those supported by
     * java.text.SimpleDateFormat and "%YYYY;%MM;%DD;-%hh;h%mm;m%ss;s".
     * 
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="yyyyMMdd")
    String getRotationSuffix();

    /**
     * Sets the value of the rotationSuffix property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setRotationSuffix(String value) throws PropertyVetoException;

    /**
     * Gets the value of the rotationEnabled property.
     * The flag for enabling the access-log rotation
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="true", dataType=Boolean.class)
    String getRotationEnabled();

    /**
     * Sets the value of the rotationEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setRotationEnabled(String value) throws PropertyVetoException;

    /**
     * Size in bytes of the buffer where access log calls are stored. If the value is less than 5120, a warning
     *  message is issued, and the value is set to 5120
     */
    @Attribute(defaultValue = "32768")
    String getBufferSizeBytes();

    void setBufferSizeBytes(String value);

    /**
     * Number of seconds before the log is written to the disk. The access log is written when the buffer is
     * full or when the interval expires. If the value is 0, the buffer is always written even if it is not full.
     * This means that each time the server is accessed, the log message is stored directly to the file
     * @return
     */
    @Attribute(defaultValue = "300")
    String getWriteIntervalSeconds();
    
    void setWriteIntervalSeconds(String value);
}