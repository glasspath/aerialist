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
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.glasspath.aerialist.HeightPolicy;
import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.editor.DocumentEditorUndoable;
import org.glasspath.aerialist.resources.AerialistResources;
import org.glasspath.aerialist.swing.view.ISwingElementView;
import org.glasspath.aerialist.swing.view.PageView;

public class SetHeightPolicyUndoable extends DocumentEditorUndoable {

	private final ISwingElementView<?> elementView;
	private final PageView pageView;
	private final HeightPolicy oldHeightPolicy;
	private final Rectangle oldBounds;
	private final HeightPolicy newHeightPolicy;

	public SetHeightPolicyUndoable(DocumentEditorPanel context, ISwingElementView<?> elementView, PageView pageView, HeightPolicy oldHeightPolicy, Rectangle oldBounds, HeightPolicy newHeightPolicy) {
		super(context);
		this.elementView = elementView;
		this.pageView = pageView;
		this.oldHeightPolicy = oldHeightPolicy;
		this.oldBounds = oldBounds;
		this.newHeightPolicy = newHeightPolicy;
	}

	@Override
	public String getPresentationName() {
		return AerialistResources.getString("ChangeVerticalLayout"); //$NON-NLS-1$
	}

	@Override
	public String getRedoPresentationName() {
		return AerialistResources.getString("RedoChangeVerticalLayout"); //$NON-NLS-1$
	}

	@Override
	public String getUndoPresentationName() {
		return AerialistResources.getString("UndoChangeVerticalLayout"); //$NON-NLS-1$
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

		context.getPageContainer().setYPolicyEnabled(false);

		elementView.setHeightPolicy(newHeightPolicy);

		if (elementView instanceof JComponent) {
			((JComponent) elementView).invalidate();
		}

		pageView.updateVerticalAnchors();

		context.refresh(pageView);

	}

	@Override
	public boolean replaceEdit(UndoableEdit anEdit) {
		return false;
	}

	@Override
	public void undo() throws CannotUndoException {

		context.getPageContainer().setYPolicyEnabled(false);

		elementView.setHeightPolicy(oldHeightPolicy);
		((Component) elementView).setBounds(oldBounds);

		if (elementView instanceof JComponent) {
			((JComponent) elementView).invalidate();
		}

		pageView.updateVerticalAnchors();

		context.refresh(pageView);

	}

	protected static class ElementData {

		protected final Component element;
		protected final int index;

		protected ElementData(Component element, int index) {
			this.element = element;
			this.index = index;
		}

	}

}
