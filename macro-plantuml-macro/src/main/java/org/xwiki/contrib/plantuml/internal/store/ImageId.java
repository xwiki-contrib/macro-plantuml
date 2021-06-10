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
package org.xwiki.contrib.plantuml.internal.store;

import org.xwiki.contrib.plantuml.PlantUMLMacroParameters;

/**
 * Compute a unique id for the image that the PlantUML macro generates.
 *
 * @version $Id$
 * @since 2.0
 */
public class ImageId
{
    private String uniqueId;

    /**
     * @param content the diagram textual input
     * @param macroParameters the macro parameters
     */
    public ImageId(String content, PlantUMLMacroParameters macroParameters)
    {
        this.uniqueId = String.format("%s%s", content.hashCode(), Math.abs(macroParameters.hashCode()));
    }

    /**
     * Compute a unique id based on the macro parameters.
     *
     * @return the unique image id used for storing the generated image
     */
    public String getId()
    {
        return this.uniqueId;
    }
}
