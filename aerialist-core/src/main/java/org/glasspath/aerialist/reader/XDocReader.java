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

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.glasspath.aerialist.Content;
import org.glasspath.aerialist.XDoc;
import org.glasspath.aerialist.media.BasicMediaCache;
import org.glasspath.aerialist.media.MediaCache;
import org.glasspath.aerialist.writer.XDocWriter;

public class XDocReader {

	private XDocReader() {

	}

	public static XDoc read(String path) throws IOException {
		return read(path, new BasicMediaCache());
	}

	public static XDoc read(String path, MediaCache<?> mediaCache) throws IOException {
		return read(path, mediaCache, null);
	}

	public static XDoc read(String path, MediaCache<?> mediaCache, XDocEntryReader entryReader) throws IOException {

		XDoc xDoc = new XDoc();
		xDoc.setVersionInfo(""); //$NON-NLS-1$

		xDoc.setMediaCache(mediaCache);

		ZipFile zipFile = new ZipFile(path);

		Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
		while (zipEntries.hasMoreElements()) {

			ZipEntry zipEntry = zipEntries.nextElement();

			InputStream inputStream = zipFile.getInputStream(zipEntry);
			readEntry(xDoc, zipEntry.getName(), inputStream, mediaCache, entryReader);
			inputStream.close();

		}

		zipFile.close();

		return xDoc;

	}

	public static XDoc read(InputStream inputStream, MediaCache<?> mediaCache, XDocEntryReader entryReader) throws IOException {

		XDoc xDoc = new XDoc();
		xDoc.setVersionInfo(""); //$NON-NLS-1$

		xDoc.setMediaCache(mediaCache);

		ZipInputStream zipInputStream = new ZipInputStream(inputStream);

		ZipEntry zipEntry = null;
		while ((zipEntry = zipInputStream.getNextEntry()) != null) {
			readEntry(xDoc, zipEntry.getName(), zipInputStream, mediaCache, entryReader);
			zipInputStream.closeEntry();
		}

		zipInputStream.close();

		return xDoc;

	}

	private static void readEntry(XDoc xDoc, String name, InputStream inputStream, MediaCache<?> mediaCache, XDocEntryReader entryReader) throws IOException {

		if (entryReader != null && entryReader.readEntry(name, inputStream)) {

			// Entry was read by XDocEntryReader

		} else if (XDoc.VERSION_INFO_PATH.equals(name)) {

			xDoc.setVersionInfo(new String(inputStream.readAllBytes()));

		} else if (XDoc.CONTENT_PATH.equals(name)) {

			xDoc.setContent(XDocWriter.createXmlMapper().readValue(inputStream, Content.class));

		} else if (name.startsWith(XDoc.IMAGES_PATH)) {

			String imageKey = name.substring(XDoc.IMAGES_PATH.length());
			byte[] imageBytes = inputStream.readAllBytes();
			mediaCache.putImage(imageKey, imageBytes);

		}

	}

	public static interface XDocEntryReader {

		public boolean readEntry(String name, InputStream inputStream);

	}

}
