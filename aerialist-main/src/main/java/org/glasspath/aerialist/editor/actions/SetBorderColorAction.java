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
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;

import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.Border;
import org.glasspath.aerialist.editor.EditorPanel;
import org.glasspath.aerialist.swing.view.ISwingElementView;
import org.glasspath.common.swing.color.ColorChooserPanel.ColorEvent;
import org.glasspath.common.swing.color.ColorUtils;

public class SetBorderColorAction extends AbstractAction {

	private final EditorPanel<? extends EditorPanel<?>> context;

	public SetBorderColorAction(EditorPanel<? extends EditorPanel<?>> context) {

		this.context = context;

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e instanceof ColorEvent) {

			Color color = ((ColorEvent) e).color;

			if (context.getSelection().size() == 1) {

				ISwingElementView<?> elementView = AerialistUtils.getElementView(context.getSelection().get(0));
				if (elementView != null) {

					List<Border> oldBorders = new ArrayList<>();
					for (Border border : elementView.getBorders()) {
						oldBorders.add(new Border(border));
					}

					applyBorderColor(elementView, color);
					((Component) elementView).repaint();

					List<Border> newBorders = new ArrayList<>();
					for (Border border : elementView.getBorders()) {
						newBorders.add(border);
					}

					context.undoableEditHappened(new SetBorderColorUndoable(elementView, newBorders, oldBorders));

				}

			}

		}

	}

	public static void applyBorderColor(ISwingElementView<?> elementView, Color color) {
		if (color == null) {
			elementView.getBorders().clear();
		} else {
			for (Border border : elementView.getBorders()) {
				border.color = ColorUtils.toHex(color);
			}
		}
	}

}
