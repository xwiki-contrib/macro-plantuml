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
import java.nio.charset.StandardCharsets;

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
import net.sourceforge.plantuml.code.AsciiEncoder;
import net.sourceforge.plantuml.code.CompressionHuffman;

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
    private CompressionHuffman compressor = new CompressionHuffman();

    private AsciiEncoder asciiEncoder = new AsciiEncoder();

    @Override
    public void outputImage(String input, OutputStream outputStream, String serverURL, String format) throws IOException
    {
        if (StringUtils.isEmpty(serverURL)) {
            new SourceStringReader(input).outputImage(outputStream);
        } else {
            // Call the remote server, by passing the input text compressed and encoded, see
            // https://plantuml.com/text-encoding
            String compressedInput = this.asciiEncoder.encode(
                this.compressor.compress(input.getBytes(StandardCharsets.UTF_8)));
            String fullURL = String.format("%s/%s/~1%s", StringUtils.removeEnd(serverURL, "/"), format, compressedInput);
            // Call the server and get the response
            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                HttpGet httpGet = new HttpGet(fullURL);
                try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                    handleResponse(response, outputStream, fullURL);
                }
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
