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

import java.awt.Color;
import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.glasspath.aerialist.RowStyle;
import org.glasspath.aerialist.editor.EditorPanel;
import org.glasspath.aerialist.editor.EditorUndoable;
import org.glasspath.aerialist.swing.view.TableView;

public class SetRowColorUndoable extends EditorUndoable {

	private final TableView tableView;
	private final int row;
	private final int repeat;
	private final Color color;
	private final List<RowStyle> oldRowStyles;

	public SetRowColorUndoable(EditorPanel<? extends EditorPanel<?>> context, TableView tableView, int row, int repeat, Color color, List<RowStyle> oldRowStyles) {
		super(context);
		this.tableView = tableView;
		this.row = row;
		this.repeat = repeat;
		this.color = color;
		this.oldRowStyles = oldRowStyles;
	}

	@Override
	public String getPresentationName() {
		return "Change row color";
	}

	@Override
	public String getRedoPresentationName() {
		return "Redo change row color";
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo change row color";
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
		SetRowColorAction.applyRowColor(tableView, row, repeat, color);
		context.refresh(tableView);
	}

	@Override
	public boolean replaceEdit(UndoableEdit anEdit) {
		return false;
	}

	@Override
	public void undo() throws CannotUndoException {
		tableView.applyRowStyles(oldRowStyles);
		context.refresh(tableView);
	}

}
