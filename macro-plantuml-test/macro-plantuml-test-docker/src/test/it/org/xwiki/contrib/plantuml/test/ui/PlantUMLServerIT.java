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

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.xwiki.contrib.plantuml.test.po.PlantUMLAdministrationSectionPage;
import org.xwiki.contrib.plantuml.test.po.PlantUMLDiagramsElement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.docker.junit5.TestReference;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.integration.junit.LogCaptureConfiguration;
import org.xwiki.test.ui.TestUtils;
import org.xwiki.test.ui.po.BasePage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.xwiki.contrib.plantuml.test.ui.PlantUMLServerExtension.SERVER_VERSION;

/**
 * Verifies that the PlantUML Macro can generate diagrams using a remote PlantUML Server, started by
 * {@link PlantUMLServerExtension}.
 *
 * @version $Id$
 */
// The PlantUML Server must be started before XWiki, hence the extension registered before @UITest.
@ExtendWith(PlantUMLServerExtension.class)
@UITest(extraJARs = {
    // The PNG diagrams are served as temporary resources, whose URL handler must be in WEB-INF/lib (as it is in the
    // XWiki distributions) to be found. It's not in the minimal WAR used by the tests.
    "org.xwiki.platform:xwiki-platform-resource-temporary"
}, resolveExtraJARs = true)
class PlantUMLServerIT
{
    private static final String VERSION_DIAGRAM = "version";

    private static final String USER = "PlantUMLServerUser";

    private static final DocumentReference CONFIGURATION_REFERENCE =
        new DocumentReference("xwiki", "PlantUML", "PlantUMLConfig");

    @Test
    @Order(1)
    void serverParameter(TestUtils setup, TestReference reference)
    {
        setup.loginAsSuperAdmin();

        String serverURL = PlantUMLServerExtension.getServerURL();
        setup.createPage(reference, String.join("\n",
            "{{plantuml server=\"" + serverURL + "\" format=\"svg\"}}",
            VERSION_DIAGRAM,
            "{{/plantuml}}",
            "",
            "{{plantuml server=\"" + serverURL + "\"}}",
            "Bob -> Alice : hello",
            "{{/plantuml}}"));

        PlantUMLDiagramsElement diagrams = new PlantUMLDiagramsElement();
        assertThat(diagrams.getSVGDiagramTexts(), contains(containsString("PlantUML version " + SERVER_VERSION)));
        // The PNG image returned by the PlantUML Server must be served by XWiki.
        assertEquals(1, diagrams.getDisplayedImageCount());
    }

    @Test
    @Order(2)
    void serverConfiguredInAdministration(TestUtils setup, TestReference reference)
    {
        setup.loginAsSuperAdmin();

        PlantUMLAdministrationSectionPage administration = PlantUMLAdministrationSectionPage.gotoPage();
        assertEquals("", administration.getServerURL());
        administration.setServerURL(PlantUMLServerExtension.getServerURL());
        administration.save();

        setup.createPage(reference, String.join("\n",
            "{{plantuml format=\"svg\"}}",
            VERSION_DIAGRAM,
            "{{/plantuml}}"));

        PlantUMLDiagramsElement diagrams = new PlantUMLDiagramsElement();
        assertThat(diagrams.getSVGDiagramTexts(), contains(containsString("PlantUML version " + SERVER_VERSION)));

        // Go back to the embedded mode, which the tests executing afterwards on the same XWiki instance expect.
        administration = PlantUMLAdministrationSectionPage.gotoPage();
        administration.setServerURL("");
        administration.save();
    }

    @Test
    @Order(3)
    void serverConfiguredWithoutAdminRight(TestUtils setup, TestReference reference,
        LogCaptureConfiguration logCaptureConfiguration)
    {
        // A user without admin right can't edit the PlantUML configuration.
        setup.createUserAndLogin(USER, USER);
        setup.gotoPage(CONFIGURATION_REFERENCE, "edit");
        assertTrue(new BasePage().isForbidden());

        // A server configured by a user without admin right, even if allowed to edit the configuration, is ignored.
        setup.loginAsSuperAdmin();
        setup.setRights(CONFIGURATION_REFERENCE, null, "XWiki." + USER, "edit", true);
        setup.login(USER, USER);
        setup.updateObject(CONFIGURATION_REFERENCE, "PlantUML.PlantUMLConfigClass", 0, "server",
            PlantUMLServerExtension.getServerURL());

        setup.loginAsSuperAdmin();
        setup.createPage(reference, String.join("\n",
            "{{plantuml format=\"svg\"}}",
            VERSION_DIAGRAM,
            "{{/plantuml}}"));

        PlantUMLDiagramsElement diagrams = new PlantUMLDiagramsElement();
        assertThat(diagrams.getSVGDiagramTexts(), contains(generatedByXWiki()));
        logCaptureConfiguration.registerExpected(
            "Ignoring the PlantUML configuration from [xwiki:PlantUML.PlantUMLConfig]");

        // Go back to the embedded mode, configured by an administrator. The PlantUML administration section isn't
        // displayed anymore since the configuration has been saved by a user without admin right.
        setup.updateObject(CONFIGURATION_REFERENCE, "PlantUML.PlantUMLConfigClass", 0, "server", "");
    }

    private static Matcher<String> generatedByXWiki()
    {
        // The diagrams generated by XWiki use the PlantUML library embedded in the macro, whose version differs from
        // the PlantUML Server's.
        return allOf(containsString("PlantUML"), not(containsString(SERVER_VERSION)));
    }
}
