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
package org.glasspath.aerialist.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.glasspath.aerialist.XDoc;
import org.glasspath.aerialist.media.MediaCache.ImageResource;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class XDocWriter {

	private XDocWriter() {

	}

	public static boolean write(XDoc xDoc, File file) {

		try {

			ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(file));

			ZipEntry zipEntry = new ZipEntry(XDoc.VERSION_INFO_PATH);
			zipOutputStream.putNextEntry(zipEntry);
			zipOutputStream.write(XDoc.VERSION_INFO.getBytes());
			zipOutputStream.closeEntry();

			if (xDoc.getContent() != null) {

				zipEntry = new ZipEntry(XDoc.CONTENT_PATH);
				zipOutputStream.putNextEntry(zipEntry);
				createXmlMapper().writeValue(zipOutputStream, xDoc.getContent());
				zipOutputStream.closeEntry();

				if (xDoc.getMediaCache() != null) {

					List<String> imageKeys = xDoc.getContent().getImageKeys();

					for (Entry<String, ImageResource> entry : xDoc.getMediaCache().getImageResources().entrySet()) {

						if (imageKeys.contains(entry.getKey())) {

							zipEntry = new ZipEntry(XDoc.IMAGES_PATH + entry.getKey());
							zipOutputStream.putNextEntry(zipEntry);
							zipOutputStream.write(entry.getValue().getBytes());
							zipOutputStream.closeEntry();

						}

					}

				}

			}

			zipOutputStream.close();

			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;

	}

	// TODO: Move to XmlUtils
	public static XmlMapper createXmlMapper() {

		XmlFactory xmlFactory = new XmlFactory();
		xmlFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false); // When writing XML to a ZipOutputStream we don't want the stream to be closed automatically

		XmlMapper mapper = new XmlMapper(xmlFactory);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, false);
		mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		mapper.setSerializationInclusion(Include.NON_NULL);

		// mapper.configure(SerializationFeature.CLOSE_CLOSEABLE, false);

		return mapper;

	}

}
