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

import java.awt.event.ActionEvent;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.Padding;
import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.swing.view.ISwingElementView;
import org.glasspath.aerialist.swing.view.TableView;
import org.glasspath.aerialist.swing.view.TextBoxView;

public class SetPaddingAction extends AbstractAction {

	private final DocumentEditorPanel context;
	private final Padding padding;

	public SetPaddingAction(DocumentEditorPanel context) {
		this(context, 0);
	}

	public SetPaddingAction(DocumentEditorPanel context, int padding) {

		this.context = context;
		this.padding = new Padding(padding);

		putValue(Action.NAME, padding + "px");
		putValue(Action.SHORT_DESCRIPTION, "Padding");

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		actionPerformed(padding);
	}

	public void actionPerformed(Padding padding) {

		if (context.getSelection().size() == 1) {

			ISwingElementView<?> elementView = AerialistUtils.getElementView(context.getSelection().get(0));
			if (elementView != null) {

				Padding oldPadding = getPadding(elementView);
				if (oldPadding != null) {

					applyPadding(elementView, padding);
					context.refresh(null);

					context.undoableEditHappened(new SetPaddingUndoable(context, elementView, padding, oldPadding, context.getPageContainer().isYPolicyEnabled()));

				}

			}

		}

	}

	public static boolean isPaddingSupported(ISwingElementView<?> elementView) {
		return elementView instanceof TextBoxView || elementView instanceof TableView;
	}

	public static Padding getPadding(ISwingElementView<?> elementView) {
		if (elementView instanceof TextBoxView) {
			return new Padding(((TextBoxView) elementView).getPadding());
		} else if (elementView instanceof TableView) {
			return new Padding(((TableView) elementView).getCellPadding());
		} else {
			return null;
		}
	}

	public static void applyPadding(ISwingElementView<?> elementView, Padding padding) {

		Consumer<Padding> consumer = null;

		if (elementView instanceof TextBoxView) {
			consumer = ((TextBoxView) elementView)::applyPadding;
		} else if (elementView instanceof TableView) {
			consumer = ((TableView) elementView)::applyCellPadding;
		}

		if (consumer != null) {
			consumer.accept(padding);
		}

	}

}
