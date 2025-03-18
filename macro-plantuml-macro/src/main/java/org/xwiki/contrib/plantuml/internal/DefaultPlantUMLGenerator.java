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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.poi.util.IOUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.plantuml.PlantUMLGenerator;

import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.code.TranscoderUtil;

/**
 * Generate an image from a textual definition, using PlantUML.
 *
 * @version $Id$
 * @since 2.0
 */
@Component
@Singleton
public class DefaultPlantUMLGenerator implements PlantUMLGenerator
{
    @Override
    public void outputImage(String input, OutputStream outputStream, String serverURL) throws IOException
    {
        FileFormat fileFormat = FileFormat.PNG;
        if (StringUtils.isEmpty(serverURL)) {
            internalGenerator(input, outputStream, fileFormat);
        } else {
            externalGenerator(input, outputStream, serverURL, fileFormat.name().toLowerCase());
        }
    }

    private void internalGenerator(String input, OutputStream outputStream, FileFormat fileFormat) throws IOException
    {
        new SourceStringReader(input).outputImage(outputStream, new FileFormatOption(fileFormat));
    }

    private void externalGenerator(String input, OutputStream outputStream, String serverURL, String outputFormat)
            throws IOException
    {
        // Call the remote server, by passing the input text compressed and encoded, see
        // https://plantuml.com/text-encoding
        String baseURL = StringUtils.removeEnd(serverURL, "/");
        String compressedInput = TranscoderUtil.getDefaultTranscoder().encode(input);
        String fullURL = String.format("%s/%s/%s", baseURL, outputFormat, compressedInput);
        // Call the server and get the response
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(fullURL);
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                handleResponse(response, outputStream, fullURL);
            }
        }
    }

    private void handleResponse(CloseableHttpResponse response, OutputStream outputStream, String fullURL)
        throws IOException
    {
        int status = response.getCode();
        if (status >= HttpStatus.SC_SUCCESS && status < HttpStatus.SC_REDIRECTION) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try (InputStream inputStream = entity.getContent()) {
                    IOUtils.copy(inputStream, outputStream);
                }
            }
        } else {
            throw new IOException(String.format("Unexpected response status for [%s] : [%s]", fullURL,
                status));
        }
    }
}
