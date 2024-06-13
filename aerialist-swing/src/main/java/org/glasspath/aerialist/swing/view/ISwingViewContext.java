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
package org.glasspath.aerialist.swing.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretListener;

import org.glasspath.aerialist.layout.ILayoutContext;
import org.glasspath.aerialist.swing.view.TextView.TextData;

public interface ISwingViewContext extends ILayoutContext<BufferedImage>, FocusListener, CaretListener {

	public static int CONTAINER_PAINT_FLAG_EDITABLE = 1;
	public static int CONTAINER_PAINT_FLAG_AUTO_REPAINT_DISABLED = 2; // TODO? This was added to disable auto-repainting in ElementContainer, but this auto-repainting should probably be removed

	public static int VIEW_PAINT_FLAG_SELECTED_PRIMARY = 1;
	public static int VIEW_PAINT_FLAG_SELECTED_SECONDARY = 2;
	public static int VIEW_PAINT_FLAG_DECORATE_FIELDS = 4;

	public boolean isRightMouseSelectionAllowed();

	public void viewEventHappened(ViewEvent viewEvent);

	public Map<Component, Rectangle> getAnchoredElementBounds(Component component);

	public void createUndoableEdit(ISwingViewContext viewContext, TextView textView, TextData oldTextData, TextData newTextData, Map<Component, Rectangle> anchoredElementBounds);

	public void refresh(Component component, Map<Component, Rectangle> anchoredElementBounds);

	public Color getDefaultForeground();

	public int getContainerPaintFlags();

	public int getViewPaintFlags(Component view);

	public static boolean getContainerPaintFlag(ISwingViewContext viewContext, int flag) {
		return (viewContext.getContainerPaintFlags() & flag) > 0;
	}

	public static boolean getViewPaintFlag(ISwingViewContext viewContext, Component view, int flag) {
		return (viewContext.getViewPaintFlags(view) & flag) > 0;
	}

	public static void installSelectionHandler(JComponent view, ISwingViewContext viewContext) {

		view.setFocusable(true);

		view.addFocusListener(viewContext);

		view.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {

				if (SwingUtilities.isLeftMouseButton(e) || (SwingUtilities.isRightMouseButton(e) && viewContext.isRightMouseSelectionAllowed())) {

					// TODO: Currently we don't remove focus from a component when it is no longer selected,
					// clicking on a component (page for example) which still has focus leads to problems,
					// for that reason we generate a focusGained event here to make sure it gets selected..
					if (view.hasFocus()) {
						viewContext.focusGained(new FocusEvent(view, FocusEvent.FOCUS_GAINED));
					} else {
						view.requestFocusInWindow();
					}

				}

			}
		});

	}

	public static class ViewEvent {

		public static final int EVENT_UNKNOWN = 0;
		public static final int EVENT_NOTHING_COPIED = 1;

		public final JComponent source;
		public final int id;

		public ViewEvent(JComponent source, int id) {
			this.source = source;
			this.id = id;
		}

	}

}
