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
package org.xwiki.contrib.plantuml;

import java.io.IOException;
import java.net.ServerSocket;

import javax.inject.Named;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.environment.Environment;
import org.xwiki.environment.internal.ServletEnvironment;
import org.xwiki.environment.internal.StandardEnvironment;
import org.xwiki.model.ModelContext;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.observation.EventListener;
import org.xwiki.rendering.test.integration.RenderingTestSuite;
import org.xwiki.resource.temporary.TemporaryResourceReference;
import org.xwiki.resource.temporary.TemporaryResourceStore;
import org.xwiki.security.authorization.AuthorizationManager;
import org.xwiki.test.XWikiTempDirUtil;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.mockito.MockitoComponentManager;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;

import net.sourceforge.plantuml.picoweb.PicoWebServer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Run all tests found in {@code *.test} files located in the classpath. These {@code *.test} files must follow the
 * conventions described in {@link org.xwiki.rendering.test.integration.TestDataParser}.
 *
 * @version $Id$
 * @since 2.0
 */
@RunWith(RenderingTestSuite.class)
@AllComponents
public class IntegrationTests
{
    private static boolean shouldStop;

    @BeforeClass
    public static void startPlantUMLServer() throws Exception
    {
        ServerSocket serverSocket = new ServerSocket(8777);

        Thread serverLoopThread = new Thread("PicoWebServerLoop") {
            @Override
            public void run() {
                try {
                    serverLoop(serverSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        serverLoopThread.setDaemon(true);
        serverLoopThread.start();
    }

    public static void serverLoop(ServerSocket serverConnect) throws IOException {
        while(!shouldStop) {
            PicoWebServer myServer = new PicoWebServer(serverConnect.accept());
            Thread thread = new Thread(myServer);
            thread.start();
        }
    }

    @AfterClass
    public static void stopPlantUMLServer()
    {
        shouldStop = true;
    }

    @RenderingTestSuite.Initialized
    public void initialize(MockitoComponentManager componentManager) throws Exception
    {
        componentManager.registerMockComponent(AuthorizationManager.class);
        componentManager.registerMockComponent(WikiDescriptorManager.class);

        DocumentAccessBridge dab = componentManager.registerMockComponent(DocumentAccessBridge.class);
        DocumentReference documentReference = new DocumentReference("wiki", "space", "page");
        when(dab.getCurrentDocumentReference()).thenReturn(documentReference);

        StandardEnvironment environment = componentManager.getInstance(Environment.class);
        environment.setTemporaryDirectory(XWikiTempDirUtil.createTemporaryDirectory());

        ConfigurationSource cs = componentManager.registerMockComponent(ConfigurationSource.class, "xwikicfg");
        when(cs.getProperty("xwiki.webapppath")).thenReturn("wikicontext");

        componentManager.unregisterComponent(EventListener.class, "refactoring.automaticRedirectCreator");
        componentManager.unregisterComponent(EventListener.class, "refactoring.relativeLinksUpdater");
        componentManager.unregisterComponent(EventListener.class, "refactoring.backLinksUpdater");
        componentManager.registerMockComponent(PlantUMLConfiguration.class);
    }
}