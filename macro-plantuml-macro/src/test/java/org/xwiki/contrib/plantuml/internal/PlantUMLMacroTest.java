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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.contrib.plantuml.PlantUMLConfiguration;
import org.xwiki.contrib.plantuml.PlantUMLDiagramFormat;
import org.xwiki.contrib.plantuml.PlantUMLMacroParameters;
import org.xwiki.contrib.plantuml.PlantUMLRenderer;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PlantUMLMacro}.
 *
 * @version $Id$
 */
@ComponentTest
class PlantUMLMacroTest
{
    @InjectMockComponents
    private PlantUMLMacro macro;

    @MockComponent
    private PlantUMLRenderer plantUMLRenderer;

    @MockComponent
    private PlantUMLConfiguration configuration;

    @BeforeEach
    void setUp() throws Exception
    {
        when(this.plantUMLRenderer.renderDiagram(anyString(), any(), any())).thenReturn(new WordBlock("diagram"));
    }

    @Test
    void executeSyncWithSVGFormat() throws Exception
    {
        PlantUMLMacroParameters parameters = new PlantUMLMacroParameters();
        parameters.setFormat(PlantUMLDiagramFormat.svg);

        this.macro.executeSync("Bob -> Alice", parameters, false, false);

        verify(this.plantUMLRenderer).renderDiagram(anyString(), isNull(), eq(PlantUMLDiagramFormat.svg));
    }

    @Test
    void executeSyncWithSVGFormatWhenRestricted() throws Exception
    {
        PlantUMLMacroParameters parameters = new PlantUMLMacroParameters();
        parameters.setFormat(PlantUMLDiagramFormat.svg);

        this.macro.executeSync("Bob -> Alice", parameters, false, true);

        verify(this.plantUMLRenderer).renderDiagram(anyString(), isNull(), eq(PlantUMLDiagramFormat.png));
    }
}
