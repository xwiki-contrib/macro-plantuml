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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.plantuml.PlantUMLConfiguration;
import org.xwiki.contrib.plantuml.PlantUMLDiagramFormat;
import org.xwiki.contrib.plantuml.PlantUMLDiagramType;
import org.xwiki.contrib.plantuml.PlantUMLRenderer;
import org.xwiki.contrib.plantuml.PlantUMLMacroParameters;
import org.xwiki.rendering.async.internal.AsyncRendererConfiguration;
import org.xwiki.rendering.async.internal.block.BlockAsyncRendererExecutor;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.CompositeBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Asynchronous macro that generates an image from a textual description, using PlantUML.
 *
 * @version $Id$
 * @since 2.0
 */
@Component
@Named("plantuml")
@Singleton
public class PlantUMLMacro extends AbstractMacro<PlantUMLMacroParameters>
{
    /**
     * The description of the macro.
     */
    private static final String DESCRIPTION =
        "Convert various text input formats into diagram images using PlantUML.";

    /**
     * The description of the macro content.
     */
    private static final String CONTENT_DESCRIPTION = "The textual definition of the diagram";

    private static final String AT_START = "@start";

    private static final String AT_END = "@end";

    private static final String NEW_LINE = "\n";

    @Inject
    private BlockAsyncRendererExecutor executor;

    @Inject
    private Provider<PlantUMLBlockAsyncRenderer> asyncRendererProvider;

    @Inject
    private PlantUMLRenderer plantUMLRenderer;

    @Inject
    private PlantUMLConfiguration configuration;

    @Inject
    private Logger logger;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public PlantUMLMacro()
    {
        super("PlantUML", DESCRIPTION, new DefaultContentDescriptor(CONTENT_DESCRIPTION),
            PlantUMLMacroParameters.class);
        setDefaultCategory(DEFAULT_CATEGORY_CONTENT);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }

    @Override
    public List<Block> execute(PlantUMLMacroParameters parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        return executeAsync(parameters, content, context);
    }

    private List<Block> executeAsync(PlantUMLMacroParameters parameters, String content,
        MacroTransformationContext context) throws MacroExecutionException
    {
        PlantUMLBlockAsyncRenderer renderer = this.asyncRendererProvider.get();
        renderer.initialize(this, parameters, content, context);

        AsyncRendererConfiguration rendererConfiguration = new AsyncRendererConfiguration();
        rendererConfiguration.setContextEntries(Collections.singleton("doc.reference"));

        // Execute the renderer
        Block result;
        try {
            result = this.executor.execute(renderer, rendererConfiguration);
        } catch (Exception e) {
            throw new MacroExecutionException(String.format("Failed to execute the PlantUML macro for content [%s]",
                content), e);
        }

        return result instanceof CompositeBlock ? result.getChildren() : Arrays.asList(result);
    }

    List<Block> executeSync(String content, PlantUMLMacroParameters parameters, boolean isInline)
        throws MacroExecutionException
    {
        String wrappedContent = maybeAddTitle(maybeAddContentMarkers(content, parameters), parameters);
        logger.debug("Rendering PlantUML diagram with content [{}]", wrappedContent);

        Block resultBlock = plantUMLRenderer.renderDiagram(
                wrappedContent,
                computeServer(parameters),
                computeFormat(parameters)
        );

        // Wrap in a DIV if not inline (we need that since an IMG is an inline element otherwise)
        if (!isInline) {
            resultBlock = new GroupBlock(Arrays.asList(resultBlock));
        }
        return Arrays.asList(resultBlock);
    }

    private String computeServer(PlantUMLMacroParameters parameters)
    {
        String serverURL = parameters.getServer();
        if (serverURL == null) {
            serverURL = this.configuration.getPlantUMLServerURL();
        }
        return serverURL;
    }

    private PlantUMLDiagramFormat computeFormat(PlantUMLMacroParameters parameters)
    {
        PlantUMLDiagramFormat format = parameters.getFormat();
        if (format == null) {
            format = this.configuration.getPlantUMLOutputFormat();
            if (format == null) {
                // fallback if mocked configuration implementation returns null
                format = PlantUMLDiagramFormat.png;
            }
        }
        return format;
    }

    private String maybeAddContentMarkers(String content, PlantUMLMacroParameters parameters)
    {
        String trimmedContent = content.trim();

        if (!trimmedContent.startsWith(AT_START)) {
            // We'll assume that if a diagram doesn't start with an @start tag, then it's likely to not end with an
            // @end.... tag
            StringBuilder sb = new StringBuilder();

            sb.append(computeContentMarker(AT_START, parameters));
            sb.append(trimmedContent);
            sb.append(NEW_LINE);
            sb.append(computeContentMarker(AT_END, parameters));

            return sb.toString();
        }

        return trimmedContent;
    }

    private String computeContentMarker(String prefix, PlantUMLMacroParameters parameters)
    {
        String newLine = (!prefix.equals(AT_END)) ? NEW_LINE : StringUtils.EMPTY;

        if (PlantUMLDiagramType.plantuml.equals(parameters.getType())) {
            return prefix + "uml" + newLine;
        } else {
            return prefix + parameters.getType().toString().toLowerCase() + newLine;
        }
    }

    private String maybeAddTitle(String content, PlantUMLMacroParameters parameters)
    {
        if (StringUtils.isNotBlank(parameters.getTitle()) && !content.contains("\ntitle ")) {
            // Insert the title just under the @start tag, which is always the first line
            List<String> splittedContent = new ArrayList<>(Arrays.asList(content.split(NEW_LINE)));
            splittedContent.add(1, String.format("title %s", parameters.getTitle()));

            return String.join(NEW_LINE, splittedContent);
        }

        return content;
    }
}
