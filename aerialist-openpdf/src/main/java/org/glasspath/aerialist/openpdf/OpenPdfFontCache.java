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
package org.glasspath.aerialist.openpdf;

import org.glasspath.aerialist.text.TextUtils.SpanInfo;
import org.glasspath.aerialist.text.font.FontCache;
import org.glasspath.aerialist.text.font.FontWeight;

import com.lowagie.text.pdf.BaseFont;

public class OpenPdfFontCache extends FontCache<BaseFont> {

	public OpenPdfFontCache() {

	}

	@Override
	protected BaseFont createDefaultFont(FontWeight weight, boolean italic) {

		try {

			String fontKey = BaseFont.HELVETICA;
			if (weight.isBoldWeight() && italic) {
				fontKey = BaseFont.HELVETICA_BOLDOBLIQUE;
			} else if (weight.isBoldWeight()) {
				fontKey = BaseFont.HELVETICA_BOLD;
			} else if (italic) {
				fontKey = BaseFont.HELVETICA_OBLIQUE;
			}

			return BaseFont.createFont(fontKey, BaseFont.CP1252, false);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	@Override
	protected void loadFontFile(FontFile fontFile) {

		try {

			BaseFont font = BaseFont.createFont(fontFile.file.getAbsolutePath(), BaseFont.CP1252, BaseFont.EMBEDDED, BaseFont.CACHED, null, null);
			fontFile.font = font;

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public SpanInfo getSpanInfo(String text, String fontName, float fontSize, FontWeight weight, boolean italic) {

		SpanInfo spanInfo = new SpanInfo();

		BaseFont font = null;

		spanInfo.fontIndex = getFontIndex(fontName, weight, italic);
		if (spanInfo.fontIndex >= 0) {

			FontCache<BaseFont>.CachedFont cachedFont = getFont(spanInfo.fontIndex);
			if (cachedFont != null && cachedFont.fontFile != null) {
				font = cachedFont.fontFile.font;
			}

		}

		if (font == null) {
			font = createDefaultFont(weight, italic);
		}

		if (font != null) {

			spanInfo.width = font.getWidthPointKerned(text, fontSize);
			// spanInfo.ascent = font.getAscentPoint(text, fontSize);
			spanInfo.ascent = font.getFontDescriptor(BaseFont.AWT_ASCENT, fontSize);
			// spanInfo.descent = -font.getDescentPoint(text, fontSize);
			spanInfo.descent = -font.getFontDescriptor(BaseFont.AWT_DESCENT, fontSize);

			// spanInfo.height = font.getFontDescriptor(BaseFont.BBOXURY, fontSize) - font.getFontDescriptor(BaseFont.BBOXLLY, fontSize);
			// System.out.println(spanInfo.height + ", " + fontSize);

		}

		return spanInfo;

	}

}
