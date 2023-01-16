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
package org.glasspath.aerialist.pdfbox;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.glasspath.aerialist.text.TextUtils.SpanInfo;
import org.glasspath.aerialist.text.font.FontCache;
import org.glasspath.aerialist.text.font.FontWeight;

public class PdfBoxFontCache extends FontCache<PDFont> {

	private PDDocument document = null;

	public PdfBoxFontCache() {

	}

	public PDDocument getDocument() {
		return document;
	}

	public void setDocument(PDDocument document) {
		this.document = document;
	}

	@Override
	protected PDFont createDefaultFont(FontWeight weight, boolean italic) {
		return null; // TODO
	}

	@Override
	protected void loadFontFile(FontFile fontFile) {

		if (document != null) {

			try {

				// PDFont font = PDTrueTypeFont.load(document, fontFile.file, StandardEncoding.INSTANCE);
				PDFont font = PDType0Font.load(document, fontFile.file);
				fontFile.font = font;

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	@Override
	public SpanInfo getSpanInfo(String text, String fontName, float fontSize, FontWeight weight, boolean italic) {

		SpanInfo spanInfo = new SpanInfo();

		PDFont font = null;

		spanInfo.fontIndex = getFontIndex(fontName, weight, italic);
		if (spanInfo.fontIndex >= 0) {

			FontCache<PDFont>.CachedFont cachedFont = getFont(spanInfo.fontIndex);
			if (cachedFont != null && cachedFont.fontFile != null) {
				font = cachedFont.fontFile.font;
			}

		}

		if (font == null) {
			font = createDefaultFont(weight, italic);
		}

		if (font != null) {

			// TODO: \n is not supported, this should be removed before calling getSpanInfo
			text = text.replace("\n", ""); //$NON-NLS-1$ //$NON-NLS-2$

			try {
				spanInfo.width = (font.getStringWidth(text) / 1000.0F) * fontSize;
			} catch (Exception e) {
				e.printStackTrace(); // TODO
			}

			spanInfo.ascent = (font.getFontDescriptor().getAscent() / 1000.0F) * fontSize;
			spanInfo.descent = -(font.getFontDescriptor().getDescent() / 1000.0F) * fontSize;

		}

		return spanInfo;

	}

}
