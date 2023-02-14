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
public class Table extends Element {

	@JacksonXmlProperty(isAttribute = true)
	private String cellPadding = Padding.DEFAULT;

	@JacksonXmlProperty(isAttribute = true)
	private int headerRows = 0;

	@JacksonXmlElementWrapper(localName = "colStyles")
	@JacksonXmlProperty(localName = "colStyle")
	private List<ColStyle> colStyles = new ArrayList<>();

	@JacksonXmlElementWrapper(localName = "rowStyles")
	@JacksonXmlProperty(localName = "rowStyle")
	private List<RowStyle> rowStyles = new ArrayList<>();

	@JacksonXmlElementWrapper(localName = "cells")
	@JacksonXmlProperty(localName = "cell")
	private List<TableCell> tableCells = new ArrayList<>();

	public Table() {

	}

	public Table(Table table) {
		fromTable(table);
	}

	public void fromTable(Table table) {

		fromElement(table);

		cellPadding = table.cellPadding;
		headerRows = table.headerRows;

		colStyles.clear();
		for (ColStyle colStyle : table.colStyles) {
			colStyles.add(new ColStyle(colStyle));
		}

		rowStyles.clear();
		for (RowStyle rowStyle : table.rowStyles) {
			rowStyles.add(new RowStyle(rowStyle));
		}

		tableCells.clear();
		for (TableCell tableCell : table.tableCells) {
			tableCells.add(new TableCell(tableCell));
		}

	}

	public String getCellPadding() {
		return cellPadding;
	}

	public void setCellPadding(String cellPadding) {
		this.cellPadding = cellPadding;
	}

	public int getHeaderRows() {
		return headerRows;
	}

	public void setHeaderRows(int headerRows) {
		this.headerRows = headerRows;
	}

	public List<ColStyle> getColStyles() {
		return colStyles;
	}

	public void setColStyles(List<ColStyle> colStyles) {
		this.colStyles = colStyles;
	}

	public List<RowStyle> getRowStyles() {
		return rowStyles;
	}

	public void setRowStyles(List<RowStyle> rowStyles) {
		this.rowStyles = rowStyles;
	}

	public List<TableCell> getTableCells() {
		return tableCells;
	}

	public void setTableCells(List<TableCell> tableCells) {
		this.tableCells = tableCells;
	}

}
