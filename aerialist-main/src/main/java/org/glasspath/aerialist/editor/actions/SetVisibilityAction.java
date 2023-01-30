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

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.glasspath.aerialist.IVisible;
import org.glasspath.aerialist.editor.EditorPanel;

public class SetVisibilityAction extends AbstractAction {

	private final EditorPanel<? extends EditorPanel<?>> context;
	private final IVisible view;
	private final String key;

	public SetVisibilityAction(EditorPanel<? extends EditorPanel<?>> context, IVisible view, String key, String description) {

		this.context = context;
		this.view = view;
		this.key = key;

		putValue(Action.NAME, description);
		putValue(Action.SHORT_DESCRIPTION, description);

		if (view.getVisible() == null && key == null) {
			putValue(Action.SELECTED_KEY, true);
		} else if (key != null && key.equals(view.getVisible())) {
			putValue(Action.SELECTED_KEY, true);
		} else {
			putValue(Action.SELECTED_KEY, false);
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (view != null) {

			String oldKey = view.getVisible();

			view.setVisible(key);

			context.undoableEditHappened(new SetVisibilityUndoable(view, key, oldKey));

		}

	}

}
