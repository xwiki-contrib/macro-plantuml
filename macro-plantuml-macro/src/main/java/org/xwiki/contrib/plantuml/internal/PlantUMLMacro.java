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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.plantuml.PlantUMLConfiguration;
import org.xwiki.contrib.plantuml.PlantUMLGenerator;
import org.xwiki.contrib.plantuml.PlantUMLMacroParameters;
import org.xwiki.contrib.plantuml.internal.store.ImageId;
import org.xwiki.contrib.plantuml.internal.store.ImageWriter;
import org.xwiki.rendering.async.internal.AsyncRendererConfiguration;
import org.xwiki.rendering.async.internal.block.BlockAsyncRendererExecutor;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.CompositeBlock;
import org.xwiki.rendering.block.ImageBlock;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
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

    @Inject
    private BlockAsyncRendererExecutor executor;

    @Inject
    private Provider<PlantUMLBlockAsyncRenderer> asyncRendererProvider;

    @Inject
    private PlantUMLGenerator plantUMLGenerator;

    @Inject
    @Named("tmp")
    private ImageWriter imageWriter;

    @Inject
    private PlantUMLConfiguration configuration;

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
        return false;
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

        // Execute the renderer
        Block result;
        try {
            result = this.executor.execute(renderer, rendererConfiguration);
        } catch (Exception e) {
            throw new MacroExecutionException("Failed to execute the PlantUML macro", e);
        }

        return result instanceof CompositeBlock ? result.getChildren() : Arrays.asList(result);
    }

    List<Block> executeSync(String content, PlantUMLMacroParameters parameters) throws MacroExecutionException
    {
        // Save the generated image in a temporary directory and return a link block that uses the "tmp" action to
        // point to it.
        ImageId imageId = new ImageId(content, parameters);
        try {
            this.plantUMLGenerator.outputImage(content, this.imageWriter.getOutputStream(imageId),
                computeServer(parameters));
        } catch (IOException e) {
            throw new MacroExecutionException("Failed to generate an image using PlantUML", e);
        }

        // Return the image block pointing to the generated image.
        ResourceReference resourceReference = new ResourceReference(this.imageWriter.getURL(imageId), ResourceType.URL);
        ImageBlock imageBlock = new ImageBlock(resourceReference, false);
        return Arrays.asList(imageBlock);
    }

    private String computeServer(PlantUMLMacroParameters parameters)
    {
        String serverURL = parameters.getServer();
        if (serverURL == null) {
            serverURL = this.configuration.getPlantUMLServerURL();
        }
        return serverURL;
    }
}
