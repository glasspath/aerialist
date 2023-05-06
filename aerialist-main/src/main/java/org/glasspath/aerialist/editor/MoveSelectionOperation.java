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
package org.glasspath.aerialist.editor;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.swing.view.PageView;

public class MoveSelectionOperation extends Operation {

	private final DocumentEditorPanel context;

	private boolean done = false;

	private Component component = null;
	private PageView pageView = null;
	private Point startPoint = null;
	private Rectangle originalBounds = null;

	public MoveSelectionOperation(DocumentEditorPanel context) {
		this.context = context;

		// TODO: For now layout Y-policy is always disabled because of following issues:
		// - Inserting element causes other elements to shift position
		// - Moving element above other element causes anchor to be created after which element also moves
		// - Undo/redo causes elements to shift due to newly created anchors
		context.getPageContainer().setYPolicyEnabled(false);

	}

	@Override
	public void mousePressed(MouseEvent e, Point p) {

		// TODO: Support moving of multiple elements
		if (context.getSelection().size() == 1) {

			component = AerialistUtils.getElementViewAsComponent(context.getSelection().get(0));
			if (component != null && component.getParent() instanceof PageView) {

				pageView = (PageView) component.getParent();
				startPoint = context.convertPointToPage(p, pageView, false);
				originalBounds = component.getBounds();

				e.consume();

			}

		}

	}

	@Override
	public void mouseDragged(MouseEvent e, Point p) {

		if (component != null && pageView != null && originalBounds != null && p != null) {

			Rectangle oldBounds = component.getBounds();
			Rectangle bounds = new Rectangle(oldBounds);

			Point point = context.convertPointToPage(p, pageView, false);
			Point location = new Point(originalBounds.x + (point.x - startPoint.x), originalBounds.y + (point.y - startPoint.y));

			context.snapToGrid(location);

			bounds.x = location.x;
			bounds.y = location.y;

			e.consume();

			component.setBounds(bounds);

			pageView.elementMoved(component, oldBounds, true);

			context.getSelection().fireSelectionChanged();
			context.refresh(pageView, false, false);

		}

	}

	@Override
	public void mouseReleased(MouseEvent e, Point p) {

		if (component != null && pageView != null && originalBounds != null) {

			pageView.updateVerticalAnchors();

			Rectangle bounds = component.getBounds();
			if (bounds.x != originalBounds.x || bounds.y != originalBounds.y) {
				context.undoableEditHappened(new MoveSelectionUndoable(context, component, pageView, originalBounds, bounds, context.getPageContainer().isYPolicyEnabled()));
			}

			context.getSelection().fireSelectionChanged();
			context.refresh(pageView);

		}

		e.consume();

		done = true;

	}

	@Override
	public boolean isDone() {
		return done;
	}

}
