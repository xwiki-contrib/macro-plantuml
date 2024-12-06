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

import org.apache.commons.lang3.StringUtils;
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

    private String title;

    private PlantUMLDiagramType type;

    /**
     * @param serverURL see {@link #getServer()}
     */
    @PropertyDescription("the PlantUML Server URL")
    public void setServer(String serverURL)
    {
        this.serverURL = serverURL;
    }

    /**
     * @return the (optional) PlantUML server URL (e.g. {@code http://www.plantuml.com/plantuml})
     */
    public String getServer()
    {
        return this.serverURL;
    }

    /**
     * @param type see {@link #getType()}
     */
    @PropertyDescription("the type of diagram")
    public void setType(PlantUMLDiagramType type)
    {
        this.type = type;
    }

    /**
     * @return the type of diagram
     */
    public PlantUMLDiagramType getType()
    {
        return (this.type != null) ? this.type : PlantUMLDiagramType.plantuml;
    }

    /**
     * @param title see {@link #getTitle()}
     */
    @PropertyDescription("the diagram title")
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @return the diagram title
     */
    public String getTitle()
    {
        return (this.title != null) ? this.title : StringUtils.EMPTY;
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
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(5, 37)
            .append(getServer())
            .toHashCode();
    }
}
