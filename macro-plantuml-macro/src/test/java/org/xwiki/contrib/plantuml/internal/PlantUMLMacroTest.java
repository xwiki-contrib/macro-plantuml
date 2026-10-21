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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.contrib.plantuml.PlantUMLConfiguration;
import org.xwiki.contrib.plantuml.PlantUMLDiagramFormat;
import org.xwiki.contrib.plantuml.PlantUMLMacroParameters;
import org.xwiki.contrib.plantuml.PlantUMLRenderer;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.rendering.async.internal.block.BlockAsyncRendererExecutor;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.CompositeBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.security.authorization.AuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

    @MockComponent
    private BlockAsyncRendererExecutor executor;

    @MockComponent
    private DocumentAccessBridge documentAccessBridge;

    @MockComponent
    private AuthorizationManager authorizationManager;

    private final DocumentReference authorReference = new DocumentReference("wiki", "XWiki", "Author");

    @BeforeEach
    void setUp() throws Exception
    {
        when(this.plantUMLRenderer.renderDiagram(anyString(), any(), any())).thenReturn(new WordBlock("diagram"));
        when(this.executor.execute(any(), any()))
            .thenReturn(new CompositeBlock(Collections.singletonList(new WordBlock("diagram"))));
        when(this.documentAccessBridge.getCurrentDocumentReference())
            .thenReturn(new DocumentReference("wiki", "Space", "Page"));
        when(this.documentAccessBridge.getCurrentAuthorReference()).thenReturn(this.authorReference);
    }

    @Test
    void executeWithServerWhenAuthorIsAdmin() throws Exception
    {
        when(this.authorizationManager.hasAccess(Right.ADMIN, this.authorReference, new WikiReference("wiki")))
            .thenReturn(true);

        List<Block> result = this.macro.execute(getParametersWithServer(), "Bob -> Alice", getContext(false));

        assertEquals(Collections.singletonList(new WordBlock("diagram")), result);
    }

    @Test
    void executeWithServerWhenAuthorIsNotAdmin()
    {
        MacroExecutionException exception = assertThrows(MacroExecutionException.class,
            () -> this.macro.execute(getParametersWithServer(), "Bob -> Alice", getContext(false)));

        assertEquals("The [server] parameter requires admin right on the wiki.", exception.getMessage());
        verifyNoInteractions(this.executor);
    }

    @Test
    void executeWithServerWhenRestricted()
    {
        when(this.authorizationManager.hasAccess(Right.ADMIN, this.authorReference, new WikiReference("wiki")))
            .thenReturn(true);

        MacroExecutionException exception = assertThrows(MacroExecutionException.class,
            () -> this.macro.execute(getParametersWithServer(), "Bob -> Alice", getContext(true)));

        assertEquals("The [server] parameter requires admin right on the wiki.", exception.getMessage());
        verifyNoInteractions(this.executor);
    }

    @Test
    void executeWithBlankServerWhenAuthorIsNotAdmin() throws Exception
    {
        PlantUMLMacroParameters parameters = new PlantUMLMacroParameters();
        parameters.setServer(" ");

        List<Block> result = this.macro.execute(parameters, "Bob -> Alice", getContext(false));
        assertEquals(Collections.singletonList(new WordBlock("diagram")), result);

        // A blank server is considered as not set, and since there's no configured server, the diagram is generated
        // by XWiki.
        this.macro.executeSync("Bob -> Alice", parameters, false, false);
        verify(this.plantUMLRenderer).renderDiagram(anyString(), isNull(), any());
    }

    @Test
    void executeWithoutServerWhenRestricted() throws Exception
    {
        List<Block> result = this.macro.execute(new PlantUMLMacroParameters(), "Bob -> Alice", getContext(true));

        assertEquals(Collections.singletonList(new WordBlock("diagram")), result);
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

    private PlantUMLMacroParameters getParametersWithServer()
    {
        PlantUMLMacroParameters parameters = new PlantUMLMacroParameters();
        parameters.setServer("https://plantuml.example.org/plantuml");
        return parameters;
    }

    private MacroTransformationContext getContext(boolean restricted)
    {
        return new MacroTransformationContext(
            new TransformationContext(new XDOM(Collections.emptyList()), Syntax.XHTML_1_0, restricted));
    }
}
