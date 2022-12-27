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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("nls")
public class HtmlExporter extends ContentParser {

	private String imageSrcPrefix = "";
	private String defaultFontStyle = " style=\"font-family: Segoe UI, Verdana, Arial, Helvetica, sans-serif; font-size: 11pt\"";
	private String html = "";
	private String plainText = "";

	public HtmlExporter() {

	}

	public String getImageSrcPrefix() {
		return imageSrcPrefix;
	}

	public void setImageSrcPrefix(String imageSrcPrefix) {
		this.imageSrcPrefix = imageSrcPrefix;
	}

	public String getDefaultFontStyle() {
		return defaultFontStyle;
	}

	public void setDefaultFontStyle(String defaultFontStyle) {
		this.defaultFontStyle = defaultFontStyle;
	}

	public void parse(ContentRoot root) {
		parse(root, "");
	}

	public void parse(ContentRoot root, String imageSrcPrefix) {

		this.imageSrcPrefix = imageSrcPrefix;

		html = "<html>\n<body" + defaultFontStyle + ">\n";
		plainText = "";

		parseRoot(root);

		html += "</body>\n</html>";

	}

	public String getPlainText() {
		return plainText;
	}

	public String getHtml() {
		return html;
	}

	public String toHtml(ContentRoot root) {
		return toHtml(root, "");
	}

	public String toHtml(ContentRoot root, String imageSrcPrefix) {
		parse(root, imageSrcPrefix);
		return html;
	}

	@Override
	public void parseTable(Table table) {

		html += "<table" + defaultFontStyle + " width=\"100%\">\n";

		if (table.getTableCells().size() > 0) {

			List<TableCell> sortedTableCells = new ArrayList<>();
			sortedTableCells.addAll(table.getTableCells());

			Collections.sort(sortedTableCells, new Comparator<TableCell>() {

				@Override
				public int compare(TableCell o1, TableCell o2) {
					int rowCompare = Integer.compare(o1.getRow(), o2.getRow());
					if (rowCompare == 0) {
						return Integer.compare(o1.getCol(), o2.getCol());
					} else {
						return rowCompare;
					}
				}
			});

			int row = -1;
			for (TableCell tableCell : sortedTableCells) {

				if (tableCell.getRow() != row) {

					if (row > 0) {
						html += "</tr>\n";
					}
					html += "<tr>\n";

					row = tableCell.getRow();

				}

				html += "<td";
				if (tableCell.getRowSpan() > 1) {
					html += " rowspan=\"" + tableCell.getRowSpan() + "\"";
				}
				if (tableCell.getColSpan() > 1) {
					html += " colspan=\"" + tableCell.getColSpan() + "\"";
				}
				if (Alignment.get(tableCell.getAlignment()) != Alignment.LEFT) {
					html += " align=\"" + tableCell.getAlignment() + "\"";
				}
				html += ">\n";

				parseIText(tableCell);

				html += "\n</td>\n";

			}

			html += "</tr>\n";

		}

		html += "</table>\n";

	}

	@Override
	public void parseIText(IText iText) {

		String text = iText.getText();
		String htmlText = "";

		String s;
		for (TextStyle textStyle : iText.getStyles()) {

			s = "";

			if (textStyle.image != null) {

				s += "<img src=\"" + imageSrcPrefix + textStyle.image + "\">";

			} else {

				if (textStyle.bold) {
					s += "<b>";
				}
				if (textStyle.italic) {
					s += "<i>";
				}
				if (textStyle.underline) {
					s += "<u>";
				}

				String subString = text.substring(textStyle.start, textStyle.end);

				s += subString;
				plainText += subString;

				if (textStyle.underline) {
					s += "</u>";
				}
				if (textStyle.italic) {
					s += "</i>";
				}
				if (textStyle.bold) {
					s += "</b>";
				}

			}

			htmlText += s;

		}

		htmlText = htmlText.replaceAll("\n", "<br>\n");

		html += htmlText;

	}

}
