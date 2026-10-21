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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.contrib.plantuml.PlantUMLConfiguration;
import org.xwiki.contrib.plantuml.PlantUMLDiagramFormat;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.XWikiTempDir;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.sun.net.httpserver.HttpServer;

import net.sourceforge.plantuml.security.SecurityProfile;
import net.sourceforge.plantuml.security.SecurityUtils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultPlantUMLGenerator}.
 *
 * @version $Id$
 */
@ComponentTest
class DefaultPlantUMLGeneratorTest
{
    @InjectMockComponents
    private DefaultPlantUMLGenerator generator;

    @MockComponent
    private PlantUMLConfiguration configuration;

    @XWikiTempDir
    private File tmpDir;

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    private HttpServer server;

    private ExecutorService serverExecutor;

    private String serverURL;

    private byte[] responseBody = new byte[0];

    private long responseDelay;

    @BeforeEach
    void setUp() throws Exception
    {
        this.server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        this.server.createContext("/", exchange -> {
            try {
                Thread.sleep(this.responseDelay);
                exchange.sendResponseHeaders(200, this.responseBody.length);
                try (OutputStream body = exchange.getResponseBody()) {
                    body.write(this.responseBody);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        });
        this.serverExecutor = Executors.newCachedThreadPool();
        this.server.setExecutor(this.serverExecutor);
        this.server.start();
        this.serverURL = String.format("http://localhost:%d/plantuml", this.server.getAddress().getPort());
    }

    @AfterEach
    void tearDown()
    {
        this.server.stop(0);
        this.serverExecutor.shutdownNow();
    }

    @Test
    void outputImageWhenIncludingLocalFile() throws Exception
    {
        File includedFile = new File(this.tmpDir, "included.puml");
        FileUtils.write(includedFile, "Bob -> Alice : includedcontent\n", UTF_8);
        String input = String.format("@startuml%n!include %s%nCarol -> Dave : maincontent%n@enduml",
            includedFile.getAbsolutePath());

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        this.generator.outputImage(input, output, null, PlantUMLDiagramFormat.txt);

        String result = output.toString(UTF_8);
        assertFalse(result.contains("includedcontent"), result);
        assertTrue(result.contains(includedFile.getName()), result);
    }

    @Test
    void outputImageWhenInvalidSecurityProfile() throws Exception
    {
        when(this.configuration.getPlantUMLSecurityProfile()).thenReturn("invalid");
        this.generator.initialize();

        assertEquals("Invalid PlantUML security profile [invalid], using [SANDBOX] instead.",
            this.logCapture.getMessage(0));
        assertEquals(SecurityProfile.SANDBOX.name(), System.getProperty("PLANTUML_SECURITY_PROFILE"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        this.generator.outputImage("@startuml\nBob -> Alice : hello\n@enduml", output, null,
            PlantUMLDiagramFormat.txt);
        assertTrue(output.toString(UTF_8).contains("hello"));
    }

    @Test
    void outputImageWhenPlantUMLUsesAnotherSecurityProfile() throws Exception
    {
        // PlantUML keeps the profile it read first, so configure one that differs from it.
        SecurityProfile currentProfile = SecurityUtils.getSecurityProfile();
        SecurityProfile otherProfile =
            currentProfile == SecurityProfile.ALLOWLIST ? SecurityProfile.INTERNET : SecurityProfile.ALLOWLIST;
        when(this.configuration.getPlantUMLSecurityProfile()).thenReturn(otherProfile.name().toLowerCase());
        try {
            this.generator.initialize();

            IOException exception = assertThrows(IOException.class,
                () -> this.generator.outputImage("@startuml\nBob -> Alice\n@enduml", new ByteArrayOutputStream(),
                    null, PlantUMLDiagramFormat.txt));
            assertEquals(String.format("PlantUML uses the security profile [%s] instead of the configured one [%s]. "
                + "Restart XWiki or use a PlantUML server.", currentProfile, otherProfile), exception.getMessage());
        } finally {
            System.setProperty("PLANTUML_SECURITY_PROFILE", currentProfile.name());
        }
    }

    @Test
    void outputImageWithServer() throws Exception
    {
        this.responseBody = "diagram".getBytes(UTF_8);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        this.generator.outputImage("Bob -> Alice", output, this.serverURL, PlantUMLDiagramFormat.png);

        assertArrayEquals(this.responseBody, output.toByteArray());
    }

    @Test
    void outputImageWithServerWhenResponseIsTooLarge()
    {
        when(this.configuration.getPlantUMLServerMaxResponseSize()).thenReturn(100L);
        this.responseBody = new byte[101];
        Arrays.fill(this.responseBody, (byte) 'a');

        IOException exception = assertThrows(IOException.class, () -> this.generator.outputImage("Bob -> Alice",
            new ByteArrayOutputStream(), this.serverURL, PlantUMLDiagramFormat.png));

        assertTrue(exception.getMessage().endsWith("exceeds the maximum allowed size of [100] bytes"),
            exception.getMessage());
    }

    @Test
    void outputImageWithServerWhenResponseIsTooSlow()
    {
        when(this.configuration.getPlantUMLServerResponseTimeout()).thenReturn(1);
        this.responseDelay = 5000;

        long start = System.currentTimeMillis();
        assertThrows(IOException.class, () -> this.generator.outputImage("Bob -> Alice",
            new ByteArrayOutputStream(), this.serverURL, PlantUMLDiagramFormat.png));
        assertTrue(System.currentTimeMillis() - start < 4000);
    }
}
