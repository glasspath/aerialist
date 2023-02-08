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
package org.glasspath.aerialist.template;

import java.util.ArrayList;
import java.util.List;

import org.glasspath.aerialist.ContentParser;
import org.glasspath.aerialist.ContentRoot;
import org.glasspath.aerialist.Document;
import org.glasspath.aerialist.Field;
import org.glasspath.aerialist.IFieldContext;
import org.glasspath.aerialist.IText;
import org.glasspath.aerialist.Image;
import org.glasspath.aerialist.Page;
import org.glasspath.aerialist.Table;
import org.glasspath.aerialist.TableCell;
import org.glasspath.aerialist.TextStyle;

public class TemplateParser extends ContentParser {

	private IFieldContext fieldContext = null;

	public TemplateParser() {

	}

	public void parseTemplate(ContentRoot template, IFieldContext templateContext) {

		if (templateContext != null) {
			this.fieldContext = templateContext;
		} else {
			this.fieldContext = new TemplateFieldContext();
		}

		parseRoot(template);

		this.fieldContext = null;

	}

	@Override
	public void prepareDocument(Document document) {

		if (document.getHeader() != null && !isPageVisisble(document.getHeader())) {
			document.setHeader(null);
		}

		if (document.getFooter() != null && !isPageVisisble(document.getFooter())) {
			document.setFooter(null);
		}

		List<Page> removePages = new ArrayList<>();

		for (Page page : document.getPages()) {
			if (!isPageVisisble(page)) {
				removePages.add(page);
			}
		}

		document.getPages().removeAll(removePages);

	}

	public boolean isPageVisisble(Page page) {
		return page.getVisible() == null || !isFieldValueFalse(page.getVisible());
	}

	@Override
	public void parseTable(Table table) {

		NewTableCellData newTableCellData = new NewTableCellData();

		for (TableCell tableCell : table.getTableCells()) {

			for (int i = 0; i < tableCell.getStyles().size(); i++) {

				TextStyle textStyle = tableCell.getStyles().get(i);
				if (textStyle.source != null) {

					Field field = new Field(textStyle.source);
					if (field.isTemplateField()) {
						textStyle.source = null;
						replaceTableCellField(tableCell, field.key, i, newTableCellData);
					}

				}

			}

		}

		if (newTableCellData.newTableCells.size() > 0 && newTableCellData.newRowMin < Integer.MAX_VALUE && newTableCellData.newRowMax > Integer.MIN_VALUE) {

			int newRowCount = (newTableCellData.newRowMax - newTableCellData.newRowMin) + 1;

			for (TableCell tableCell : table.getTableCells()) {

				if (tableCell.getRow() >= newTableCellData.newRowMin) {
					tableCell.setRow(tableCell.getRow() + newRowCount);
				} else if (tableCell.getRow() + (tableCell.getRowSpan() - 1) >= newTableCellData.newRowMin) {
					tableCell.setRowSpan(tableCell.getRowSpan() + newRowCount);
				}

			}

			table.getTableCells().addAll(newTableCellData.newTableCells);

		}

	}

	private void replaceTableCellField(TableCell tableCell, String key, int styleIndex, NewTableCellData newTableCellData) {

		String replacement = null;
		List<String> replacements = fieldContext.getList(key);
		if (replacements != null) {

			if (replacements.size() == 0) {
				replacement = fieldContext.getDefaultValue();
			} else if (replacements.size() == 1) {
				replacement = replacements.get(0);
			} else {

				TableCell originalTableCell = new TableCell(tableCell);
				IText.replaceText(tableCell, styleIndex, replacements.get(0));

				int row;
				TableCell newTableCell;
				for (int i = 1; i < replacements.size(); i++) {

					row = originalTableCell.getRow() + i;

					newTableCell = new TableCell(originalTableCell);
					newTableCell.setRow(row);
					newTableCellData.newTableCells.add(newTableCell);

					IText.replaceText(newTableCell, styleIndex, replacements.get(i));

					if (row < newTableCellData.newRowMin) {
						newTableCellData.newRowMin = row;
					}

					if (row > newTableCellData.newRowMax) {
						newTableCellData.newRowMax = row;
					}

				}

			}

		} else {
			replacement = fieldContext.getString(key);
		}

		if (replacement != null) {
			IText.replaceText(tableCell, styleIndex, replacement);
		}

	}

	@Override
	public void parseIText(IText iText) {

		for (int i = 0; i < iText.getStyles().size(); i++) {

			TextStyle textStyle = iText.getStyles().get(i);
			if (textStyle.source != null) {

				Field field = new Field(textStyle.source);
				if (field.isTemplateField()) {

					String replacement = fieldContext.getString(field.key);
					if (replacement != null) {
						textStyle.source = null;
						IText.replaceText(iText, i, replacement);
					}

				}

			}

		}

	}

	@Override
	public void parseImage(Image image) {

		// TODO?

	}

	private boolean isFieldValueFalse(String source) {

		Field field = new Field(source);
		if (field.isTemplateField()) {

			Object value = fieldContext.getObject(field.key);
			if (value == null) {
				value = fieldContext.getString(field.key);
			}

			if (value == null) {
				return false;
			} else if (value instanceof Boolean) {
				return ((Boolean) value) == false;
			} else if (value instanceof Number) {
				return ((Number) value).intValue() == 0;
			} else if (value instanceof String) {
				return "false".equals(((String) value).toLowerCase());
			}

		}

		return false;

	}

	private static class NewTableCellData {

		private List<TableCell> newTableCells = new ArrayList<>();
		private int newRowMin = Integer.MAX_VALUE;
		private int newRowMax = Integer.MIN_VALUE;

		private NewTableCellData() {

		}

	}

}
