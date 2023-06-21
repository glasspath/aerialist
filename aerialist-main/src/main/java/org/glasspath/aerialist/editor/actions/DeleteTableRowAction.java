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

import org.glasspath.aerialist.editor.ChangeTableLayoutUndoable;
import org.glasspath.aerialist.editor.ChangeTableLayoutUndoable.TableCellViewData;
import org.glasspath.aerialist.editor.ChangeTableLayoutUndoable.TableViewData;
import org.glasspath.aerialist.swing.view.TableCellView;
import org.glasspath.aerialist.swing.view.TableView;
import org.glasspath.aerialist.editor.EditorPanel;

public class DeleteTableRowAction extends AbstractAction {

	private final EditorPanel<? extends EditorPanel<?>> context;
	private final TableCellView tableCellView;

	public DeleteTableRowAction(EditorPanel<? extends EditorPanel<?>> context, TableCellView tableCellView) {

		this.context = context;
		this.tableCellView = tableCellView;

		putValue(Action.NAME, "Delete row");
		putValue(Action.SHORT_DESCRIPTION, "Delete row");

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (tableCellView.getParent() instanceof TableView) {

			TableView tableView = (TableView) tableCellView.getParent();

			int row = tableCellView.getRow();

			List<TableCellViewData> oldTableCellData = new ArrayList<>();
			List<TableCellView> removeCellViews = new ArrayList<>();
			for (TableCellView cellView : tableView.getTableCellViews()) {

				oldTableCellData.add(new TableCellViewData(cellView, cellView.toTableCell()));

				if (cellView.getRow() > row) {
					cellView.setRow(cellView.getRow() - 1);
				} else if (cellView.getRow() == row) {
					if (cellView.getRowSpan() > 1) {
						cellView.setRowSpan(cellView.getRowSpan() - 1);
					} else {
						removeCellViews.add(cellView);
					}
				} else if (cellView.getRow() + (cellView.getRowSpan() - 1) >= row) {
					cellView.setRowSpan(cellView.getRowSpan() - 1);
				}

			}

			TableViewData oldTableViewData = new TableViewData(tableView.getColStylesCopy(), oldTableCellData);

			tableView.getTableCellViews().removeAll(removeCellViews);

			List<TableCellViewData> newTableCellData = new ArrayList<>();
			for (TableCellView cellView : tableView.getTableCellViews()) {
				newTableCellData.add(new TableCellViewData(cellView, cellView.toTableCell()));
			}

			tableView.layoutTableCells();
			context.refresh(tableView);

			TableViewData newTableViewData = new TableViewData(tableView.getColStylesCopy(), newTableCellData);
			context.undoableEditHappened(new ChangeTableLayoutUndoable(context, tableView, oldTableViewData, newTableViewData));

		}

	}

}
