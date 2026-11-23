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

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Inserts the configured default style (e.g. a {@code !theme} line followed by {@code skinparam} lines) into a
 * diagram, so that all the diagrams of a wiki share the same look without repeating it in each of them.
 *
 * @version $Id$
 * @since 2.6
 */
final class DefaultStyleInserter
{
    private static final String NEW_LINE = "\n";

    private static final Pattern THEME_PATTERN = Pattern.compile("^\\s*!theme\\b", Pattern.MULTILINE);

    // PlantUML block comments, which can span several lines.
    private static final Pattern BLOCK_COMMENT_PATTERN = Pattern.compile("/'.*?'/", Pattern.DOTALL);

    // PlantUML only renders the first diagram of the content.
    private static final Pattern END_PATTERN = Pattern.compile("^\\s*@end", Pattern.MULTILINE);

    private static final Pattern START_TYPE_PATTERN = Pattern.compile("^@start([a-z]+)", Pattern.CASE_INSENSITIVE);

    /**
     * The diagram types whose content is written in the PlantUML language, and thus supports a {@code !theme} and
     * {@code skinparam} lines. The other types (e.g. {@code ditaa}, {@code dot}, {@code jcckit}) take their content
     * verbatim, so the default style would end up drawn in the diagram or break it.
     */
    private static final Set<String> STYLABLE_TYPES =
        Set.of("uml", "salt", "mindmap", "gantt", "wbs", "json", "yaml", "regex", "ebnf", "nwdiag", "chen", "files",
            "board", "wire");

    private DefaultStyleInserter()
    {
        // Utility class.
    }

    /**
     * @param content the diagram, starting with its {@code @start...} line
     * @param style the default style to insert, or null if there's none
     * @return the diagram with the style inserted just after its {@code @start...} line, or the diagram unchanged
     *         when there's no style, when the diagram sets its own {@code !theme}, or when its type doesn't support
     *         styling
     */
    static String insert(String content, String style)
    {
        if (StringUtils.isBlank(style) || setsItsOwnTheme(content)) {
            return content;
        }

        Matcher matcher = START_TYPE_PATTERN.matcher(content);
        int index = content.indexOf(NEW_LINE);
        if (index < 0 || !matcher.find() || !STYLABLE_TYPES.contains(matcher.group(1).toLowerCase(Locale.ROOT))) {
            return content;
        }

        // Insert the style just under the @start line, so that the diagram can still override any of its skinparams.
        return String.join(NEW_LINE, content.substring(0, index), style.strip(), content.substring(index + 1));
    }

    private static boolean setsItsOwnTheme(String content)
    {
        Matcher endMatcher = END_PATTERN.matcher(content);
        String firstDiagram = endMatcher.find() ? content.substring(0, endMatcher.start()) : content;
        return THEME_PATTERN.matcher(BLOCK_COMMENT_PATTERN.matcher(firstDiagram).replaceAll("")).find();
    }
}
