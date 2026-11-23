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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link DefaultStyleInserter}.
 *
 * @version $Id$
 */
class DefaultStyleInserterTest
{
    private static final String STYLE = "!theme bluegray";

    @Test
    void insertAfterStartLine()
    {
        assertEquals("@startuml\n!theme bluegray\nskinparam shadowing false\nBob -> Alice\n@enduml",
            DefaultStyleInserter.insert("@startuml\nBob -> Alice\n@enduml", " !theme bluegray\nskinparam shadowing "
                + "false\n"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "nwdiag", "salt", "MindMap" })
    void insertForOtherDiagramTypesWrittenInPlantUMLLanguage(String type)
    {
        assertEquals("@start" + type + "\n!theme bluegray\nfoo\n@end" + type,
            DefaultStyleInserter.insert("@start" + type + "\nfoo\n@end" + type, STYLE));
    }

    @ParameterizedTest
    @ValueSource(strings = { "ditaa", "dot", "jcckit", "unknown" })
    void insertWhenDiagramIsNotWrittenInPlantUMLLanguage(String type)
    {
        String content = "@start" + type + "\nfoo\n@end" + type;
        assertEquals(content, DefaultStyleInserter.insert(content, STYLE));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "\n" })
    void insertWhenNoStyle(String style)
    {
        String content = "@startuml\nBob -> Alice\n@enduml";
        assertEquals(content, DefaultStyleInserter.insert(content, style));
        assertEquals(content, DefaultStyleInserter.insert(content, null));
    }

    @Test
    void insertWhenDiagramSetsItsOwnTheme()
    {
        String content = "@startuml\nBob -> Alice\n  !theme plain\n@enduml";
        assertEquals(content, DefaultStyleInserter.insert(content, STYLE));
    }

    @Test
    void insertWhenDiagramThemeIsCommentedOut()
    {
        assertEquals("@startuml\n!theme bluegray\n' !theme plain\n/'\n!theme plain\n'/\nBob -> Alice\n@enduml",
            DefaultStyleInserter.insert("@startuml\n' !theme plain\n/'\n!theme plain\n'/\nBob -> Alice\n@enduml",
                STYLE));
    }

    @Test
    void insertWhenOnlyAnotherDiagramSetsItsOwnTheme()
    {
        // PlantUML only renders the first diagram.
        assertEquals("@startuml\n!theme bluegray\nA -> B\n@enduml\n@startuml\n!theme plain\nC -> D\n@enduml",
            DefaultStyleInserter.insert("@startuml\nA -> B\n@enduml\n@startuml\n!theme plain\nC -> D\n@enduml",
                STYLE));
    }

    @Test
    void insertWhenDiagramHasASingleLine()
    {
        assertEquals("@startuml", DefaultStyleInserter.insert("@startuml", STYLE));
    }
}
