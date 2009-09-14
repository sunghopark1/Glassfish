/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.web.plugin.common;

import com.sun.enterprise.config.serverbeans.Engine;
import java.beans.PropertyVetoException;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Sets the value of an env-entry customization for a web application.
 * 
 * @author tjquinn
 */
@Service(name="set-web-env-entry")
@I18n("setWebEnvEntry.command")
@Scoped(PerLookup.class)public class SetWebEnvEntryCommand extends WebEnvEntryCommand {

    @Param(name="name")
    private String name;

    @Param(name="value",optional=true)
    private String value;

    @Param(name="type",optional=true)
    private String envEntryType;

    @Param(name="description",optional=true)
    private String description;

    @Param(name="ignoreDescriptorItem", optional=true)
    private Boolean ignoreDescriptorItem;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();

        try {
            setEnvEntry(engine(report),
                    name, description, ignoreDescriptorItem, value, envEntryType);
        } catch (Exception e) {
            fail(report, e, "errSetEnvEntry", "Error setting env entry");
        }

    }

    private void setEnvEntry(final Engine owningEngine,
            final String name,
            final String description,
            final Boolean ignoreDescriptorItem,
            final String value,
            final String envEntryType) throws PropertyVetoException, TransactionFailure {

        WebModuleConfig config = WebModuleConfig.Duck.webModuleConfig(owningEngine);
        if (config == null) {
            createEnvEntryOnNewWMC(owningEngine, name, value, envEntryType,
                    description, ignoreDescriptorItem);
        } else {
            EnvEntry entry = config.getEnvEntry(name);
            if (entry == null) {
                createEnvEntryOnExistingWMC(config, name,
                        value, envEntryType, description, ignoreDescriptorItem);
            } else {
                modifyEnvEntry(entry, value, envEntryType, description,
                            ignoreDescriptorItem);
            }
        }
    }

    private void createEnvEntryOnNewWMC(final Engine owningEngine,
            final String name,
            final String value,
            final String envEntryType,
            final String description,
            final Boolean ignoreDescriptorItem) throws PropertyVetoException, TransactionFailure {

        ConfigSupport.apply(new SingleConfigCode<Engine>() {

            @Override
            public Object run(Engine e) throws PropertyVetoException, TransactionFailure {
                final WebModuleConfig config = e.createChild(WebModuleConfig.class);
                e.getApplicationConfigs().add(config);
                final EnvEntry envEntry = config.createChild(EnvEntry.class);
                config.getEnvEntry().add(envEntry);
                set(envEntry, name, value, envEntryType, description, ignoreDescriptorItem);
                return config;
             }
        }, owningEngine);
    }

    private void createEnvEntryOnExistingWMC(final WebModuleConfig config,
            final String name,
            final String value,
            final String envEntryType,
            final String description,
            final Boolean ignoreDescriptorItem) throws PropertyVetoException, TransactionFailure {

        ConfigSupport.apply(new SingleConfigCode<WebModuleConfig>() {
            @Override
            public Object run(WebModuleConfig cf) throws PropertyVetoException, TransactionFailure {
                final EnvEntry envEntry = cf.createChild(EnvEntry.class);
                cf.getEnvEntry().add(envEntry);
                set(envEntry, name, value, envEntryType, description, ignoreDescriptorItem);
                return envEntry;
             }
        }, config);
    }

    private void modifyEnvEntry(final EnvEntry envEntry,
            final String value,
            final String envEntryType,
            final String description,
            final Boolean ignoreDescriptorItem) throws PropertyVetoException, TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode<EnvEntry>() {

            @Override
            public Object run(EnvEntry ee) throws PropertyVetoException, TransactionFailure {
                set(ee, ee.getEnvEntryName(), value, envEntryType, description, ignoreDescriptorItem);
                return ee;
            }
        }, envEntry);
    }

    private void set(final EnvEntry envEntry,
            final String name,
            final String value,
            final String envEntryType,
            final String description,
            final Boolean ignoreDescriptorItem) throws PropertyVetoException, TransactionFailure {

        envEntry.setEnvEntryName(name);
        if (envEntryType != null) {
            envEntry.setEnvEntryType(envEntryType);
        }
        if (value != null) {
            envEntry.setEnvEntryValue(value);
        }
        if (description != null) {
            envEntry.setDescription(description);
        }
        if (ignoreDescriptorItem != null) {
            envEntry.setIgnoreDescriptorItem(ignoreDescriptorItem.toString());
        }
    }
}
