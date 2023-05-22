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

import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.Radius;
import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.swing.view.ISwingElementView;

public class SetRadiusAction extends AbstractAction {

	private final DocumentEditorPanel context;
	private final Radius radius;

	public SetRadiusAction(DocumentEditorPanel context) {
		this(context, 0);
	}

	public SetRadiusAction(DocumentEditorPanel context, int radius) {

		this.context = context;
		this.radius = new Radius(radius);

		putValue(Action.NAME, radius + "px");
		putValue(Action.SHORT_DESCRIPTION, "Radius");
		putValue(Action.SELECTED_KEY, this.radius.equals(getRadius(getElementView())));

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		actionPerformed(radius);
	}

	public void actionPerformed(Radius radius) {

		ISwingElementView<?> elementView = getElementView();
		if (elementView != null) {

			Radius oldRadius = getRadius(elementView);
			if (oldRadius != null) {

				applyRadius(elementView, radius);
				context.refresh(null);

				context.undoableEditHappened(new SetRadiusUndoable(context, elementView, radius, oldRadius));

			}

		}

	}

	private ISwingElementView<?> getElementView() {
		if (context.getSelection().size() == 1) {
			return AerialistUtils.getElementView(context.getSelection().get(0));
		} else {
			return null;
		}
	}

	public static boolean isRadiusSupported(ISwingElementView<?> elementView) {
		return true;
	}

	public static Radius getRadius(ISwingElementView<?> elementView) {
		return new Radius(elementView.getRadius());
	}

	public static void applyRadius(ISwingElementView<?> elementView, Radius radius) {
		elementView.setRadius(radius);
	}

}
