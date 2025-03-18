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
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.macro.MacroExecutionException;

/**
 * Render PlantUML diagram as a wiki {@link org.xwiki.rendering.block.Block}.
 *
 * @version $Id$
 * @since 2.4
 */
@Role
public interface PlantUMLRenderer
{
    /**
     * Render specific {@link org.xwiki.rendering.block.Block} on the basis of generated diagram and output format.
     * Depending on the {@link PlantUMLDiagramFormat} returned Block can be
     * either {@link org.xwiki.rendering.block.ImageBlock} for image resource
     * or {@link org.xwiki.rendering.block.RawBlock} for embedded text.
     *
     * @param content diagram as a code (see {@link PlantUMLGenerator#outputImage})
     * @param serverURL optional generator URL  (see {@link PlantUMLGenerator#outputImage})
     * @param diagramFormat diagram output format (see {@link PlantUMLDiagramFormat})
     * @return diagram specific {@link org.xwiki.rendering.block.Block}
     * @throws MacroExecutionException if generating or rendering of the diagram fails
     */
    Block renderDiagram(String content, String serverURL, PlantUMLDiagramFormat diagramFormat)
            throws MacroExecutionException;
}
