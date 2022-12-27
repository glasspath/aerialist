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
package org.glasspath.aerialist;

import org.glasspath.aerialist.media.MediaCache;

@SuppressWarnings("nls")
public class XDoc {

	// Extension format: gp = glasspath, ? = type, x = xml
	public static final String DOCUMENT_EXTENSION = "gpdx";
	public static final String EMAIL_EXTENSION = "gpex";
	// public static final String SPREADSHEET_EXTENSION = "gpsx";
	// public static final String PRESENTATION_EXTENSION = "gppx";

	public static final String VERSION_INFO = "0.0.1";
	public static final String VERSION_INFO_PATH = "version.info";
	public static final String CONTENT_PATH = "content.xml";
	public static final String IMAGES_PATH = "media/images/";

	private String versionInfo = VERSION_INFO;
	private Content content = null;
	private MediaCache<?> mediaCache = null;

	public XDoc() {

	}

	public String getVersionInfo() {
		return versionInfo;
	}

	public void setVersionInfo(String versionInfo) {
		this.versionInfo = versionInfo;
	}

	public Content getContent() {
		return content;
	}

	public void setContent(Content content) {
		this.content = content;
	}

	public MediaCache<?> getMediaCache() {
		return mediaCache;
	}

	public void setMediaCache(MediaCache<?> mediaCache) {
		this.mediaCache = mediaCache;
	}

}
