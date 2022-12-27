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
import org.glasspath.aerialist.YPolicy;
import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.icons.Icons;
import org.glasspath.aerialist.swing.view.ISwingElementView;
import org.glasspath.aerialist.swing.view.PageView;

public class SetYPolicyAction extends AbstractAction {

	private final DocumentEditorPanel context;
	private final YPolicy yPolicy;

	public SetYPolicyAction(DocumentEditorPanel context, YPolicy yPolicy) {
		this(context, yPolicy, false);
	}

	public SetYPolicyAction(DocumentEditorPanel context, YPolicy yPolicy, boolean toolbarButton) {

		this.context = context;
		this.yPolicy = yPolicy;

		putValue(Action.SELECTED_KEY, false);

		if (yPolicy == YPolicy.FIXED) {
			if (!toolbarButton) {
				putValue(Action.NAME, "Fixed");
			} else {
				putValue(Action.SMALL_ICON, Icons.arrowVerticalLock);
			}
			putValue(Action.SHORT_DESCRIPTION, "Vertical positioning, fixed");
		} else if (yPolicy == YPolicy.DEFAULT) {
			if (!toolbarButton) {
				putValue(Action.NAME, "Automatic");
			}
			putValue(Action.SHORT_DESCRIPTION, "Vertical positioning, automatic");
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (context.getSelection().size() == 1) {

			ISwingElementView<?> elementView = AerialistUtils.getElementView(context.getSelection().get(0));
			if (elementView != null) {

				PageView pageView = AerialistUtils.getPageView((Component) elementView);
				if (pageView != null) {

					YPolicy oldYPolicy = elementView.getYPolicy();
					Rectangle oldBounds = ((Component) elementView).getBounds();

					YPolicy newYPolicy;
					if ((boolean) getValue(Action.SELECTED_KEY)) {
						newYPolicy = yPolicy;
					} else {
						if (yPolicy == YPolicy.DEFAULT) {
							newYPolicy = YPolicy.FIXED;
						} else {
							newYPolicy = YPolicy.DEFAULT;
						}
					}

					elementView.setYPolicy(newYPolicy);

					if (elementView instanceof JComponent) {
						((JComponent) elementView).invalidate();
					}

					pageView.updateVerticalAnchors();

					context.undoableEditHappened(new SetYPolicyUndoable(context, elementView, pageView, oldYPolicy, oldBounds, newYPolicy));
					context.refresh(pageView);

				}

			}

		}

	}

}
