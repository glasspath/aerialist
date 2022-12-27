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

import java.io.File;
import java.io.FileOutputStream;

import org.glasspath.aerialist.text.font.FontCache;
import org.glasspath.aerialist.text.font.FontWeight;
import org.glasspath.aerialist.writer.DocumentWriter;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

public class OpenPdfDocumentWriter extends DocumentWriter {

	private final OpenPdfFontCache fontCache;
	private final OpenPdfMediaCache mediaCache;
	private Document document = null;
	private boolean documentOpen = false;
	private PdfWriter writer = null;
	private PdfContentByte cb = null;
	private boolean firstPage = true;
	private PdfTemplate template = null;
	private int pageHeight = 0;

	public OpenPdfDocumentWriter(File file, OpenPdfFontCache fontCache, OpenPdfMediaCache mediaCache) {
		super(file);

		this.fontCache = fontCache;
		this.mediaCache = mediaCache;

	}

	@Override
	protected void openDocument(int width, int height) throws Exception {

		document = new Document(new Rectangle(width, height));
		writer = PdfWriter.getInstance(document, new FileOutputStream(file));

		document.open();
		cb = writer.getDirectContent();

		/*
		document.addTitle("Test title");
		document.addAuthor("Test author");
		document.addSubject("Test subject");
		document.addCreator("Glasspath Aerialist");
		*/

		documentOpen = true;

	}

	@Override
	public boolean isDocumentOpen() {
		return documentOpen;
	}

	@Override
	public void openPage(int width, int height) throws Exception {

		if (documentOpen) {

			if (firstPage) {
				firstPage = false;
			} else {
				document.newPage();
			}

			template = cb.createTemplate(width, height);
			pageHeight = height;

		}

	}

	@Override
	protected void saveState() throws Exception {
		cb.saveState();
	}

	@Override
	protected void restoreState() throws Exception {
		cb.restoreState();
	}

	@Override
	protected void setFillColor(int r, int g, int b, int a) throws Exception {
		cb.setRGBColorFill(r, g, b, a);
	}

	@Override
	protected void setStrokeColor(int r, int g, int b, int a) throws Exception {
		cb.setRGBColorStroke(r, g, b, a);
	}

	@Override
	protected void setStroke(float w) throws Exception {
		cb.setLineWidth(w);
	}

	@Override
	protected void clip(float x, float y, float w, float h) throws Exception {

		y = pageHeight - y - h;

		cb.moveTo(x, y);
		cb.lineTo(x + w, y);
		cb.lineTo(x + w, y + h);
		cb.lineTo(x, y + h);
		cb.closePath();

		cb.clip();

		cb.newPath();

	}

	@Override
	protected void fill(float x, float y, float w, float h) throws Exception {

		y = pageHeight - y - h;

		cb.moveTo(x, y);
		cb.lineTo(x + w, y);
		cb.lineTo(x + w, y + h);
		cb.lineTo(x, y + h);
		cb.closePath();

		cb.fill();

		cb.newPath();

	}

	@Override
	protected void draw(float x, float y, float w, float h) throws Exception {

		y = pageHeight - y - h;

		cb.moveTo(x, y);
		cb.lineTo(x + w, y);
		cb.lineTo(x + w, y + h);
		cb.lineTo(x, y + h);
		cb.closePath();

		cb.stroke();

		cb.newPath();

	}

	@Override
	protected void drawLine(float x1, float y1, float x2, float y2) throws Exception {

		y1 = pageHeight - y1;
		y2 = pageHeight - y2;

		cb.moveTo(x1, y1);
		cb.lineTo(x2, y2);

		cb.stroke();

		cb.newPath();

	}

	@Override
	protected void drawImage(String key, float x, float y, float w, float h, String name) throws Exception {

		y = pageHeight - y - h;

		Image image = mediaCache.getImage(key);
		if (image != null) {
			cb.addImage(image, w, 0, 0, h, x, y);
		}

	}

	@Override
	protected void beginText() throws Exception {
		cb.beginText();
	}

	@Override
	protected void drawString(String s, float x, float y, int fontIndex, float fontSize, boolean bold, boolean italic) throws Exception {

		if (fontIndex >= 0) {

			BaseFont font = null;
			float fontAngle = 0.0F;

			FontCache<BaseFont>.CachedFont cachedFont = fontCache.getFont(fontIndex);
			if (cachedFont != null && cachedFont.fontFile != null && cachedFont.fontFile.font != null) {

				font = cachedFont.fontFile.font;

				// Check if italic needs to be simulated (the FontFile class tells us if the loaded font is italic or not)
				if (italic && !cachedFont.fontFile.italic) {

					// TODO: The FontFile check above is based on the font-name containing the text 'italic'
					// After loading the font we have more detailed information available, here we check the angle from the font
					float angle = font.getFontDescriptor(BaseFont.ITALICANGLE, 1000);
					if (angle == 0) {
						fontAngle = 15.0F / 100.0F;
					}

				}

			}

			if (font == null) {
				font = fontCache.createDefaultFont(bold ? FontWeight.BOLD : FontWeight.REGULAR, italic);
			}

			if (font != null) {

				y = pageHeight - y;

				// cb.beginText();

				cb.setFontAndSize(font, fontSize);

				cb.setTextMatrix(1.0F, 0.0F, fontAngle, 1.0F, x, y);

				cb.showText(s);

				// cb.endText();

			}

		}

	}

	@Override
	protected void endText() throws Exception {
		cb.endText();
	}

	@Override
	public void closePage() throws Exception {
		if (documentOpen) {
			cb.addTemplate(template, 0, 0);
		}
	}

	@Override
	protected void closeDocument() throws Exception {

		if (documentOpen) {

			documentOpen = false;

			document.close();
			writer.close();

			document = null;

		}

	}

}
