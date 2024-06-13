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
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.resources.AerialistResources;
import org.glasspath.aerialist.swing.view.GroupView;
import org.glasspath.aerialist.swing.view.PageView;

public class UngroupElementsAction extends AbstractAction {

	private final DocumentEditorPanel context;
	private final PageView pageView;
	private final List<Component> elements;

	public UngroupElementsAction(DocumentEditorPanel context, PageView pageView, List<Component> elements) {

		this.context = context;
		this.pageView = pageView;
		this.elements = elements;

		putValue(Action.NAME, AerialistResources.getString("Ungroup")); //$NON-NLS-1$
		putValue(Action.SHORT_DESCRIPTION, AerialistResources.getString("Ungroup")); //$NON-NLS-1$
		// putValue(Action.SMALL_ICON, Icons.add2_16x16);

		boolean enabled = false;

		if (elements.size() > 1) {
			for (Component element : elements) {
				if (element.getParent() instanceof GroupView) {
					enabled = true;
					break;
				}
			}
		}

		setEnabled(enabled);

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		/* TODO
		List<ElementData> elementsData = new ArrayList<>();

		Component component;
		for (int i = 0; i < pageView.getComponentCount(); i++) {
			component = pageView.getComponent(i);
			if (elements.contains(component)) {
				elementsData.add(new ElementData(component, i));
			}
		}

		Collections.sort(elementsData, new Comparator<ElementData>() {

			@Override
			public int compare(ElementData o1, ElementData o2) {
				return Integer.compare(o1.index, o2.index);
			}
		});

		for (ElementData elementData : elementsData) {
			pageView.remove(elementData.element);
		}

		context.undoableEditHappened(new DeleteElementsUndoable(context, pageView, elementsData));

		context.getSelection().clear();

		context.refresh(pageView);
		*/

	}

}
