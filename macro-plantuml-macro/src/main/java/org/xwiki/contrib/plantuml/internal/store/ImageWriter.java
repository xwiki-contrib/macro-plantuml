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
package org.xwiki.contrib.plantuml.internal.store;

import java.io.OutputStream;

import org.xwiki.component.annotation.Role;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.url.ExtendedURL;

/**
 * Writes an Image represented as an array of Bytes to storage. Also provides a helper method to get an XWiki URL to
 * access the written data.
 *
 * @version $Id$
 * @since 2.0
 */
@Role
public interface ImageWriter
{
    /**
     * @param imageId the image id that we use to generate a unique storage location
     * @return the output stream into which to write to save the image data to disk
     * @throws MacroExecutionException if the target file cannot be created (already exists and is a directory, etc)
     */
    OutputStream getOutputStream(String imageId) throws MacroExecutionException;

    /**
     * Compute the URL to use to access the stored generate chart image.
     *
     * @param imageId the image id for the image that we have stored
     * @return the URL to use to access the stored generate chart image
     * @throws MacroExecutionException if an error happened when computing the URL (eg if the current wiki cannot be
     *         computed)
     */
    ExtendedURL getURL(String imageId) throws MacroExecutionException;
}
