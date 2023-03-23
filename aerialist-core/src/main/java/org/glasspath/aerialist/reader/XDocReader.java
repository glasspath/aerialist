/*
 * This file is part of Glasspath Aerialist.
 * Copyright (C) 2011 - 2022 Remco Poelstra
 * Authors: Remco Poelstra
 * 
 * This program is offered under a commercial and under the AGPL license.
 * For commercial licensing, contact us at https://glasspath.org. For AGPL licensing, see below.
 * 
 * AGPL licensing:
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.glasspath.aerialist.reader;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.glasspath.aerialist.Content;
import org.glasspath.aerialist.XDoc;
import org.glasspath.aerialist.media.MediaCache;
import org.glasspath.aerialist.writer.XDocWriter;

public class XDocReader {

	private XDocReader() {

	}

	public static XDoc read(String path, MediaCache<?> mediaCache) {

		try {

			XDoc xDoc = new XDoc();
			xDoc.setVersionInfo(""); //$NON-NLS-1$

			xDoc.setMediaCache(mediaCache);

			ZipFile zipFile = new ZipFile(path);

			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			while (zipEntries.hasMoreElements()) {

				ZipEntry zipEntry = zipEntries.nextElement();

				String name = zipEntry.getName();

				if (XDoc.VERSION_INFO_PATH.equals(name)) {

					InputStream inputStream = zipFile.getInputStream(zipEntry);
					xDoc.setVersionInfo(new String(inputStream.readAllBytes()));
					inputStream.close();

				} else if (XDoc.CONTENT_PATH.equals(name)) {

					InputStream inputStream = zipFile.getInputStream(zipEntry);
					xDoc.setContent(XDocWriter.createXmlMapper().readValue(inputStream, Content.class));
					inputStream.close();

				} else if (name.startsWith(XDoc.IMAGES_PATH)) {

					InputStream inputStream = zipFile.getInputStream(zipEntry);

					String imageKey = name.substring(XDoc.IMAGES_PATH.length());
					byte[] imageBytes = inputStream.readAllBytes();
					mediaCache.putImage(imageKey, imageBytes);

					inputStream.close();

				}

			}

			zipFile.close();

			return xDoc;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

}
