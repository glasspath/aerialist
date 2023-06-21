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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.editor.EditorPanel;
import org.glasspath.aerialist.swing.view.ISwingElementView;
import org.glasspath.common.swing.color.ColorChooserPanel.ColorEvent;

public class SetBackgroundColorAction extends AbstractAction {

	private final EditorPanel<? extends EditorPanel<?>> context;

	public SetBackgroundColorAction(EditorPanel<? extends EditorPanel<?>> context) {

		this.context = context;

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e instanceof ColorEvent) {

			Color color = ((ColorEvent) e).color;

			if (context.getSelection().size() == 1) {

				ISwingElementView<?> elementView = AerialistUtils.getElementView(context.getSelection().get(0));
				if (elementView != null) {

					Color oldColor = elementView.getBackgroundColor();

					elementView.setBackgroundColor(color);
					context.refresh((Component) elementView);

					context.undoableEditHappened(new SetBackgroundColorUndoable(context, elementView, color, oldColor));

				}

			}

		}

	}

}
