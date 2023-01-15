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
package org.glasspath.aerialist.itext;

import org.glasspath.aerialist.text.TextUtils.SpanInfo;
import org.glasspath.aerialist.text.font.FontCache;
import org.glasspath.aerialist.text.font.FontWeight;

import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;

public class ITextFontCache extends FontCache<Font> {

	public ITextFontCache() {

	}

	@Override
	protected Font createDefaultFont(FontWeight weight, boolean italic) {
		return null; // TODO
	}

	@Override
	protected void loadFontFile(FontFile fontFile) {

		try {

			BaseFont baseFont = BaseFont.createFont(fontFile.file.getAbsolutePath(), BaseFont.CP1252, BaseFont.EMBEDDED, BaseFont.CACHED, null, null);
			fontFile.font = new Font(baseFont);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public SpanInfo getSpanInfo(String text, String fontName, float fontSize, FontWeight weight, boolean italic) {

		SpanInfo spanInfo = new SpanInfo();

		Font font = null;

		spanInfo.fontIndex = getFontIndex(fontName, weight, italic);
		if (spanInfo.fontIndex >= 0) {

			FontCache<Font>.CachedFont cachedFont = getFont(spanInfo.fontIndex);
			if (cachedFont != null && cachedFont.fontFile != null) {
				font = cachedFont.fontFile.font;
			}

		}

		if (font == null) {
			font = createDefaultFont(weight, italic);
		}

		if (font != null) {

			spanInfo.width = font.getBaseFont().getWidthPointKerned(text, fontSize);
			spanInfo.ascent = font.getBaseFont().getFontDescriptor(BaseFont.AWT_ASCENT, fontSize);
			spanInfo.descent = -font.getBaseFont().getFontDescriptor(BaseFont.AWT_DESCENT, fontSize);

		}

		return spanInfo;

	}

}
