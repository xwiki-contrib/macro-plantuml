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

import net.sourceforge.plantuml.FileFormat;

/**
 * Represents PlantUML diagram output formats (SVG, PNG, TXT).
 *
 * @version $Id$
 * @since 2.4
 */
public enum PlantUMLDiagramFormat
{
    /**
     * Map {@code svg} as a macro parameter
     * to {@link net.sourceforge.plantuml.FileFormat#SVG} for internal rendering
     * and {@code svg} path parameter for external rendering.
     */
    svg(FileFormat.SVG),
    /**
     * Map {@code png} as a macro parameter
     * to {@link net.sourceforge.plantuml.FileFormat#PNG} for internal rendering
     * and {@code png} path parameter for external rendering.
     */
    png(FileFormat.PNG),
    /**
     * Map {@code txt} as a macro parameter
     * to {@link net.sourceforge.plantuml.FileFormat#ATXT} for internal rendering
     * and {@code txt} path parameter for external rendering.
     */
    txt(FileFormat.ATXT);

    private final FileFormat fileFormat;

    PlantUMLDiagramFormat(FileFormat fileFormat)
    {
        this.fileFormat = fileFormat;
    }

    /**
     * Get FileFormat enum element for internal (local) rendering.
     * @return the PlantUML file format to use
     */
    public FileFormat getFileFormat()
    {
        return fileFormat;
    }

    /**
     * Get output format path parameter URL fragment for external server rendering.
     * The complete PlantUML URL is in the form: {@code https://plantuml.com/plantuml/format/encoded},
     * where {@code format} is {@code svg}, {@code png}, {@code txt}
     * and {@code encoded} is compressed diagram definition.
     * @return diagram format path parameter
     */
    public String getPathParameter()
    {
        return name();
    }

    /**
     * Resolve PlantUML diagram output format by configuration parameter
     * with fallback to {@link PlantUMLDiagramFormat#png} for unknown values.
     *
     * @param format parameter's value (e.g. {@code svg}, {@code png}, {@code txt})
     * @return corresponding {@link PlantUMLDiagramFormat} with specific {@link net.sourceforge.plantuml.FileFormat}
     */
    public static PlantUMLDiagramFormat fromString(String format)
    {
        for (PlantUMLDiagramFormat enumItem : PlantUMLDiagramFormat.values()) {
            if (enumItem.name().equalsIgnoreCase(format)) {
                return enumItem;
            }
        }
        return png;
    }
}
