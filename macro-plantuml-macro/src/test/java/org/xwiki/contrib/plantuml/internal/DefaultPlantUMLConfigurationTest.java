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

import javax.inject.Named;

import org.junit.jupiter.api.Test;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultPlantUMLConfiguration}.
 *
 * @version $Id$
 */
@ComponentTest
class DefaultPlantUMLConfigurationTest
{
    @InjectMockComponents
    private DefaultPlantUMLConfiguration configuration;

    @MockComponent
    @Named("plantuml")
    private ConfigurationSource plantUMLConfigurationSource;

    @MockComponent
    @Named("xwikiproperties")
    private ConfigurationSource xwikiPropertiesConfigurationSource;

    @Test
    void getPlantUMLDefaultStyle()
    {
        when(this.plantUMLConfigurationSource.getProperty("defaultStyle")).thenReturn("!theme bluegray");

        assertEquals("!theme bluegray", this.configuration.getPlantUMLDefaultStyle());
    }

    @Test
    void getPlantUMLDefaultStyleWhenBlank()
    {
        when(this.plantUMLConfigurationSource.getProperty("defaultStyle")).thenReturn(" \n");

        assertNull(this.configuration.getPlantUMLDefaultStyle());
    }
}
