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
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.editor.AbstractEditorPanel;
import org.glasspath.aerialist.editor.ElementData;
import org.glasspath.aerialist.icons.Icons;
import org.glasspath.aerialist.resources.Resources;
import org.glasspath.aerialist.swing.view.PageView;

public class AlignElementsAction extends AbstractAction {

	public static final int HORIZONTAL_LEFT = 0;
	public static final int HORIZONTAL_CENTER = 1;
	public static final int HORIZONTAL_RIGHT = 2;
	public static final int VERTICAL_TOP = 3;
	public static final int VERTICAL_CENTER = 4;
	public static final int VERTICAL_BOTTOM = 5;

	private final AbstractEditorPanel context;
	private final int alignAction;

	public AlignElementsAction(AbstractEditorPanel context, int alignAction) {

		this.context = context;
		this.alignAction = alignAction;

		if (alignAction == HORIZONTAL_LEFT) {
			putValue(Action.NAME, Resources.getString("AlignHorizontalLeft")); //$NON-NLS-1$
			putValue(Action.SHORT_DESCRIPTION, Resources.getString("AlignHorizontalLeft")); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, Icons.alignHorizontalLeft);
		} else if (alignAction == HORIZONTAL_CENTER) {
			putValue(Action.NAME, Resources.getString("AlignHorizontalCenter")); //$NON-NLS-1$
			putValue(Action.SHORT_DESCRIPTION, Resources.getString("AlignHorizontalCenter")); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, Icons.alignHorizontalCenter);
		} else if (alignAction == HORIZONTAL_RIGHT) {
			putValue(Action.NAME, Resources.getString("AlignHorizontalRight")); //$NON-NLS-1$
			putValue(Action.SHORT_DESCRIPTION, Resources.getString("AlignHorizontalRight")); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, Icons.alignHorizontalRight);
		} else if (alignAction == VERTICAL_TOP) {
			putValue(Action.NAME, Resources.getString("AlignVerticalTop")); //$NON-NLS-1$
			putValue(Action.SHORT_DESCRIPTION, Resources.getString("AlignVerticalTop")); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, Icons.alignVerticalTop);
		} else if (alignAction == VERTICAL_CENTER) {
			putValue(Action.NAME, Resources.getString("AlignVerticalCenter")); //$NON-NLS-1$
			putValue(Action.SHORT_DESCRIPTION, Resources.getString("AlignVerticalCenter")); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, Icons.alignVerticalCenter);
		} else if (alignAction == VERTICAL_BOTTOM) {
			putValue(Action.NAME, Resources.getString("AlignVerticalBottom")); //$NON-NLS-1$
			putValue(Action.SHORT_DESCRIPTION, Resources.getString("AlignVerticalBottom")); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, Icons.alignVerticalBottom);
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		PageView pageView = null;
		List<ElementData> elementsData = new ArrayList<>();

		Component elementView;
		for (Component component : context.getSelection()) {

			elementView = AerialistUtils.getElementViewAsComponent(component);
			if (elementView != null) {

				PageView componentPageView = AerialistUtils.getPageView(elementView);
				if (componentPageView != null) {

					if (pageView == null) {
						pageView = componentPageView;
					}

					if (pageView == componentPageView) {
						elementsData.add(new ElementData(elementView, -1));
					}

				}

			}

		}

		if (pageView != null && elementsData.size() > 0) {

			if (alignElements(pageView, elementsData, alignAction)) {
				context.undoableEditHappened(new AlignElementsUndoable(context, pageView, elementsData, alignAction));
			}

			ActionUtils.setSelection(context, pageView, elementsData);

		}

	}

	public static boolean alignElements(PageView pageView, List<ElementData> elementsData, int alignAction) {

		boolean aligned = false;

		if (elementsData.size() > 1) {

			Rectangle referenceBounds = elementsData.get(0).bounds;

			ElementData elementData;
			Rectangle bounds;
			for (int i = 1; i < elementsData.size(); i++) {

				elementData = elementsData.get(i);
				bounds = new Rectangle(elementData.bounds);

				int x = bounds.x;
				int y = bounds.y;

				if (alignAction == HORIZONTAL_LEFT) {
					x = referenceBounds.x;
				} else if (alignAction == HORIZONTAL_CENTER) {
					x = (referenceBounds.x + (referenceBounds.width / 2)) - (bounds.width / 2);
				} else if (alignAction == HORIZONTAL_RIGHT) {
					x = (referenceBounds.x + referenceBounds.width) - bounds.width;
				} else if (alignAction == VERTICAL_TOP) {
					y = referenceBounds.y;
				} else if (alignAction == VERTICAL_CENTER) {
					y = (referenceBounds.y + (referenceBounds.height / 2)) - (bounds.width / 2);
				} else if (alignAction == VERTICAL_BOTTOM) {
					y = (referenceBounds.y + referenceBounds.height) - bounds.height;
				}

				if (x != bounds.x || y != bounds.y) {

					bounds.x = x;
					bounds.y = y;
					elementData.element.setBounds(bounds);

					aligned = true;

				}

			}

		}

		return aligned;

	}

}
