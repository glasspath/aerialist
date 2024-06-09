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

import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.Padding;
import org.glasspath.aerialist.editor.AbstractEditorPanel;
import org.glasspath.aerialist.editor.DefaultEditorUndoable;
import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.resources.AerialistResources;
import org.glasspath.aerialist.swing.view.ISwingElementView;

public class SetPaddingUndoable extends DefaultEditorUndoable {

	private final ISwingElementView<?> elementView;
	private final Padding padding;
	private final Padding oldPadding;
	private final boolean yPolicyEnabled;

	public SetPaddingUndoable(AbstractEditorPanel context, ISwingElementView<?> elementView, Padding padding, Padding oldPadding) {
		super(context);

		this.elementView = elementView;
		this.padding = padding;
		this.oldPadding = oldPadding;
		this.yPolicyEnabled = context instanceof DocumentEditorPanel ? ((DocumentEditorPanel) context).getPageContainer().isYPolicyEnabled() : false;

	}

	@Override
	public String getPresentationName() {
		return AerialistResources.getString("ChangePadding"); //$NON-NLS-1$
	}

	@Override
	public String getRedoPresentationName() {
		return AerialistResources.getString("RedoChangePadding"); //$NON-NLS-1$
	}

	@Override
	public String getUndoPresentationName() {
		return AerialistResources.getString("UndoChangePadding"); //$NON-NLS-1$
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

		if (context instanceof DocumentEditorPanel) {
			((DocumentEditorPanel) context).getPageContainer().setYPolicyEnabled(yPolicyEnabled);
		}

		SetPaddingAction.applyPadding(elementView, padding);
		context.refresh(AerialistUtils.getPageView(elementView));

	}

	@Override
	public boolean replaceEdit(UndoableEdit anEdit) {
		return false;
	}

	@Override
	public void undo() throws CannotUndoException {

		if (context instanceof DocumentEditorPanel) {
			((DocumentEditorPanel) context).getPageContainer().setYPolicyEnabled(yPolicyEnabled);
		}

		SetPaddingAction.applyPadding(elementView, oldPadding);
		context.refresh(AerialistUtils.getPageView(elementView));

	}

}
