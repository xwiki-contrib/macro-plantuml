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

import java.io.IOException;
import java.io.OutputStream;

import org.xwiki.component.annotation.Role;

/**
 * Generate an image from a textual definition, using PlantUML.
 *
 * @version $Id$
 * @since 2.0
 */
@Role
public interface PlantUMLGenerator
{
    /**
     * Generate the image in the passed output parameter, using PlantUML.
     *
     * @param input     the textual definition input
     * @param output    the stream into which the generated image will be written to
     * @param serverURL the optional plantUML server URL (e.g. {@code http://www.plantuml.com/plantuml}. If not an
     *                  empty string or not null then the URL is called to get the generated image. Otherwise PlantUML works in
     *                  embedded mode (requires installation of Graphviz locally for most diagram types, and the {@code
     *                  GRAPHVIZ_DOT} environment variable must be set to point to the path of the GraphViz executable).
     * @throws IOException when there's a generation or writing error
     */
    void outputImage(String input, OutputStream output, String serverURL) throws IOException;

    /**
     * Generate the image in the passed output parameter, using PlantUML.
     *
     * @param input the textual definition input
     * @param output the stream into which the generated image will be written to
     * @param serverURL the optional plantUML server URL (e.g. {@code http://www.plantuml.com/plantuml}. If not an
     *        empty string or not null then the URL is called to get the generated image. Otherwise PlantUML works in
     *        embedded mode (requires installation of Graphviz locally for most diagram types, and the {@code
     *        GRAPHVIZ_DOT} environment variable must be set to point to the path of the GraphViz executable).
     * @param format sets the output format from the PlantUML server
     * @throws IOException when there's a generation or writing error
     */
    void outputImage(String input, OutputStream output, String serverURL, String format) throws IOException;
}
