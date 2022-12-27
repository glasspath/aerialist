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
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonInclude(Include.NON_DEFAULT)
@SuppressWarnings("nls")
public class TableCell implements IText {

	@JacksonXmlProperty(isAttribute = false)
	private String text = "";

	@JacksonXmlProperty(isAttribute = true)
	private String alignment = Alignment.DEFAULT.stringValue;

	@JacksonXmlElementWrapper(localName = "styles")
	@JacksonXmlProperty(localName = "style")
	private List<TextStyle> styles = new ArrayList<>();

	@JacksonXmlProperty(isAttribute = true)
	private int row = 0;

	@JacksonXmlProperty(isAttribute = true)
	private int col = 0;

	@JacksonXmlProperty(isAttribute = true)
	private int rowSpan = 1;

	@JacksonXmlProperty(isAttribute = true)
	private int colSpan = 1;

	public TableCell() {

	}

	public TableCell(TableCell tableCell) {
		fromTableCell(tableCell);
	}

	public void fromTableCell(TableCell tableCell) {

		this.text = tableCell.text;
		this.alignment = tableCell.alignment;

		styles.clear();
		for (TextStyle style : tableCell.styles) {
			styles.add(new TextStyle(style));
		}

		this.row = tableCell.row;
		this.col = tableCell.col;
		this.rowSpan = tableCell.rowSpan;
		this.colSpan = tableCell.colSpan;

	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String getAlignment() {
		return alignment;
	}

	@Override
	public void setAlignment(String alignment) {
		this.alignment = alignment;
	}

	@Override
	public List<TextStyle> getStyles() {
		return styles;
	}

	@Override
	public void setStyles(List<TextStyle> styles) {
		this.styles = styles;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public int getRowSpan() {
		return rowSpan;
	}

	public void setRowSpan(int rowSpan) {
		this.rowSpan = rowSpan;
	}

	public int getColSpan() {
		return colSpan;
	}

	public void setColSpan(int colSpan) {
		this.colSpan = colSpan;
	}

}
