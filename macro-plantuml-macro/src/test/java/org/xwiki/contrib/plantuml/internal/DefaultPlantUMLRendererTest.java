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

import java.io.OutputStream;

import javax.inject.Named;

import org.junit.jupiter.api.Test;
import org.xwiki.contrib.plantuml.PlantUMLDiagramFormat;
import org.xwiki.contrib.plantuml.PlantUMLGenerator;
import org.xwiki.contrib.plantuml.internal.store.ImageWriter;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.block.VerbatimBlock;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.xml.html.DefaultHTMLCleanerComponentList;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

/**
 * Unit tests for {@link DefaultPlantUMLRenderer}.
 *
 * @version $Id$
 */
@ComponentTest
@DefaultHTMLCleanerComponentList
class DefaultPlantUMLRendererTest
{
    @InjectMockComponents
    private DefaultPlantUMLRenderer renderer;

    @MockComponent
    private PlantUMLGenerator generator;

    @MockComponent
    @Named("tmp")
    private ImageWriter imageWriter;

    @Test
    void renderDiagramWithSVGFormat() throws Exception
    {
        mockGeneratorOutput(PlantUMLDiagramFormat.svg, "<svg xmlns=\"http://www.w3.org/2000/svg\">"
            + "<g onclick=\"clicked()\"><text x=\"10\" y=\"20\">Bob</text></g><script>run()</script></svg>");

        Block block = this.renderer.renderDiagram("content", null, PlantUMLDiagramFormat.svg);

        RawBlock rawBlock = assertInstanceOf(RawBlock.class, block);
        String svg = rawBlock.getRawContent();
        assertTrue(svg.contains("<text x=\"10\" y=\"20\">Bob</text>"), svg);
        assertFalse(svg.contains("onclick"), svg);
        assertFalse(svg.contains("script"), svg);
    }

    @Test
    void renderDiagramWithTXTFormat() throws Exception
    {
        mockGeneratorOutput(PlantUMLDiagramFormat.txt, "Bob -> <Alice>");

        Block block = this.renderer.renderDiagram("content", null, PlantUMLDiagramFormat.txt);

        VerbatimBlock verbatimBlock = assertInstanceOf(VerbatimBlock.class, block);
        assertEquals("Bob -> <Alice>", verbatimBlock.getProtectedString());
        assertFalse(verbatimBlock.isInline());
    }

    private void mockGeneratorOutput(PlantUMLDiagramFormat format, String output) throws Exception
    {
        doAnswer(invocation -> {
            invocation.<OutputStream>getArgument(1).write(output.getBytes(UTF_8));
            return null;
        }).when(this.generator).outputImage(eq("content"), any(OutputStream.class), eq(null), eq(format));
    }
}
