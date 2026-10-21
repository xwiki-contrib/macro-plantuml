/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.plantuml.internal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.contrib.plantuml.PlantUMLConfiguration;
import org.xwiki.contrib.plantuml.PlantUMLDiagramFormat;

/**
 * Implementation of the PlantUML configuration.
 *
 * @version $Id$
 * @since 2.0
 */
@Component
@Singleton
public class DefaultPlantUMLConfiguration implements PlantUMLConfiguration
{
    @Inject
    @Named("plantuml")
    private ConfigurationSource plantUMLConfigurationSource;

    @Inject
    @Named("xwikiproperties")
    private ConfigurationSource xwikiPropertiesConfigurationSource;

    @Override
    public String getPlantUMLServerURL()
    {
        String serverURL = this.plantUMLConfigurationSource.getProperty("server");
        // The returned value can be null if no xobject has been defined on the wiki config page, and empty if no
        // server has been set in it (which is the default).
        if (StringUtils.isBlank(serverURL)) {
            // Fallback to xwiki.properties
            serverURL = this.xwikiPropertiesConfigurationSource.getProperty("plantuml.server");
        }
        return StringUtils.isBlank(serverURL) ? null : serverURL;
    }

    @Override
    public PlantUMLDiagramFormat getPlantUMLOutputFormat()
    {
        String format = this.xwikiPropertiesConfigurationSource.getProperty("plantuml.format", "png");
        return PlantUMLDiagramFormat.fromString(format);
    }

    @Override
    public String getPlantUMLSecurityProfile()
    {
        return this.xwikiPropertiesConfigurationSource.getProperty("plantuml.securityProfile");
    }

    @Override
    public int getPlantUMLServerConnectTimeout()
    {
        return this.xwikiPropertiesConfigurationSource.getProperty("plantuml.server.connectTimeout", 0);
    }

    @Override
    public int getPlantUMLServerResponseTimeout()
    {
        return this.xwikiPropertiesConfigurationSource.getProperty("plantuml.server.responseTimeout", 0);
    }

    @Override
    public long getPlantUMLServerMaxResponseSize()
    {
        return this.xwikiPropertiesConfigurationSource.getProperty("plantuml.server.maxResponseSize", 0L);
    }
}
