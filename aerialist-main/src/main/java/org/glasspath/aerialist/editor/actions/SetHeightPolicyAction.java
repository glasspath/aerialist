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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;

import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.HeightPolicy;
import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.icons.Icons;
import org.glasspath.aerialist.swing.view.ISwingElementView;
import org.glasspath.aerialist.swing.view.PageView;

public class SetHeightPolicyAction extends AbstractAction {

	private final DocumentEditorPanel context;
	private final HeightPolicy heightPolicy;

	public SetHeightPolicyAction(DocumentEditorPanel context, HeightPolicy heightPolicy) {
		this(context, heightPolicy, false);
	}

	public SetHeightPolicyAction(DocumentEditorPanel context, HeightPolicy heightPolicy, boolean toolbarButton) {

		this.context = context;
		this.heightPolicy = heightPolicy;

		putValue(Action.SELECTED_KEY, false);

		if (heightPolicy == HeightPolicy.DEFAULT) {
			if (!toolbarButton) {
				putValue(Action.NAME, "Fixed");
			}
			putValue(Action.SHORT_DESCRIPTION, "Layout height, fixed");
		} else if (heightPolicy == HeightPolicy.AUTO) {
			if (!toolbarButton) {
				putValue(Action.NAME, "Automatic");
			} else {
				putValue(Action.SMALL_ICON, Icons.arrowExpandVertical);
			}
			putValue(Action.SHORT_DESCRIPTION, "Layout height, automatic");
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (context.getSelection().size() == 1) {

			ISwingElementView<?> elementView = AerialistUtils.getElementView(context.getSelection().get(0));
			if (elementView != null) {

				PageView pageView = AerialistUtils.getPageView((Component) elementView);
				if (pageView != null) {

					context.getPageContainer().setYPolicyEnabled(false);
					
					HeightPolicy oldHeightPolicy = elementView.getHeightPolicy();
					Rectangle oldBounds = ((Component) elementView).getBounds();

					HeightPolicy newHeightPolicy;
					if ((boolean) getValue(Action.SELECTED_KEY)) {
						newHeightPolicy = heightPolicy;
					} else {
						if (heightPolicy == HeightPolicy.DEFAULT) {
							newHeightPolicy = HeightPolicy.AUTO;
						} else {
							newHeightPolicy = HeightPolicy.DEFAULT;
						}
					}

					elementView.setHeightPolicy(newHeightPolicy);

					if (elementView instanceof JComponent) {
						((JComponent) elementView).invalidate();
					}

					pageView.updateVerticalAnchors();

					context.undoableEditHappened(new SetHeightPolicyUndoable(context, elementView, pageView, oldHeightPolicy, oldBounds, newHeightPolicy));
					context.refresh(pageView);

				}

			}

		}

	}

}
