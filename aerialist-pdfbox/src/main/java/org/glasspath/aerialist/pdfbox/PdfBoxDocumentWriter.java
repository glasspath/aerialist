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

import java.io.File;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.glasspath.aerialist.text.font.FontCache;
import org.glasspath.aerialist.text.font.FontWeight;
import org.glasspath.aerialist.writer.DocumentWriter;

public class PdfBoxDocumentWriter extends DocumentWriter {

	private final PdfBoxFontCache fontCache;
	private final PdfBoxMediaCache mediaCache;
	private PDDocument document = null;
	private boolean documentOpen = false;
	private PDPage page = null;
	private PDPageContentStream cs = null;
	private int pageHeight = 0;

	public PdfBoxDocumentWriter(File file, PdfBoxFontCache fontCache, PdfBoxMediaCache mediaCache) {
		super(file);

		this.fontCache = fontCache;
		this.mediaCache = mediaCache;

	}

	@Override
	protected void openDocument(int width, int height) throws Exception {

		document = new PDDocument();
		fontCache.setDocument(document);
		mediaCache.setDocument(document);

		documentOpen = true;

	}

	@Override
	public boolean isDocumentOpen() {
		return documentOpen;
	}

	@Override
	public void openPage(int width, int height) throws Exception {

		pageHeight = height;

		if (document != null) {

			page = new PDPage(new PDRectangle(width, height));
			document.addPage(page);

			cs = new PDPageContentStream(document, page);

		}

	}

	@Override
	protected void saveState() throws Exception {
		cs.saveGraphicsState();
	}

	@Override
	protected void restoreState() throws Exception {
		cs.restoreGraphicsState();
	}

	@Override
	protected void setFillColor(int r, int g, int b, int a) throws Exception {

		cs.setNonStrokingColor(r / 255.0F, g / 255.0F, b / 255.0F);

		if (a < 255) {

			PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
			graphicsState.setNonStrokingAlphaConstant(a / 255.0F);
			cs.setGraphicsStateParameters(graphicsState);

		}

	}

	@Override
	protected void setStrokeColor(int r, int g, int b, int a) throws Exception {

		cs.setStrokingColor(r / 255.0F, g / 255.0F, b / 255.0F);

		if (a < 255) {

			PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
			graphicsState.setStrokingAlphaConstant(a / 255.0F);
			cs.setGraphicsStateParameters(graphicsState);

		}

	}

	@Override
	protected void setStroke(float w) throws Exception {
		cs.setLineWidth(w);
	}

	@Override
	protected void clip(float x, float y, float w, float h) throws Exception {

		y = pageHeight - y - h;

		cs.moveTo(x, y);
		cs.lineTo(x + w, y);
		cs.lineTo(x + w, y + h);
		cs.lineTo(x, y + h);
		cs.closePath();

		cs.clip();

	}

	@Override
	protected void fill(float x, float y, float w, float h) throws Exception {

		y = pageHeight - y - h;

		cs.moveTo(x, y);
		cs.lineTo(x + w, y);
		cs.lineTo(x + w, y + h);
		cs.lineTo(x, y + h);
		cs.closePath();

		cs.fill();

	}

	@Override
	protected void draw(float x, float y, float w, float h) throws Exception {

		y = pageHeight - y - h;

		cs.moveTo(x, y);
		cs.lineTo(x + w, y);
		cs.lineTo(x + w, y + h);
		cs.lineTo(x, y + h);
		cs.closePath();

		cs.stroke();

	}

	@Override
	protected void drawLine(float x1, float y1, float x2, float y2) throws Exception {

		y1 = pageHeight - y1;
		y2 = pageHeight - y2;

		cs.moveTo(x1, y1);
		cs.lineTo(x2, y2);

		cs.stroke();

	}

	@Override
	protected void drawImage(String key, float x, float y, float w, float h, String name) throws Exception {

		y = pageHeight - y - h;

		PDImageXObject image = mediaCache.getImage(key);
		if (image != null) {
			cs.drawImage(image, x, y, w, h);
		}

	}

	@Override
	protected void beginText() throws Exception {
		cs.beginText();
	}

	@Override
	protected void drawString(String s, float x, float y, int fontIndex, float fontSize, boolean bold, boolean italic) throws Exception {

		if (fontIndex >= 0) {

			PDFont font = null;
			float fontAngle = 0.0F;

			FontCache<PDFont>.CachedFont cachedFont = fontCache.getFont(fontIndex);
			if (cachedFont != null && cachedFont.fontFile != null) {

				font = cachedFont.fontFile.font;

				// Check if italic needs to be simulated (the FontFile class tells us if the loaded font is italic or not)
				if (italic && !cachedFont.fontFile.italic) {
					fontAngle = 15.0F / 100.0F;
				}

			}

			if (font == null) {
				font = fontCache.createDefaultFont(bold ? FontWeight.BOLD : FontWeight.REGULAR, italic);
			}

			if (font != null) {

				y = pageHeight - y;

				// cs.beginText();

				cs.setFont(font, fontSize);

				cs.setTextMatrix(new Matrix(1.0F, 0.0F, fontAngle, 1.0F, x, y));

				cs.showText(s);

				// cs.endText();

			}

		}

	}

	@Override
	protected void endText() throws Exception {
		cs.endText();
	}

	@Override
	public void closePage() throws Exception {
		if (cs != null) {
			cs.close();
		}
	}

	@Override
	protected void closeDocument() throws Exception {

		if (documentOpen) {

			documentOpen = false;

			mediaCache.setDocument(null);

			document.save(file);
			document.close();

			document = null;

		}

	}

}
