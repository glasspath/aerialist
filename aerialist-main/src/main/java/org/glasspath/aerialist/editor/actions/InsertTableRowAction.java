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
package org.glasspath.aerialist.editor.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.glasspath.aerialist.TableCell;
import org.glasspath.aerialist.editor.ChangeTableLayoutUndoable;
import org.glasspath.aerialist.editor.ChangeTableLayoutUndoable.TableCellViewData;
import org.glasspath.aerialist.editor.ChangeTableLayoutUndoable.TableViewData;
import org.glasspath.aerialist.editor.EditorPanel;
import org.glasspath.aerialist.swing.view.TableCellView;
import org.glasspath.aerialist.swing.view.TableView;

public class InsertTableRowAction extends AbstractAction {

	private final EditorPanel<? extends EditorPanel<?>> context;
	private final TableView tableView;
	private final int row;

	public InsertTableRowAction(EditorPanel<? extends EditorPanel<?>> context, TableView tableView, int row, String description) {

		this.context = context;
		this.tableView = tableView;
		this.row = row;

		putValue(Action.NAME, description);
		putValue(Action.SHORT_DESCRIPTION, description);

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		List<Integer> skipColumns = new ArrayList<>();

		List<TableCellViewData> oldTableCellData = new ArrayList<>();
		for (TableCellView cellView : tableView.getTableCellViews()) {

			oldTableCellData.add(new TableCellViewData(cellView, cellView.toTableCell()));

			if (cellView.getRow() >= row) {
				cellView.setRow(cellView.getRow() + 1);
			} else if (cellView.getRow() + (cellView.getRowSpan() - 1) >= row) {
				skipColumns.add(cellView.getCol());
				cellView.setRowSpan(cellView.getRowSpan() + 1);
			}

		}

		TableViewData oldTableViewData = new TableViewData(tableView.getColStylesCopy(), oldTableCellData);

		TableCell cell;
		TableCellView tableCellView;
		for (int col = 0; col < tableView.getColumnCount(); col++) {

			if (!skipColumns.contains(col + 1)) {

				cell = new TableCell();
				cell.setRow(row);
				cell.setCol(col + 1);

				tableCellView = new TableCellView(tableView.getViewContext());
				tableCellView.init(cell, tableView.getCellPadding());
				tableView.getTableCellViews().add(tableCellView);

			}

		}

		List<TableCellViewData> newTableCellData = new ArrayList<>();
		for (TableCellView cellView : tableView.getTableCellViews()) {
			newTableCellData.add(new TableCellViewData(cellView, cellView.toTableCell()));
		}

		tableView.layoutTableCells();
		tableView.invalidate();
		tableView.revalidate();
		tableView.repaint();

		TableViewData newTableViewData = new TableViewData(tableView.getColStylesCopy(), newTableCellData);
		context.undoableEditHappened(new ChangeTableLayoutUndoable(tableView, oldTableViewData, newTableViewData));

	}

}
