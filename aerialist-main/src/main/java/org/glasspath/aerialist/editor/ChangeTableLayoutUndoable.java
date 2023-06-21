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
package org.glasspath.aerialist.editor;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.glasspath.aerialist.ColStyle;
import org.glasspath.aerialist.TableCell;
import org.glasspath.aerialist.swing.view.TableCellView;
import org.glasspath.aerialist.swing.view.TableView;

public class ChangeTableLayoutUndoable extends EditorUndoable {

	private final TableView tableView;
	private final TableViewData oldTableViewData;
	private final TableViewData newTableViewData;

	public ChangeTableLayoutUndoable(EditorPanel<? extends EditorPanel<?>> context, TableView tableView, TableViewData oldTableViewData, TableViewData newTableViewData) {
		super(context);
		this.tableView = tableView;
		this.oldTableViewData = oldTableViewData;
		this.newTableViewData = newTableViewData;
	}

	@Override
	public String getPresentationName() {
		return "Change table layout";
	}

	@Override
	public String getRedoPresentationName() {
		return "Redo change table layout";
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo change table layout";
	}

	@Override
	public boolean addEdit(UndoableEdit anEdit) {
		return false;
	}

	@Override
	public boolean canRedo() {
		return true;
	}

	@Override
	public boolean canUndo() {
		return true;
	}

	@Override
	public void die() {

	}

	@Override
	public boolean isSignificant() {
		return true;
	}

	@Override
	public void redo() throws CannotRedoException {

		if (newTableViewData.cellViewData != null) {

			tableView.getTableCellViews().clear();
			for (TableCellViewData tableCellData : newTableViewData.cellViewData) {
				tableCellData.view.init(tableCellData.data, tableView.getCellPadding());
				tableView.getTableCellViews().add(tableCellData.view);
			}

		}

		tableView.setColStyles(newTableViewData.colStyles);
		tableView.layoutTableCells();
		context.refresh(tableView);

	}

	@Override
	public boolean replaceEdit(UndoableEdit anEdit) {
		return false;
	}

	@Override
	public void undo() throws CannotUndoException {

		if (oldTableViewData.cellViewData != null) {

			tableView.getTableCellViews().clear();
			for (TableCellViewData tableCellData : oldTableViewData.cellViewData) {
				tableCellData.view.init(tableCellData.data, tableView.getCellPadding());
				tableView.getTableCellViews().add(tableCellData.view);
			}

		}

		tableView.setColStyles(oldTableViewData.colStyles);
		tableView.layoutTableCells();
		context.refresh(tableView);

	}

	public static class TableViewData {

		public final List<ColStyle> colStyles;
		public final List<TableCellViewData> cellViewData;

		public TableViewData(List<ColStyle> colStyles, List<TableCellViewData> cellViewData) {
			this.colStyles = colStyles;
			this.cellViewData = cellViewData;
		}

	}

	public static class TableCellViewData {

		public final TableCellView view;
		public final TableCell data;

		public TableCellViewData(TableCellView view, TableCell data) {
			this.view = view;
			this.data = data;
		}

	}

}
