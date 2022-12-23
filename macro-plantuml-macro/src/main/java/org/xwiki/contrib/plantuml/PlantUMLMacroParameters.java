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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.properties.annotation.PropertyDescription;

/**
 * Parameters for the {@link org.xwiki.contrib.plantuml.internal.PlantUMLMacro} Macro.
 *
 * @version $Id$
 * @since 2.0
 */
public class PlantUMLMacroParameters
{
    private String serverURL;

    private String format;

    /**
     * @param serverURL see {@link #getServer()}
     */
    @PropertyDescription("the PlantUML Server URL")
    public void setServer(String serverURL)
    {
        this.serverURL = serverURL;
    }

    /**
     * @param format see {@link #getFormat()}. Valid values: svg, png, txt
     *
     */
    @PropertyDescription("the PlantUML output format")
    public void setFormat(String format)
    {
        this.format = format;
    }

    /**
     * @return the (optional) PlantUML server URL (e.g. {@code http://www.plantuml.com/plantuml})
     */
    public String getServer()
    {
        return this.serverURL;
    }

    /**
     * @return the (optional) PlantUML output format, png by default. Valid values: svg, png, txt
     */
    public String getFormat()
    {
        return this.format;
    }

    @Override
    public boolean equals(Object object)
    {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (object.getClass() != getClass()) {
            return false;
        }
        PlantUMLMacroParameters rhs = (PlantUMLMacroParameters) object;
        return new EqualsBuilder()
            .append(getServer(), rhs.getServer())
            .append(getFormat(), rhs.getFormat())
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(5, 37)
            .append(getServer())
            .append(getFormat())
            .toHashCode();
    }
}
