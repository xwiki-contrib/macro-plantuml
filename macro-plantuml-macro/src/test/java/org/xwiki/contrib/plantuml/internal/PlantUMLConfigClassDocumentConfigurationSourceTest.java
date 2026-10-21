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
import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheManager;
import org.xwiki.model.ModelContext;
import org.xwiki.model.document.DocumentAuthors;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.properties.ConverterManager;
import org.xwiki.security.authorization.AuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.test.LogLevel;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.user.UserReference;
import org.xwiki.user.UserReferenceSerializer;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.StringProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PlantUMLConfigClassDocumentConfigurationSource}.
 *
 * @version $Id$
 */
@ComponentTest
class PlantUMLConfigClassDocumentConfigurationSourceTest
{
    private static final String SERVER_URL = "https://plantuml.example.org/plantuml";

    private static final DocumentReference CONFIG_REFERENCE =
        new DocumentReference("wiki", "PlantUML", "PlantUMLConfig");

    private static final DocumentReference AUTHOR_REFERENCE = new DocumentReference("wiki", "XWiki", "Author");

    @InjectMockComponents
    private PlantUMLConfigClassDocumentConfigurationSource configurationSource;

    @MockComponent
    private CacheManager cacheManager;

    @MockComponent
    private ModelContext modelContext;

    @MockComponent
    private WikiDescriptorManager wikiDescriptorManager;

    @MockComponent
    private Provider<XWikiContext> xcontextProvider;

    @MockComponent
    private ConverterManager converterManager;

    @MockComponent
    private AuthorizationManager authorizationManager;

    @MockComponent
    @Named("document")
    private UserReferenceSerializer<DocumentReference> documentUserReferenceSerializer;

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @BeforeComponent
    void beforeComponent() throws Exception
    {
        when(this.cacheManager.createNewCache(any())).thenReturn(mock(Cache.class));
        when(this.modelContext.getCurrentEntityReference()).thenReturn(new WikiReference("wiki"));
    }

    @BeforeEach
    void setUp() throws Exception
    {
        when(this.wikiDescriptorManager.getCurrentWikiId()).thenReturn("wiki");

        XWikiContext xcontext = mock(XWikiContext.class);
        when(this.xcontextProvider.get()).thenReturn(xcontext);
        XWiki xwiki = mock(XWiki.class);
        when(xcontext.getWiki()).thenReturn(xwiki);

        XWikiDocument document = mock(XWikiDocument.class);
        when(xwiki.getDocument(CONFIG_REFERENCE, xcontext)).thenReturn(document);
        when(document.getDocumentReference()).thenReturn(CONFIG_REFERENCE);
        DocumentAuthors authors = mock(DocumentAuthors.class);
        when(document.getAuthors()).thenReturn(authors);
        UserReference author = mock(UserReference.class);
        when(authors.getEffectiveMetadataAuthor()).thenReturn(author);
        when(this.documentUserReferenceSerializer.serialize(author)).thenReturn(AUTHOR_REFERENCE);

        BaseObject baseObject = mock(BaseObject.class);
        when(document.getXObject((EntityReference) new LocalDocumentReference("PlantUML", "PlantUMLConfigClass")))
            .thenReturn(baseObject);
        when(baseObject.getOwnerDocument()).thenReturn(document);
        StringProperty property = new StringProperty();
        property.setValue(SERVER_URL);
        when(baseObject.getField("server")).thenReturn(property);

        when(this.converterManager.convert(any(), any())).then(invocation -> invocation.getArgument(1));
    }

    @Test
    void getPropertyWhenAuthorIsAdmin()
    {
        when(this.authorizationManager.hasAccess(Right.ADMIN, AUTHOR_REFERENCE, new WikiReference("wiki")))
            .thenReturn(true);

        assertEquals(SERVER_URL, this.configurationSource.getProperty("server"));
    }

    @Test
    void getPropertyWhenAuthorIsNotAdmin()
    {
        assertNull(this.configurationSource.getProperty("server"));

        assertEquals("Ignoring the PlantUML configuration from [wiki:PlantUML.PlantUMLConfig] since its last author "
            + "[wiki:XWiki.Author] doesn't have admin right on the wiki.", this.logCapture.getMessage(0));
    }
}
