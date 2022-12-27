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

import org.glasspath.aerialist.FitPolicy;
import org.glasspath.aerialist.editor.EditorPanel;
import org.glasspath.aerialist.swing.view.ImageView;

public class SetFitPolicyAction extends AbstractAction {

	private final EditorPanel<? extends EditorPanel<?>> context;
	private final ImageView imageView;
	private final FitPolicy fitPolicy;

	public SetFitPolicyAction(EditorPanel<? extends EditorPanel<?>> context, ImageView imageView, FitPolicy fitPolicy) {
		this(context, imageView, fitPolicy, false);
	}

	public SetFitPolicyAction(EditorPanel<? extends EditorPanel<?>> context, ImageView imageView, FitPolicy fitPolicy, boolean toolbarButton) {

		this.context = context;
		this.imageView = imageView;
		this.fitPolicy = fitPolicy;

		putValue(Action.SELECTED_KEY, false);

		if (fitPolicy == FitPolicy.DEFAULT) {
			if (!toolbarButton) {
				putValue(Action.NAME, "Default");
			}
			putValue(Action.SHORT_DESCRIPTION, "Layout height, fixed");
		} else if (fitPolicy == FitPolicy.WIDTH) {
			if (!toolbarButton) {
				putValue(Action.NAME, "Fit width");
			} else {
				// putValue(Action.SMALL_ICON, Icons.arrowExpandVertical);
			}
			putValue(Action.SHORT_DESCRIPTION, "Fit width");
		} else if (fitPolicy == FitPolicy.HEIGHT) {
			if (!toolbarButton) {
				putValue(Action.NAME, "Fit height");
			} else {
				// putValue(Action.SMALL_ICON, Icons.arrowExpandVertical);
			}
			putValue(Action.SHORT_DESCRIPTION, "Fit height");
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		FitPolicy oldFitPolicy = imageView.getFitPolicy();

		imageView.setFitPolicy(fitPolicy);
		imageView.repaint();

		context.undoableEditHappened(new SetFitPolicyUndoable(context, imageView, oldFitPolicy, fitPolicy));

	}

}
