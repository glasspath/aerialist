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

import java.awt.Component;
import java.awt.Rectangle;
import java.util.Map;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.glasspath.aerialist.resources.AerialistResources;
import org.glasspath.aerialist.swing.view.ElementUndoable;
import org.glasspath.aerialist.swing.view.ISwingViewContext;
import org.glasspath.aerialist.swing.view.TextView;
import org.glasspath.aerialist.swing.view.TextView.TextData;

public class TextViewUndoableEdit extends ElementUndoable {

	private final ISwingViewContext viewContext;
	private final TextView textView;
	private final TextData oldTextData;
	private final TextData newTextData;
	private final boolean yPolicyEnabled;

	public TextViewUndoableEdit(ISwingViewContext viewContext, TextView textView, TextData oldTextData, TextData newTextData, Map<Component, Rectangle> anchoredElementBounds) {
		super(anchoredElementBounds);
		this.viewContext = viewContext;
		this.textView = textView;
		this.oldTextData = oldTextData;
		this.newTextData = newTextData;
		this.yPolicyEnabled = viewContext.isYPolicyEnabled();
	}

	@Override
	public String getPresentationName() {
		return AerialistResources.getString("EditText"); //$NON-NLS-1$
	}

	@Override
	public String getUndoPresentationName() {
		return AerialistResources.getString("UndoEditText"); //$NON-NLS-1$
	}

	@Override
	public String getRedoPresentationName() {
		return AerialistResources.getString("RedoEditText"); //$NON-NLS-1$
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
		viewContext.setYPolicyEnabled(yPolicyEnabled);
		textView.init(newTextData);
		viewContext.refresh(textView, null);
	}

	@Override
	public boolean replaceEdit(UndoableEdit anEdit) {
		return false;
	}

	@Override
	public void undo() throws CannotUndoException {
		viewContext.setYPolicyEnabled(yPolicyEnabled);
		textView.init(oldTextData);
		viewContext.refresh(textView, anchoredElementBounds);
	}

}
