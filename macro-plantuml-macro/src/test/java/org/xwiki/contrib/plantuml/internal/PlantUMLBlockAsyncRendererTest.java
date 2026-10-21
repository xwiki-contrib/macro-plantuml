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

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.xwiki.contrib.plantuml.PlantUMLDiagramFormat;
import org.xwiki.contrib.plantuml.PlantUMLMacroParameters;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Unit tests for {@link PlantUMLBlockAsyncRenderer}.
 *
 * @version $Id$
 */
@ComponentTest
class PlantUMLBlockAsyncRendererTest
{
    @InjectMockComponents
    private PlantUMLBlockAsyncRenderer renderer;

    @MockComponent
    private PlantUMLMacro macro;

    @Test
    void getIdDependsOnContent()
    {
        assertEquals(getId("Bob -> Alice", new PlantUMLMacroParameters(), false),
            getId("Bob -> Alice", new PlantUMLMacroParameters(), false));
        assertNotEquals(getId("Bob -> Alice", new PlantUMLMacroParameters(), false),
            getId("Carol -> Dave", new PlantUMLMacroParameters(), false));
    }

    @Test
    void getIdDependsOnParameters()
    {
        PlantUMLMacroParameters parameters = new PlantUMLMacroParameters();
        parameters.setFormat(PlantUMLDiagramFormat.svg);

        assertNotEquals(getId("Bob -> Alice", new PlantUMLMacroParameters(), false),
            getId("Bob -> Alice", parameters, false));
    }

    @Test
    void getIdDependsOnRestricted()
    {
        assertNotEquals(getId("Bob -> Alice", new PlantUMLMacroParameters(), false),
            getId("Bob -> Alice", new PlantUMLMacroParameters(), true));
    }

    private List<String> getId(String content, PlantUMLMacroParameters parameters, boolean restricted)
    {
        // Same source and same position in the XDOM for all calls.
        MacroBlock macroBlock = new MacroBlock("plantuml", Collections.emptyMap(), content, false);
        XDOM xdom = new XDOM(Collections.singletonList(macroBlock));
        TransformationContext transformationContext = new TransformationContext(xdom, Syntax.XHTML_1_0, restricted);
        transformationContext.setId("wiki:space.page");
        MacroTransformationContext context = new MacroTransformationContext(transformationContext);
        context.setXDOM(xdom);
        context.setCurrentMacroBlock(macroBlock);

        this.renderer.initialize(this.macro, parameters, content, context);
        return this.renderer.getId();
    }
}
