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

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.glasspath.aerialist.Document;
import org.glasspath.aerialist.IFieldContext;
import org.glasspath.aerialist.XDoc;
import org.glasspath.aerialist.Page.PageSize;
import org.glasspath.aerialist.layout.DefaultLayoutContext;
import org.glasspath.aerialist.layout.DefaultLayoutMetrics;
import org.glasspath.aerialist.layout.IElementLayoutMetrics;
import org.glasspath.aerialist.layout.LayoutListener;
import org.glasspath.aerialist.reader.XDocReader;
import org.glasspath.aerialist.template.TemplateDocumentLoader;

public class PdfBoxDocumentLoader {

	private LayoutListener layoutListener = null;

	public PdfBoxDocumentLoader() {

	}

	public LayoutListener getLayoutListener() {
		return layoutListener;
	}

	public void setLayoutListener(LayoutListener layoutListener) {
		this.layoutListener = layoutListener;
	}

	public Document loadDocument(String path) {
		return loadDocument(new File(path));
	}

	public Document loadDocument(File file) {
		return loadDocument(file, null, null, null);
	}

	public Document loadDocument(File file, IFieldContext templateFieldContext, String fontsPath, File outputFile) {

		if (file.exists()) {

			try {

				PdfBoxMediaCache mediaCache = new PdfBoxMediaCache();

				PdfBoxFontCache fontCache = new PdfBoxFontCache();
				fontCache.registerFonts(fontsPath);

				DefaultLayoutContext<PDFont, PDImageXObject> layoutContext = new DefaultLayoutContext<>(fontCache, mediaCache);

				TemplateDocumentLoader documentLoader = new TemplateDocumentLoader(layoutListener, layoutContext) {

					@Override
					protected IElementLayoutMetrics createLayoutMetrics() {
						return new DefaultLayoutMetrics(layoutContext);
					}
				};

				if (outputFile != null) {
					documentLoader.setDocumentWriter(new PdfBoxDocumentWriter(outputFile, fontCache, mediaCache));
				}

				// TODO: We need to open the document first because PdfBoxMediaCache and PdfBoxFontCache need a PDDocument
				try {
					documentLoader.getDocumentWriter().open(PageSize.A4.getWidth(), PageSize.A4.getHeight()); // TODO
				} catch (Exception e) {
					e.printStackTrace(); // TODO
				}

				XDoc xDoc = XDocReader.read(file.getAbsolutePath(), mediaCache);
				if (xDoc != null && xDoc.getContent() != null && xDoc.getContent().getRoot() instanceof Document) {

					Document document = (Document) xDoc.getContent().getRoot();

					if (templateFieldContext != null) {
						documentLoader.loadDocument(document, templateFieldContext, mediaCache);
					}

					return document;

				}

			} catch (Exception e) {
				e.printStackTrace(); // TODO
			}

		}

		return null;

	}

}
