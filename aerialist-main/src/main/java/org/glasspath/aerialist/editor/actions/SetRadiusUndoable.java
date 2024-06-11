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

import org.glasspath.aerialist.Radius;
import org.glasspath.aerialist.editor.AbstractEditorPanel;
import org.glasspath.aerialist.editor.DefaultEditorUndoable;
import org.glasspath.aerialist.resources.AerialistResources;
import org.glasspath.aerialist.swing.view.ISwingElementView;

public class SetRadiusUndoable extends DefaultEditorUndoable {

	private final ISwingElementView<?> elementView;
	private final Radius radius;
	private final Radius oldRadius;

	public SetRadiusUndoable(AbstractEditorPanel context, ISwingElementView<?> elementView, Radius radius, Radius oldRadius) {
		super(context);
		this.elementView = elementView;
		this.radius = radius;
		this.oldRadius = oldRadius;
	}

	@Override
	public String getPresentationName() {
		return AerialistResources.getString("ChangeCornerRadius"); //$NON-NLS-1$
	}

	@Override
	public String getRedoPresentationName() {
		return AerialistResources.getString("RedoChangeCornerRadius"); //$NON-NLS-1$
	}

	@Override
	public String getUndoPresentationName() {
		return AerialistResources.getString("UndoChangeCornerRadius"); //$NON-NLS-1$
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
		SetRadiusAction.applyRadius(elementView, radius);
		context.refresh((Component) elementView);
	}

	@Override
	public boolean replaceEdit(UndoableEdit anEdit) {
		return false;
	}

	@Override
	public void undo() throws CannotUndoException {
		SetRadiusAction.applyRadius(elementView, oldRadius);
		context.refresh((Component) elementView);
	}

}
