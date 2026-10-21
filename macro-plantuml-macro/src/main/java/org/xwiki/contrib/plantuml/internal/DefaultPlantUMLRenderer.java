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

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.plantuml.PlantUMLDiagramFormat;
import org.xwiki.contrib.plantuml.PlantUMLGenerator;
import org.xwiki.contrib.plantuml.PlantUMLRenderer;
import org.xwiki.contrib.plantuml.internal.store.ImageWriter;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.ImageBlock;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.block.VerbatimBlock;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.xml.html.HTMLCleaner;
import org.xwiki.xml.html.HTMLCleanerConfiguration;
import org.xwiki.xml.html.HTMLConstants;
import org.xwiki.xml.html.HTMLUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Render PlantUML diagram as a wiki {@link org.xwiki.rendering.block.Block}.
 *
 * @version $Id$
 * @since 2.4
 */
@Component
@Singleton
public class DefaultPlantUMLRenderer implements PlantUMLRenderer
{
    @Inject
    private PlantUMLGenerator plantUMLGenerator;

    @Inject
    @Named("tmp")
    private ImageWriter imageWriter;

    @Inject
    private HTMLCleaner htmlCleaner;

    /**
     * Local functional interface to serve rendering registry.
     */
    interface BlockRenderer
    {
        Block render(String content, String serverURL, PlantUMLDiagramFormat diagramFormat)
                throws MacroExecutionException;
    }
    // Map {@link org.xwiki.contrib.plantuml.PlantUMLDiagramFormat} to corresponding rendering method in registry.
    private final Map<PlantUMLDiagramFormat, BlockRenderer> renderers;
    {
        renderers = new HashMap<>();
        renderers.put(PlantUMLDiagramFormat.png, this::renderImageBlock);
        renderers.put(PlantUMLDiagramFormat.svg, this::renderRawBlock);
        renderers.put(PlantUMLDiagramFormat.txt, this::renderPreBlock);
    }

    /**
     * See {@link org.xwiki.contrib.plantuml.PlantUMLRenderer#renderDiagram(String, String, PlantUMLDiagramFormat)}.
     */
    @Override
    public Block renderDiagram(String content, String serverURL, PlantUMLDiagramFormat diagramFormat)
            throws MacroExecutionException
    {
        BlockRenderer renderer = renderers.get(diagramFormat);
        if (renderer == null) {
            throw new MacroExecutionException("Unknown diagram format: " + diagramFormat);
        }
        return renderer.render(content, serverURL, diagramFormat);
    }

    private Block renderRawBlock(String content, String serverURL, PlantUMLDiagramFormat diagramFormat)
            throws MacroExecutionException
    {
        String text = renderToString(content, serverURL, diagramFormat);
        return new RawBlock(clean(text), Syntax.XHTML_1_0);
    }

    private Block renderPreBlock(String content, String serverURL, PlantUMLDiagramFormat diagramFormat)
            throws MacroExecutionException
    {
        String text = renderToString(content, serverURL, diagramFormat);
        return new VerbatimBlock(text, false);
    }

    private String clean(String html)
    {
        // In restricted mode, the HTML Cleaner removes the elements and attributes that are not allowed by the HTML
        // sanitizer.
        HTMLCleanerConfiguration configuration = this.htmlCleaner.getDefaultConfiguration();
        Map<String, String> parameters = new HashMap<>(configuration.getParameters());
        parameters.put(HTMLCleanerConfiguration.HTML_VERSION, "5");
        parameters.put(HTMLCleanerConfiguration.RESTRICTED, Boolean.TRUE.toString());
        configuration.setParameters(parameters);
        Document document = this.htmlCleaner.clean(new StringReader(html), configuration);
        HTMLUtils.stripHTMLEnvelope(document);
        // The HTML Cleaner wraps the (inline) svg element in a paragraph, remove it.
        HTMLUtils.stripFirstElementInside(document, HTMLConstants.TAG_HTML, HTMLConstants.TAG_P);
        // Don't print the XML declaration nor the doctype.
        String cleanedHTML = HTMLUtils.toString(document, true, true);
        // Remove the top level html element, which is always present since the HTML Cleaner normalizes the HTML.
        String content = StringUtils.substringAfter(cleanedHTML, '<' + HTMLConstants.TAG_HTML + '>');
        return StringUtils.substringBeforeLast(content, "</" + HTMLConstants.TAG_HTML + '>');
    }

    private String renderToString(String content, String serverURL, PlantUMLDiagramFormat diagramFormat)
            throws MacroExecutionException
    {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            this.plantUMLGenerator.outputImage(content, baos, serverURL, diagramFormat);
            return baos.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new MacroExecutionException(
                    String.format("Failed to generate a text using PlantUML for content [%s]", content), e);
        }
    }

    private Block renderImageBlock(String content, String serverURL, PlantUMLDiagramFormat diagramFormat)
            throws MacroExecutionException
    {
        String imageId = getImageId(content, diagramFormat.getFileFormat().getFileSuffix());
        try (OutputStream os = this.imageWriter.getOutputStream(imageId)) {
            this.plantUMLGenerator.outputImage(content, os, serverURL, diagramFormat);
        } catch (IOException e) {
            throw new MacroExecutionException(
                    String.format("Failed to generate an image using PlantUML for content [%s]", content), e);
        }

        // Return the image block pointing to the generated image.
        ResourceReference resourceReference =
                new ResourceReference(this.imageWriter.getURL(imageId).serialize(), ResourceType.URL);
        return new ImageBlock(resourceReference, false);
    }

    private String getImageId(String content, String extension)
    {
        return String.format("%d%s", content.hashCode(), extension);
    }
}
