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

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.editor.ElementData;
import org.glasspath.aerialist.swing.view.GroupView;
import org.glasspath.aerialist.swing.view.PageView;

public class GroupElementsUndoable implements UndoableEdit {

	private final DocumentEditorPanel context;
	private final PageView pageView;
	private final GroupView groupView;
	private final List<ElementData> oldElementsData;
	private final List<ElementData> newElementsData;

	public GroupElementsUndoable(DocumentEditorPanel context, PageView pageView, GroupView groupView, List<ElementData> oldElementsData, List<ElementData> newElementsData) {
		this.context = context;
		this.pageView = pageView;
		this.groupView = groupView;
		this.oldElementsData = oldElementsData;
		this.newElementsData = newElementsData;
	}

	@Override
	public String getPresentationName() {
		return "Group";
	}

	@Override
	public String getRedoPresentationName() {
		return "Redo group";
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo group";
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

		for (ElementData elementData : oldElementsData) {
			pageView.remove(elementData.element);
		}

		for (ElementData elementData : newElementsData) {
			elementData.element.setBounds(elementData.bounds);
			groupView.add(elementData.element, elementData.index);
		}
		pageView.add(groupView);

		groupView.updateVerticalAnchors();
		pageView.updateVerticalAnchors();

		context.getSelection().clear();

		context.refresh(pageView);

	}

	@Override
	public boolean replaceEdit(UndoableEdit anEdit) {
		return false;
	}

	@Override
	public void undo() throws CannotUndoException {

		for (ElementData elementData : newElementsData) {
			groupView.remove(elementData.element);
		}
		pageView.remove(groupView);

		for (ElementData elementData : oldElementsData) {
			elementData.element.setBounds(elementData.bounds);
			pageView.add(elementData.element, elementData.index);
		}

		groupView.updateVerticalAnchors();
		pageView.updateVerticalAnchors();

		context.getSelection().clear();

		context.refresh(pageView);

	}

}
