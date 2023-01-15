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

import java.io.File;

import org.glasspath.aerialist.Document;
import org.glasspath.aerialist.IFieldContext;
import org.glasspath.aerialist.XDoc;
import org.glasspath.aerialist.layout.DefaultLayoutContext;
import org.glasspath.aerialist.layout.DefaultLayoutMetrics;
import org.glasspath.aerialist.layout.IElementLayoutMetrics;
import org.glasspath.aerialist.layout.LayoutListener;
import org.glasspath.aerialist.reader.XDocReader;
import org.glasspath.aerialist.template.TemplateDocumentLoader;

import com.itextpdf.text.Font;
import com.itextpdf.text.Image;

public class ITextDocumentLoader {

	private LayoutListener layoutListener = null;

	public ITextDocumentLoader() {

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

	public Document loadDocument(File file, IFieldContext templateFieldContext, File fontsDir, File outputFile) {

		if (file.exists()) {

			ITextMediaCache mediaCache = new ITextMediaCache();

			XDoc xDoc = XDocReader.read(file.getAbsolutePath(), mediaCache);
			if (xDoc != null && xDoc.getContent() != null && xDoc.getContent().getRoot() instanceof Document) {

				Document document = (Document) xDoc.getContent().getRoot();

				if (templateFieldContext != null) {

					ITextFontCache fontCache = new ITextFontCache();
					if (fontsDir != null && fontsDir.exists()) {
						fontCache.registerFonts(fontsDir);
					}

					DefaultLayoutContext<Font, Image> layoutContext = new DefaultLayoutContext<>(fontCache, mediaCache);

					TemplateDocumentLoader documentLoader = new TemplateDocumentLoader(layoutListener, layoutContext) {

						@Override
						protected IElementLayoutMetrics createLayoutMetrics() {
							return new DefaultLayoutMetrics(layoutContext);
						}
					};

					if (outputFile != null) {
						documentLoader.setDocumentWriter(new ITextDocumentWriter(outputFile, fontCache, mediaCache));
					}

					documentLoader.loadDocument(document, templateFieldContext);

				}

				return document;

			}

		}

		return null;

	}

}
