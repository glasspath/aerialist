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
package org.glasspath.aerialist.text.font;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.glasspath.aerialist.text.TextUtils.SpanInfo;

@SuppressWarnings("nls")
public abstract class FontCache<T> {

	protected final List<FontFile> fontFiles = new ArrayList<>();
	protected final List<CachedFont> cachedFonts = new ArrayList<>();

	public FontCache() {

	}

	public void registerFonts(File fontsDir) {

		if (fontsDir.exists() && fontsDir.isDirectory()) {

			File[] files = fontsDir.listFiles();
			if (files != null) {

				for (File file : files) {

					if (file.isDirectory()) {
						registerFonts(file);
					} else if (isSupportedFontFile(file)) {
						fontFiles.add(new FontFile(file));
					}

				}

			}

		}

	}

	protected boolean isSupportedFontFile(File file) {
		String name = file.getName().toLowerCase();
		if (name.endsWith(".ttf") || name.endsWith(".otf") || name.endsWith(".afm")) {
			return true;
		} else {
			return false;
		}
	}

	public SpanInfo getSpanInfo(String text, String fontName, float fontSize, boolean bold, boolean italic) {
		return getSpanInfo(text, fontName, fontSize, bold ? FontWeight.BOLD : FontWeight.REGULAR, italic);
	}

	public abstract SpanInfo getSpanInfo(String text, String fontName, float fontSize, FontWeight weight, boolean italic);

	protected int getFontIndex(String fontName, FontWeight weight, boolean italic) {

		if (fontName != null) {

			for (int i = 0; i < cachedFonts.size(); i++) {

				CachedFont cachedFont = cachedFonts.get(i);
				if (fontName.equals(cachedFont.fontName) && weight == cachedFont.weight && italic == cachedFont.italic) {
					return i;
				}

			}

			return addFont(fontName, weight, italic);

		}

		return -1;

	}

	private synchronized int addFont(String fontName, FontWeight weight, boolean italic) {

		List<FontFile> fontFileMatches = new ArrayList<>();

		if (fontName != null) {

			fontName = fontName.toLowerCase().replaceAll("[^A-Za-z0-9]", "");

			for (int i = 0; i < fontFiles.size(); i++) {

				FontFile fontFile = fontFiles.get(i);
				if (fontFile.weight == weight && fontFile.italic == italic && fontFile.name.contains(fontName)) {
					fontFileMatches.add(fontFile);
				}

			}

		}

		FontFile fontFile = null;

		if (fontFileMatches.size() == 1) {
			fontFile = fontFileMatches.get(0);
		} else if (fontFileMatches.size() > 1) {
			fontFile = fontFileMatches.get(0); // TODO
			System.err.println("TODO: FontCache: " + fontName + ", multiple matching font files found, load them all and find the one with the best matching family name");
		} else {
			System.err.println("TODO: FontCache: " + fontName + ", no matching font files found, load all available font files and find the one with the best matching family name");
		}

		if (fontFile != null && fontFile.font == null) {
			loadFontFile(fontFile);
		}

		// If font is null we still add it (so we don't try to create it again later)
		cachedFonts.add(new CachedFont(fontName, weight, italic, fontFile));

		return cachedFonts.size() - 1;

	}

	protected abstract T createDefaultFont(FontWeight weight, boolean italic);

	protected abstract void loadFontFile(FontFile fontFile);

	public CachedFont getFont(int index) {
		return cachedFonts.get(index);
	}

	public List<CachedFont> getCachedFonts() {
		return cachedFonts;
	}

	public class FontFile {

		public final File file;
		public final String name;
		public final FontWeight weight;
		public final boolean italic;

		public T font = null;

		public FontFile(File file) {
			this.file = file;
			this.name = file.getName().toLowerCase().replaceAll("[^A-Za-z0-9]", "");
			this.weight = FontWeight.getFontWeight(name);
			this.italic = name.contains("italic");
		}

	}

	public class CachedFont {

		public final String fontName;
		public final FontWeight weight;
		public final boolean italic;
		public final FontFile fontFile;

		public CachedFont(String fontName, FontWeight weight, boolean italic, FontFile fontFile) {
			this.fontName = fontName;
			this.weight = weight;
			this.italic = italic;
			this.fontFile = fontFile;
		}

	}

}
