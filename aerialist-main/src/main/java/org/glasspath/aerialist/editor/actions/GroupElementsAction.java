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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.HeightPolicy;
import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.editor.ElementData;
import org.glasspath.aerialist.resources.Resources;
import org.glasspath.aerialist.swing.view.GroupView;
import org.glasspath.aerialist.swing.view.PageView;

public class GroupElementsAction extends AbstractAction {

	private final DocumentEditorPanel context;
	private final PageView pageView;
	private final List<Component> elements;

	public GroupElementsAction(DocumentEditorPanel context, PageView pageView, List<Component> elements) {

		this.context = context;
		this.pageView = pageView;
		this.elements = elements;

		putValue(Action.NAME, Resources.getString("GroupElements")); //$NON-NLS-1$
		putValue(Action.SHORT_DESCRIPTION, Resources.getString("GroupElements")); //$NON-NLS-1$

		boolean enabled = true;

		if (elements.size() > 1) {
			for (Component element : elements) {
				if (element instanceof GroupView || element.getParent() instanceof GroupView) {
					enabled = false;
					break;
				}
			}
		} else {
			enabled = false;
		}

		setEnabled(enabled);

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		List<ElementData> elementsData = new ArrayList<>();

		GroupView groupView = new GroupView(context.getPageContainer());
		Rectangle groupBounds = null;

		Component component;
		for (int i = 0; i < pageView.getComponentCount(); i++) {

			component = pageView.getComponent(i);
			if (elements.contains(component)) {

				elementsData.add(new ElementData(component, i));

				if (groupBounds == null) {
					groupBounds = component.getBounds();
				} else {
					groupBounds.add(component.getBounds());
				}

			}

		}

		Collections.sort(elementsData, new Comparator<ElementData>() {

			@Override
			public int compare(ElementData o1, ElementData o2) {
				return Integer.compare(o1.index, o2.index);
			}
		});

		if (groupBounds != null) {

			groupView.setBounds(groupBounds);

			HeightPolicy heightPolicy = HeightPolicy.DEFAULT;

			List<ElementData> newElementsData = new ArrayList<>();

			ElementData elementData;
			Rectangle bounds;
			for (int i = 0; i < elementsData.size(); i++) {

				elementData = elementsData.get(i);

				pageView.remove(elementData.element);

				bounds = new Rectangle(elementData.bounds);
				bounds.x -= groupBounds.x;
				bounds.y -= groupBounds.y;
				elementData.element.setBounds(bounds);

				groupView.add(elementData.element);

				if (heightPolicy == HeightPolicy.DEFAULT && AerialistUtils.getHeightPolicy(elementData.element) == HeightPolicy.AUTO) {
					heightPolicy = HeightPolicy.AUTO;
				}

				newElementsData.add(new ElementData(elementData.element, i));

			}

			groupView.setHeightPolicy(heightPolicy);

			pageView.add(groupView, elementsData.get(0).index);

			groupView.updateVerticalAnchors();
			pageView.updateVerticalAnchors();

			context.undoableEditHappened(new GroupElementsUndoable(context, pageView, groupView, elementsData, newElementsData));

			context.getSelection().clear();

			context.refresh(pageView);

		}

	}

}
