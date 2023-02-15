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

public class DragHandleOperation extends Operation {

	private final DocumentEditorPanel context;
	private final int handle;

	private boolean done = false;

	private Component component = null;
	private PageView pageView = null;
	private Rectangle originalBounds = null;

	public DragHandleOperation(DocumentEditorPanel context, int handle) {
		this.context = context;
		this.handle = handle;
	}

	@Override
	public void mousePressed(MouseEvent e, Point p) {

		if (context.getSelection().size() == 1) {

			component = AerialistUtils.getElementViewAsComponent(context.getSelection().get(0));
			if (component != null && component.getParent() instanceof PageView) {

				pageView = (PageView) component.getParent();
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

			Point point = context.convertPointToPage(p, pageView, true);

			switch (handle) {

			case DocumentEditorView.HANDLE_NW:
				if (point.x < originalBounds.x + originalBounds.width) {
					bounds.x = point.x;
					bounds.width = (originalBounds.x - point.x) + originalBounds.width;
				} else {
					bounds.x = originalBounds.x + originalBounds.width;
					bounds.width = (point.x - originalBounds.x) - originalBounds.width;
				}
				if (point.y < originalBounds.y + originalBounds.height) {
					bounds.y = point.y;
					bounds.height = (originalBounds.y - point.y) + originalBounds.height;
				} else {
					bounds.y = originalBounds.y + originalBounds.height;
					bounds.height = (point.y - originalBounds.y) - originalBounds.height;
				}
				break;

			case DocumentEditorView.HANDLE_N:
				if (point.y < originalBounds.y + originalBounds.height) {
					bounds.y = point.y;
					bounds.height = (originalBounds.y - point.y) + originalBounds.height;
				} else {
					bounds.y = originalBounds.y + originalBounds.height;
					bounds.height = (point.y - originalBounds.y) - originalBounds.height;
				}
				break;

			case DocumentEditorView.HANDLE_NE:
				if (point.x > originalBounds.x) {
					bounds.x = originalBounds.x;
					bounds.width = point.x - originalBounds.x;
				} else {
					bounds.x = point.x;
					bounds.width = originalBounds.x - point.x;
				}
				if (point.y < originalBounds.y + originalBounds.height) {
					bounds.y = point.y;
					bounds.height = (originalBounds.y - point.y) + originalBounds.height;
				} else {
					bounds.y = originalBounds.y + originalBounds.height;
					bounds.height = (point.y - originalBounds.y) - originalBounds.height;
				}
				break;

			case DocumentEditorView.HANDLE_W:
				if (point.x < originalBounds.x + originalBounds.width) {
					bounds.x = point.x;
					bounds.width = (originalBounds.x - point.x) + originalBounds.width;
				} else {
					bounds.x = originalBounds.x + originalBounds.width;
					bounds.width = (point.x - originalBounds.x) - originalBounds.width;
				}
				break;

			case DocumentEditorView.HANDLE_E:
				if (point.x > originalBounds.x) {
					bounds.x = originalBounds.x;
					bounds.width = point.x - originalBounds.x;
				} else {
					bounds.x = point.x;
					bounds.width = originalBounds.x - point.x;
				}
				break;

			case DocumentEditorView.HANDLE_SW:
				if (point.x < originalBounds.x + originalBounds.width) {
					bounds.x = point.x;
					bounds.width = (originalBounds.x - point.x) + originalBounds.width;
				} else {
					bounds.x = originalBounds.x + originalBounds.width;
					bounds.width = (point.x - originalBounds.x) - originalBounds.width;
				}
				if (point.y > originalBounds.y) {
					bounds.y = originalBounds.y;
					bounds.height = point.y - originalBounds.y;
				} else {
					bounds.y = point.y;
					bounds.height = originalBounds.y - point.y;
				}
				break;

			case DocumentEditorView.HANDLE_S:
				if (point.y > originalBounds.y) {
					bounds.y = originalBounds.y;
					bounds.height = point.y - originalBounds.y;
				} else {
					bounds.y = point.y;
					bounds.height = originalBounds.y - point.y;
				}
				break;

			case DocumentEditorView.HANDLE_SE:
				if (point.x > originalBounds.x) {
					bounds.x = originalBounds.x;
					bounds.width = point.x - originalBounds.x;
				} else {
					bounds.x = point.x;
					bounds.width = originalBounds.x - point.x;
				}
				if (point.y > originalBounds.y) {
					bounds.y = originalBounds.y;
					bounds.height = point.y - originalBounds.y;
				} else {
					bounds.y = point.y;
					bounds.height = originalBounds.y - point.y;
				}
				break;

			default:
				break;
			}

			e.consume();

			component.setBounds(bounds);

			pageView.elementResized(component, oldBounds);

			context.getSelection().fireSelectionChanged();
			context.refresh(pageView);

		}

	}

	@Override
	public void mouseReleased(MouseEvent e, Point p) {

		if (component != null && pageView != null && originalBounds != null) {

			Rectangle bounds = component.getBounds();
			if (bounds.x != originalBounds.x || bounds.y != originalBounds.y || bounds.width != originalBounds.width || bounds.height != originalBounds.height) {
				context.undoableEditHappened(new ResizeUndoable(context, component, pageView, originalBounds, bounds, context.getPageContainer().isYPolicyEnabled()));
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
