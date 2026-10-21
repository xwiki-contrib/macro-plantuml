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

import org.apache.commons.io.FileUtils;
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

import net.sourceforge.plantuml.security.SecurityProfile;
import net.sourceforge.plantuml.security.SecurityUtils;

import static java.nio.charset.StandardCharsets.UTF_8;
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
}
