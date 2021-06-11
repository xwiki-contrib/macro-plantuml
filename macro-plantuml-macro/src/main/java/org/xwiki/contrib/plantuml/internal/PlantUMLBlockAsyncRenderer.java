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

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.plantuml.PlantUMLMacroParameters;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.rendering.async.AsyncContext;
import org.xwiki.rendering.async.internal.block.AbstractBlockAsyncRenderer;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.CompositeBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.block.match.MetadataBlockMatcher;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.util.ErrorBlockGenerator;

/**
 * Render the PlantUML macro content asynchronously.
 *
 * @version $Id$
 * @since 2.0
 */
@Component(roles = PlantUMLBlockAsyncRenderer.class)
public class PlantUMLBlockAsyncRenderer extends AbstractBlockAsyncRenderer
{
    private static final String ESCAPE_CHAR_SLASH = "_";

    private static final String ESCAPE_CHAR_BACKSLASH = "-";

    @Inject
    private DocumentReferenceResolver<String> resolver;

    @Inject
    private AsyncContext asyncContext;

    @Inject
    private ErrorBlockGenerator errorBlockGenerator;

    private List<String> id;

    private Syntax targetSyntax;

    private PlantUMLMacroParameters parameters;

    private String content;

    private DocumentReference sourceReference;

    private PlantUMLMacro macro;

    void initialize(PlantUMLMacro macro, PlantUMLMacroParameters parameters, String content,
        MacroTransformationContext context)
    {
        this.macro = macro;
        this.parameters = parameters;
        this.content = content;
        this.targetSyntax = context.getTransformationContext().getTargetSyntax();

        String source = getCurrentSource(context);
        if (source != null) {
            this.sourceReference = this.resolver.resolve(source);
        }

        this.id = createId(source, context);
    }

    @Override
    protected Block execute(boolean async, boolean cached)
    {
        List<Block> resultBlocks;

        if (this.sourceReference != null) {
            // Invalidate the cache when the document containing the macro call is modified
            this.asyncContext.useEntity(this.sourceReference);
        }
        try {
            resultBlocks = this.macro.executeSync(this.content, this.parameters);
        } catch (MacroExecutionException e) {
            // Display the error in the result
            resultBlocks = this.errorBlockGenerator.generateErrorBlocks("Failed to execute the PlantUML macro", e,
                false);
        }

        return new CompositeBlock(resultBlocks);
    }

    @Override
    public boolean isInline()
    {
        return false;
    }

    @Override
    public Syntax getTargetSyntax()
    {
        return this.targetSyntax;
    }

    @Override
    public List<String> getId()
    {
        return this.id;
    }

    @Override
    public boolean isAsyncAllowed()
    {
        return true;
    }

    @Override
    public boolean isCacheAllowed()
    {
        return true;
    }

    private List<String> createId(String source, MacroTransformationContext context)
    {
        // Find index of the macro in the XDOM to create a unique id.
        long index = context.getXDOM().indexOf(context.getCurrentMacroBlock());

        // Make sure we don't have a / or \ in the source so that it works on Tomcat by default even if Tomcat is not
        // configured to support \ and / in URLs.
        // Make "_" and "-" escape characters and thus:
        // - replace "_" by "__"
        // - replace "-" by "--"
        // - replace "\" by "_"
        // - replace "/" by "-"
        // This keeps the unicity of the source reference.
        // TODO: Remove when the parent pom is upgraded to the version of XWiki where
        // https://jira.xwiki.org/browse/XWIKI-17515 has been fixed.
        String escapedSource = StringUtils.replaceEach(source,
            new String[] { ESCAPE_CHAR_SLASH, ESCAPE_CHAR_BACKSLASH },
            new String[] { ESCAPE_CHAR_SLASH + ESCAPE_CHAR_SLASH, ESCAPE_CHAR_BACKSLASH + ESCAPE_CHAR_BACKSLASH });
        escapedSource = StringUtils.replaceEach(escapedSource, new String[] { "\\", "/" },
            new String[] { ESCAPE_CHAR_SLASH, ESCAPE_CHAR_BACKSLASH });

        return createId("rendering", "macro", "plantuml", escapedSource, index);
    }

    private String getCurrentSource(MacroTransformationContext context)
    {
        String currentSource = null;

        if (context != null) {
            currentSource =
                context.getTransformationContext() != null ? context.getTransformationContext().getId() : null;

            MacroBlock currentMacroBlock = context.getCurrentMacroBlock();

            if (currentMacroBlock != null) {
                MetaDataBlock metaDataBlock =
                    currentMacroBlock.getFirstBlock(new MetadataBlockMatcher(MetaData.SOURCE),
                        Block.Axes.ANCESTOR_OR_SELF);

                if (metaDataBlock != null) {
                    currentSource = (String) metaDataBlock.getMetaData().getMetaData(MetaData.SOURCE);
                }
            }
        }

        return currentSource;
    }

    private Block wrapInMacroMarker(MacroBlock macroBlockToWrap, List<Block> newBlocks)
    {
        return new MacroMarkerBlock(macroBlockToWrap.getId(), macroBlockToWrap.getParameters(),
            macroBlockToWrap.getContent(), newBlocks, macroBlockToWrap.isInline());
    }
}
