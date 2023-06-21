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

import org.glasspath.aerialist.Margin;
import org.glasspath.aerialist.editor.DocumentEditorPanel;

public class SetMarginUndoable implements UndoableEdit {

	private final DocumentEditorPanel context;
	private final Component view;
	private final Margin margin;
	private final Margin oldMargin;
	private final boolean yPolicyEnabled;

	public SetMarginUndoable(DocumentEditorPanel context, Component view, Margin margin, Margin oldMargin, boolean yPolicyEnabled) {
		this.context = context;
		this.view = view;
		this.margin = margin;
		this.oldMargin = oldMargin;
		this.yPolicyEnabled = yPolicyEnabled;
	}

	@Override
	public String getPresentationName() {
		return "Change margin";
	}

	@Override
	public String getRedoPresentationName() {
		return "Redo change margin";
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo change margin";
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
		context.getPageContainer().setYPolicyEnabled(yPolicyEnabled);
		SetMarginAction.applyMargin(view, margin);
		context.refresh(null);
	}

	@Override
	public boolean replaceEdit(UndoableEdit anEdit) {
		return false;
	}

	@Override
	public void undo() throws CannotUndoException {
		context.getPageContainer().setYPolicyEnabled(yPolicyEnabled);
		SetMarginAction.applyMargin(view, oldMargin);
		context.refresh(null);
	}

}
