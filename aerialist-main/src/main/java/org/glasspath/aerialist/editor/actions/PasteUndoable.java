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
import java.awt.Container;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.editor.DocumentEditorUndoable;
import org.glasspath.aerialist.swing.view.LayeredPageView;
import org.glasspath.aerialist.swing.view.PageContainer;

public class PasteUndoable extends DocumentEditorUndoable {

	private final List<ComponentInfo> pastedComponents;

	public PasteUndoable(DocumentEditorPanel context, List<ComponentInfo> pastedComponents) {
		super(context);
		this.pastedComponents = pastedComponents;
	}

	@Override
	public String getPresentationName() {
		return "Paste";
	}

	@Override
	public String getRedoPresentationName() {
		return "Redo paste";
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo paste";
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
		pastedComponents.clear();
	}

	@Override
	public boolean isSignificant() {
		return true;
	}

	@Override
	public void redo() throws CannotRedoException {

		if (context.getPageContainer().isEditingHeader()) {
			context.getPageContainer().stopEditingHeaderView();
		} else if (context.getPageContainer().isEditingFooter()) {
			context.getPageContainer().stopEditingFooterView();
		}

		// TODO? Make sure only one parent is used?
		Container parent = null;

		for (ComponentInfo pastedComponent : pastedComponents) {

			parent = pastedComponent.parent;

			if (pastedComponent.component instanceof LayeredPageView && parent instanceof PageContainer) {
				((PageContainer) parent).insertPageView((LayeredPageView) pastedComponent.component, pastedComponent.index);
			} else {
				parent.add(pastedComponent.component, pastedComponent.index);
			}

		}

		Container refreshComponent = parent;

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				PasteAction.selectPastedComponents(context, pastedComponents);
				context.refresh(refreshComponent, null, true, true);
			}
		});

	}

	@Override
	public boolean replaceEdit(UndoableEdit anEdit) {
		return false;
	}

	@Override
	public void undo() throws CannotUndoException {

		if (context.getPageContainer().isEditingHeader()) {
			context.getPageContainer().stopEditingHeaderView();
		} else if (context.getPageContainer().isEditingFooter()) {
			context.getPageContainer().stopEditingFooterView();
		}

		// TODO? Make sure only one parent is used?
		Container parent = null;

		for (ComponentInfo pastedComponent : pastedComponents) {

			parent = pastedComponent.parent;

			if (pastedComponent.component instanceof LayeredPageView && parent instanceof PageContainer) {
				((PageContainer) parent).removePageView((LayeredPageView) pastedComponent.component);
			} else {
				parent.remove(pastedComponent.component);
			}

		}

		context.getSelection().deselectAll();
		context.refresh(parent, null, true, true);

	}

	public static class ComponentInfo {

		public final Component component;
		public final Container parent;
		public final int index;

		public ComponentInfo(Component component, Container parent, int index) {
			this.component = component;
			this.parent = parent;
			this.index = index;
		}

	}

}
