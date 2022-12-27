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

import org.glasspath.aerialist.Aerialist;
import org.glasspath.aerialist.ColStyle;
import org.glasspath.aerialist.editor.ChangeTableLayoutUndoable;
import org.glasspath.aerialist.editor.ChangeTableLayoutUndoable.TableCellViewData;
import org.glasspath.aerialist.editor.ChangeTableLayoutUndoable.TableViewData;
import org.glasspath.aerialist.editor.EditorPanel;
import org.glasspath.aerialist.swing.view.TableCellView;
import org.glasspath.aerialist.swing.view.TableView;

public class DeleteTableColumnAction extends AbstractAction {

	private final EditorPanel<? extends EditorPanel<?>> context;
	private final TableCellView tableCellView;

	public DeleteTableColumnAction(EditorPanel<? extends EditorPanel<?>> context, TableCellView tableCellView) {

		this.context = context;
		this.tableCellView = tableCellView;

		putValue(Action.NAME, "Delete column");
		putValue(Action.SHORT_DESCRIPTION, "Delete column");

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (tableCellView.getParent() instanceof TableView) {

			TableView tableView = (TableView) tableCellView.getParent();

			int col = tableCellView.getCol();

			List<TableCellViewData> oldTableCellData = new ArrayList<>();
			List<ColStyle> oldColStyles = tableView.getColStylesCopy();

			tableView.removeColStyle(col);

			List<TableCellView> removeCellViews = new ArrayList<>();
			for (TableCellView cellView : tableView.getTableCellViews()) {

				oldTableCellData.add(new TableCellViewData(cellView, cellView.toTableCell()));

				if (cellView.getCol() > col) {
					cellView.setCol(cellView.getCol() - 1);
				} else if (cellView.getCol() == col) {
					removeCellViews.add(cellView);
				} else if (cellView.getCol() + (cellView.getColSpan() - 1) >= col) {
					cellView.setColSpan(cellView.getColSpan() - 1);
				}

			}

			TableViewData oldTableViewData = new TableViewData(oldColStyles, oldTableCellData);

			tableView.getTableCellViews().removeAll(removeCellViews);

			List<TableCellViewData> newTableCellData = new ArrayList<>();
			for (TableCellView cellView : tableView.getTableCellViews()) {
				newTableCellData.add(new TableCellViewData(cellView, cellView.toTableCell()));
			}

			tableView.layoutTableCells();
			tableView.invalidate();
			tableView.revalidate();
			tableView.repaint();

			TableViewData newTableViewData = new TableViewData(tableView.updateColStyles(), newTableCellData);
			context.undoableEditHappened(new ChangeTableLayoutUndoable(tableView, oldTableViewData, newTableViewData));

		}

	}

}
