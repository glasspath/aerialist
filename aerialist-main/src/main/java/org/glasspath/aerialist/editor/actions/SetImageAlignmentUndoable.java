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

import java.awt.Component;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.glasspath.aerialist.Alignment;
import org.glasspath.aerialist.swing.view.IScalableView;

public class SetImageAlignmentUndoable implements UndoableEdit {

	private final IScalableView view;
	private final Alignment oldAlignment;
	private final Alignment newAlignment;

	public SetImageAlignmentUndoable(IScalableView view, Alignment oldAlignment, Alignment newAlignment) {
		this.view = view;
		this.oldAlignment = oldAlignment;
		this.newAlignment = newAlignment;
	}

	@Override
	public String getPresentationName() {
		return "Change image alignment";
	}

	@Override
	public String getRedoPresentationName() {
		return "Redo change image alignment";
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo change image alignment";
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
		view.setAlignment(newAlignment);
		((Component) view).repaint();
	}

	@Override
	public boolean replaceEdit(UndoableEdit anEdit) {
		return false;
	}

	@Override
	public void undo() throws CannotUndoException {
		view.setAlignment(oldAlignment);
		((Component) view).repaint();
	}

}
