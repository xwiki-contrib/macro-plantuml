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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.xwiki.contrib.plantuml.test.po.PlantUMLAdministrationSectionPage;
import org.xwiki.contrib.plantuml.test.po.PlantUMLDiagramsElement;
import org.xwiki.test.docker.junit5.TestReference;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.ui.TestUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies that the PlantUML Macro can generate diagrams using a remote PlantUML Server.
 * <p>
 * The PlantUML Server runs in a Docker container started by this test, that XWiki reaches through the Docker network
 * (which is why the module's {@code pom.xml} makes XWiki run in a Docker container too). The PlantUML Server's domain
 * is also added to the list of trusted domains, since the macro refuses to use an untrusted server.
 *
 * @version $Id$
 */
@UITest(properties = {
    "xwikiPropertiesAdditionalProperties=url.trustedDomains=" + PlantUMLServerIT.SERVER_HOST
}, extraJARs = {
    // The PNG diagrams are served as temporary resources, whose URL handler must be in WEB-INF/lib (as it is in the
    // XWiki distributions) to be found. It's not in the minimal WAR used by the tests.
    "org.xwiki.platform:xwiki-platform-resource-temporary"
}, resolveExtraJARs = true)
class PlantUMLServerIT
{
    static final String SERVER_HOST = "plantuml";

    private static final String SERVER_URL = "http://" + SERVER_HOST + ":8080";

    // Use a PlantUML version different from the one embedded in the macro, to verify that the diagrams are generated
    // by the PlantUML Server and not by XWiki.
    private static final String SERVER_VERSION = "1.2025.10";

    private static final String VERSION_DIAGRAM = "version";

    private static GenericContainer<?> plantUMLServer;

    @BeforeAll
    static void startPlantUMLServer()
    {
        plantUMLServer =
            new GenericContainer<>(DockerImageName.parse("plantuml/plantuml-server:jetty-v" + SERVER_VERSION))
                .withNetwork(Network.SHARED)
                .withNetworkAliases(SERVER_HOST)
                .withExposedPorts(8080)
                .waitingFor(Wait.forHttp("/"));
        plantUMLServer.start();
    }

    @AfterAll
    static void stopPlantUMLServer()
    {
        if (plantUMLServer != null) {
            plantUMLServer.stop();
        }
    }

    @Test
    @Order(1)
    void serverParameter(TestUtils setup, TestReference reference)
    {
        setup.loginAsSuperAdmin();

        setup.createPage(reference, String.join("\n",
            "{{plantuml server=\"" + SERVER_URL + "\" format=\"svg\"}}",
            VERSION_DIAGRAM,
            "{{/plantuml}}",
            "",
            "{{plantuml server=\"" + SERVER_URL + "\"}}",
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
        administration.setServerURL(SERVER_URL);
        administration.save();

        setup.createPage(reference, String.join("\n",
            "{{plantuml format=\"svg\"}}",
            VERSION_DIAGRAM,
            "{{/plantuml}}"));

        PlantUMLDiagramsElement diagrams = new PlantUMLDiagramsElement();
        assertThat(diagrams.getSVGDiagramTexts(), contains(containsString("PlantUML version " + SERVER_VERSION)));

        // Go back to the embedded mode since the PlantUML Server is stopped after this test while other tests may
        // execute afterwards on the same XWiki instance.
        administration = PlantUMLAdministrationSectionPage.gotoPage();
        administration.setServerURL("");
        administration.save();
    }
}
