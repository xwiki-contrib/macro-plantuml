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
package org.xwiki.contrib.plantuml;

import org.xwiki.component.annotation.Role;

/**
 * Configuration for the PlantUML Macro.
 *
 * @version $Id$
 * @since 2.0
 */
@Role
public interface PlantUMLConfiguration
{
    /**
     * @return the (optional) PlantUML server URL (e.g. {@code https://www.plantuml.com/plantuml})
     * or null if not defined
     */
    String getPlantUMLServerURL();

    /**
     * @return {@link PlantUMLDiagramFormat}
     * @since 2.4
     */
    default PlantUMLDiagramFormat getPlantUMLOutputFormat()
    {
        // Fallback for backward compatibility
        return PlantUMLDiagramFormat.png;
    }

    /**
     * @return the name of the PlantUML security profile to use when generating diagrams in embedded mode (e.g.
     *         {@code SANDBOX}, {@code ALLOWLIST}), or null to use the default one ({@code SANDBOX})
     * @since 2.5
     */
    default String getPlantUMLSecurityProfile()
    {
        return null;
    }

    /**
     * @return the maximum time in seconds to wait for establishing the connection to the PlantUML server, a
     *         non-positive value meaning the default value (10 seconds)
     * @since 2.5
     */
    default int getPlantUMLServerConnectTimeout()
    {
        return 0;
    }

    /**
     * @return the maximum time in seconds to wait for data from the PlantUML server, a non-positive value meaning the
     *         default value (30 seconds)
     * @since 2.5
     */
    default int getPlantUMLServerResponseTimeout()
    {
        return 0;
    }

    /**
     * @return the maximum size in bytes of a diagram returned by the PlantUML server, a non-positive value meaning the
     *         default value (10 MB)
     * @since 2.5
     */
    default long getPlantUMLServerMaxResponseSize()
    {
        return 0;
    }
}
