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

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.glasspath.aerialist.FitPolicy;
import org.glasspath.aerialist.editor.EditorPanel;
import org.glasspath.aerialist.swing.view.ImageView;

public class SetFitPolicyUndoable implements UndoableEdit {

	private final EditorPanel<? extends EditorPanel<?>> context;
	private final ImageView imageView;
	private final FitPolicy oldFitPolicy;
	private final FitPolicy newFitPolicy;

	public SetFitPolicyUndoable(EditorPanel<? extends EditorPanel<?>> context, ImageView imageView, FitPolicy oldFitPolicy, FitPolicy newFitPolicy) {
		this.context = context;
		this.imageView = imageView;
		this.oldFitPolicy = oldFitPolicy;
		this.newFitPolicy = newFitPolicy;
	}

	@Override
	public String getPresentationName() {
		return "Change image fit";
	}

	@Override
	public String getRedoPresentationName() {
		return "Redo change image fit";
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo change image fit";
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
		imageView.setFitPolicy(newFitPolicy);
		imageView.repaint();
	}

	@Override
	public boolean replaceEdit(UndoableEdit anEdit) {
		return false;
	}

	@Override
	public void undo() throws CannotUndoException {
		imageView.setFitPolicy(oldFitPolicy);
		imageView.repaint();
	}

}
