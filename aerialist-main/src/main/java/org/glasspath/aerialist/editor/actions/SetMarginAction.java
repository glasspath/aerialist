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
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.glasspath.aerialist.Margin;
import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.swing.view.PageView;

public class SetMarginAction extends AbstractAction {

	private final DocumentEditorPanel context;
	private final Margin margin;

	public SetMarginAction(DocumentEditorPanel context, Margin margin, String description) {

		this.context = context;
		this.margin = margin;

		putValue(Action.NAME, description);
		putValue(Action.SHORT_DESCRIPTION, description);
		putValue(Action.SELECTED_KEY, this.margin.equals(getMargin(getView())));

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		actionPerformed();
	}

	public void actionPerformed() {

		Component view = getView();
		if (view != null) {

			Margin oldMargin = getMargin(view);
			if (oldMargin != null) {

				applyMargin(view, margin);
				context.refresh(null);

				context.undoableEditHappened(new SetMarginUndoable(context, view, margin, oldMargin, context.getPageContainer().isYPolicyEnabled()));

			}

		}

	}

	private Component getView() {
		if (context.getSelection().size() == 1) {
			return context.getSelection().get(0);
		} else {
			return null;
		}
	}
	
	public static boolean isMarginSupported(Component view) {
		return view instanceof PageView;
	}

	public static Margin getMargin(Component view) {
		if (view instanceof PageView) {
			return new Margin(((PageView) view).getMargin());
		} else {
			return null;
		}
	}

	public static void applyMargin(Component view, Margin margin) {

		Consumer<String> consumer = null;

		if (view instanceof PageView) {
			consumer = ((PageView) view)::setMargin;
		}

		if (consumer != null) {
			consumer.accept(margin != null ? margin.toString() : null);
		}

	}

}
