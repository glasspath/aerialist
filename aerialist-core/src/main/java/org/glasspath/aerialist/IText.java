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

import java.util.List;

@SuppressWarnings("nls")
public interface IText {

	public String getText();

	public void setText(String text);

	public String getAlignment();

	public void setAlignment(String alignment);

	public List<TextStyle> getStyles();

	public void setStyles(List<TextStyle> styles);

	public static void replaceText(IText iText, int styleIndex, String replacement) {

		String s = iText.getText();
		if (s != null && styleIndex >= 0 && styleIndex < iText.getStyles().size()) {

			TextStyle style = iText.getStyles().get(styleIndex);

			// Copy start and end (style.start and style.end will be updated later)
			int start = style.start;
			int end = style.end;

			// TODO: JTextPane includes line feed in the attributes, this means the source-attribute we use for defining fields is 'extended over' the line feed..
			String original = s.substring(start, end);
			if (original.endsWith("\n") && !replacement.endsWith("\n")) {
				replacement += "\n";
			}

			iText.setText(s.substring(0, start) + replacement + s.substring(end, s.length()));

			int delta = replacement.length() - (end - start);

			// Shift start and end index of all styles that follow
			for (int i = styleIndex; i < iText.getStyles().size(); i++) {

				TextStyle textStyle = iText.getStyles().get(i);

				if (textStyle.start >= end) {
					textStyle.start += delta;
				}
				if (textStyle.end >= end) {
					textStyle.end += delta;
				}

			}

			// If replacement contains multiple lines we have to create a style for each line
			int breakIndex = replacement.indexOf("\n");
			while (breakIndex >= 0 && breakIndex < replacement.length() - 1) {

				// Create new style for right hand side of text
				TextStyle newStyle = new TextStyle(style);
				newStyle.start = start + breakIndex + 1;

				// Update current style for left hand side
				style.end = start + breakIndex + 1;

				// Add style and go to next
				iText.getStyles().add(++styleIndex, newStyle);
				style = newStyle;

				breakIndex = replacement.indexOf("\n", breakIndex + 1);

			}

		}

	}

}
