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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.editor.AbstractEditorPanel;
import org.glasspath.aerialist.editor.ElementData;
import org.glasspath.aerialist.icons.Icons;
import org.glasspath.aerialist.resources.AerialistResources;
import org.glasspath.aerialist.swing.view.PageView;

public class ArrangeElementsAction extends AbstractAction {

	public static final int BRING_FORWARD = 0;
	public static final int BRING_TO_FRONT = 1;
	public static final int SEND_BACKWARD = 2;
	public static final int SEND_TO_BACK = 3;

	private final AbstractEditorPanel context;
	private final int arrangeAction;

	public ArrangeElementsAction(AbstractEditorPanel context, int arrangeAction, boolean toolbarButton) {

		this.context = context;
		this.arrangeAction = arrangeAction;

		if (arrangeAction == BRING_FORWARD) {
			if (!toolbarButton) {
				putValue(Action.NAME, AerialistResources.getString("BringForward")); //$NON-NLS-1$
			}
			putValue(Action.SHORT_DESCRIPTION, AerialistResources.getString("BringForward")); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, Icons.arrangeBringForward);
		} else if (arrangeAction == BRING_TO_FRONT) {
			if (!toolbarButton) {
				putValue(Action.NAME, AerialistResources.getString("BringToFront")); //$NON-NLS-1$
			}
			putValue(Action.SMALL_ICON, Icons.arrangeBringToFront);
			putValue(Action.SHORT_DESCRIPTION, AerialistResources.getString("BringToFront")); //$NON-NLS-1$
		} else if (arrangeAction == SEND_BACKWARD) {
			if (!toolbarButton) {
				putValue(Action.NAME, AerialistResources.getString("SendBackward")); //$NON-NLS-1$
			}
			putValue(Action.SHORT_DESCRIPTION, AerialistResources.getString("SendBackward")); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, Icons.arrangeSendBackward);
		} else if (arrangeAction == SEND_TO_BACK) {
			if (!toolbarButton) {
				putValue(Action.NAME, AerialistResources.getString("SendToBack")); //$NON-NLS-1$
			}
			putValue(Action.SHORT_DESCRIPTION, AerialistResources.getString("SendToBack")); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, Icons.arrangeSendToBack);
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		PageView pageView = null;
		List<Component> selection = new ArrayList<>();

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
						selection.add(elementView);
					}

				}

			}

		}

		if (pageView != null && selection.size() > 0) {

			List<ElementData> elementsData = new ArrayList<>();

			Component component;
			for (int i = 0; i < pageView.getComponentCount(); i++) {
				component = pageView.getComponent(i);
				if (selection.contains(component)) {
					elementsData.add(new ElementData(component, i));
				}
			}

			if (elementsData.size() > 0) {

				Collections.sort(elementsData, new Comparator<ElementData>() {

					@Override
					public int compare(ElementData o1, ElementData o2) {
						return Integer.compare(o1.index, o2.index);
					}
				});

				if (arrangeElements(pageView, elementsData, arrangeAction)) {
					context.undoableEditHappened(new ArrangeElementsUndoable(context, pageView, elementsData, arrangeAction));
				}

				ActionUtils.setSelection(context, pageView, elementsData);

			}

		}

	}

	public static boolean arrangeElements(PageView pageView, List<ElementData> elementsData, int arrangeAction) {

		boolean arrange = true;

		for (ElementData elementData : elementsData) {
			pageView.remove(elementData.element);
		}

		int index = 0;

		if (arrangeAction == BRING_FORWARD) {

			index = elementsData.get(0).index - 1;
			if (index < 0) {
				arrange = false;
			}

		} else if (arrangeAction == BRING_TO_FRONT) {

			index = elementsData.get(0).index - 1;
			if (index < 0) {
				arrange = false;
			} else {
				index = 0;
			}

		} else if (arrangeAction == SEND_BACKWARD) {

			index = elementsData.get(0).index + 1;
			if (index > pageView.getComponentCount()) {
				arrange = false;
			}

		} else if (arrangeAction == SEND_TO_BACK) {

			index = elementsData.get(0).index + 1;
			if (index > pageView.getComponentCount()) {
				arrange = false;
			} else {
				index = pageView.getComponentCount();
			}

		}

		for (ElementData elementData : elementsData) {
			if (arrange) {
				pageView.add(elementData.element, index++);
			} else {
				pageView.add(elementData.element, elementData.index);
			}
		}

		return arrange;

	}

}
