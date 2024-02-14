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

import org.glasspath.aerialist.editor.AbstractEditorPanel;
import org.glasspath.aerialist.editor.DefaultEditorUndoable;
import org.glasspath.aerialist.swing.view.PageView;

public class SetPageSizeUndoable extends DefaultEditorUndoable {

	private final PageView pageView;
	private final int width;
	private final int height;
	private final int oldWidth;
	private final int oldHeight;

	public SetPageSizeUndoable(AbstractEditorPanel context, PageView pageView, int width, int height, int oldWidth, int oldHeight) {
		super(context);
		this.pageView = pageView;
		this.width = width;
		this.height = height;
		this.oldWidth = oldWidth;
		this.oldHeight = oldHeight;
	}

	@Override
	public String getPresentationName() {
		return "Change page size";
	}

	@Override
	public String getRedoPresentationName() {
		return "Redo change page size";
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo change page size";
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
		pageView.setPageSize(width, height);
		context.refresh(null);
	}

	@Override
	public boolean replaceEdit(UndoableEdit anEdit) {
		return false;
	}

	@Override
	public void undo() throws CannotUndoException {
		pageView.setPageSize(oldWidth, oldHeight);
		context.refresh(null);
	}

}
