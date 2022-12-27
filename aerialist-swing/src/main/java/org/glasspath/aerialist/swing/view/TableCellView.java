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
package org.glasspath.aerialist.swing.view;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;

import org.glasspath.aerialist.Padding;
import org.glasspath.aerialist.TableCell;

public class TableCellView extends TextView {

	private int row = 0;
	private int col = 0;
	private int rowSpan = 1;
	private int colSpan = 1;

	public TableCellView(ISwingViewContext viewContext) {
		super(viewContext);

		addFocusListener(new FocusAdapter() {

			@Override
			public void focusGained(FocusEvent e) {
				viewContext.focusGained(TableCellView.this);
			}
		});

	}

	public void init(TableCell tableCell) {
		init(tableCell, null);
	}
	
	public void init(TableCell tableCell, Padding cellPadding) {
		super.init(tableCell);

		row = tableCell.getRow();
		col = tableCell.getCol();
		rowSpan = tableCell.getRowSpan();
		colSpan = tableCell.getColSpan();

		applyCellPadding(cellPadding);

	}

	public void applyCellPadding(Padding cellPadding) {

		// TODO: For now we only handle cellPadding defined in the Table
		if (cellPadding != null) {
			setBorder(BorderFactory.createEmptyBorder(cellPadding.top, cellPadding.left, cellPadding.bottom, cellPadding.right));
		}

	}

	public TableCell toTableCell() {

		TableCell tableCell = new TableCell();
		toText(tableCell);
		tableCell.setRow(row);
		tableCell.setCol(col);
		tableCell.setRowSpan(rowSpan);
		tableCell.setColSpan(colSpan);

		return tableCell;

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
