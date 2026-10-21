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
package org.xwiki.contrib.plantuml.test.ui;

import java.util.List;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.xwiki.contrib.plantuml.test.po.PlantUMLDiagramsElement;
import org.xwiki.test.docker.junit5.TestReference;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.ui.TestUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies that the PlantUML Macro generates diagrams, using the PlantUML library embedded in XWiki.
 *
 * @version $Id$
 */
@UITest(extraJARs = {
    // The PNG diagrams are served as temporary resources, whose URL handler must be in WEB-INF/lib (as it is in the
    // XWiki distributions) to be found. It's not in the minimal WAR used by the tests.
    "org.xwiki.platform:xwiki-platform-resource-temporary"
}, resolveExtraJARs = true)
class PlantUMLMacroIT
{
    private static final String USER = "PlantUMLMacroUser";

    @Test
    @Order(1)
    void generateDiagrams(TestUtils setup, TestReference reference)
    {
        setup.loginAsSuperAdmin();

        setup.createPage(reference, String.join("\n",
            "{{plantuml}}",
            "Bob -> Alice : png message",
            "{{/plantuml}}",
            "",
            "{{plantuml format=\"svg\" title=\"SVG Title\"}}",
            "Bob -> Alice : svg message",
            "{{/plantuml}}",
            "",
            "{{plantuml format=\"txt\"}}",
            "@startuml",
            "Bob -> Alice : txt message",
            "@enduml",
            "{{/plantuml}}",
            "",
            "{{plantuml type=\"mindmap\" format=\"svg\"}}",
            "* Root node",
            "** Child node",
            "{{/plantuml}}",
            "",
            "{{plantuml server=\"https://untrusted.example.org/plantuml\"}}",
            "Bob -> Alice : untrusted message",
            "{{/plantuml}}"));

        PlantUMLDiagramsElement diagrams = new PlantUMLDiagramsElement();

        // The default format is PNG, and the generated image must be served by XWiki.
        assertEquals(1, diagrams.getDisplayedImageCount());

        // The title parameter is added to the diagram and the type parameter wraps the content with the right
        // start/end tags.
        List<String> svgTexts = diagrams.getSVGDiagramTexts();
        assertThat(svgTexts, contains(
            allOf(containsString("SVG Title"), containsString("Bob"), containsString("Alice"),
                containsString("svg message")),
            allOf(containsString("Root node"), containsString("Child node"))));

        assertThat(diagrams.getTextDiagrams(), contains(allOf(containsString("Bob"), containsString("Alice"),
            containsString("txt message"))));

        // Only PlantUML servers whose domain is trusted can be used, to prevent SSRF attacks.
        assertThat(diagrams.getErrors(), contains(allOf(containsString("Failed to execute the PlantUML macro"),
            containsString("The PlantUML Server URL [https://untrusted.example.org/plantuml] is not in the list of "
                + "trusted domains."))));
    }

    @Test
    @Order(2)
    void diagramsInComments(TestUtils setup, TestReference reference)
    {
        setup.loginAsSuperAdmin();
        setup.createPage(reference, String.join("\n",
            "{{plantuml format=\"svg\"}}",
            "Bob -> Alice : page message",
            "{{/plantuml}}"));

        setup.createUserAndLogin(USER, USER);
        setup.addObject(reference, "XWiki.XWikiComments", "author", "XWiki." + USER, "comment", String.join("\n",
            "{{plantuml format=\"svg\"}}",
            "Bob -> Alice : comment message",
            "{{/plantuml}}"));

        // The comments are rendered in restricted mode, where diagrams are only generated as PNG images.
        setup.gotoPage(reference, "view", "viewer=comments");
        PlantUMLDiagramsElement commentDiagrams = PlantUMLDiagramsElement.inComments();
        assertThat(commentDiagrams.getSVGDiagramTexts(), empty());
        assertEquals(1, commentDiagrams.getDisplayedImageCount());
    }
}
