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

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.undo.UndoableEdit;

import org.glasspath.aerialist.media.BufferedImageMediaCache;
import org.glasspath.common.swing.FrameContext;
import org.glasspath.common.swing.search.UISearchHandler;
import org.glasspath.common.swing.selection.Selection;
import org.glasspath.common.swing.undo.DefaultUndoManager;

public abstract class AbstractEditorPanel extends JPanel implements AWTEventListener {

	public static final long MOUSE_EVENTS_MASK = AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK;

	private final DefaultUndoManager undoManager;
	protected final Selection<Component> selection;

	private UISearchHandler searchHandler = null;
	private BufferedImageMediaCache mediaCache = null;

	public AbstractEditorPanel() {
		this.undoManager = new DefaultUndoManager();
		this.selection = new Selection<>();
	}

	public abstract FrameContext getFrameContext();

	public abstract Component getContentContainer();

	public DefaultUndoManager getUndoManager() {
		return undoManager;
	}

	public void undoableEditHappened(UndoableEdit edit) {
		undoManager.addEdit(edit);
	}

	public BufferedImageMediaCache getMediaCache() {
		return mediaCache;
	}

	public void setMediaCache(BufferedImageMediaCache mediaCache) {
		this.mediaCache = mediaCache;
	}

	public Selection<Component> getSelection() {
		return selection;
	}

	public UISearchHandler getSearchHandler() {
		return searchHandler;
	}

	public void setSearchHandler(UISearchHandler searchHandler) {
		this.searchHandler = searchHandler;
	}

	public void searchNext(String text) {
		if (searchHandler != null) {
			searchHandler.search(text, false);
		}
	}

	public void searchPrevious(String text) {
		if (searchHandler != null) {
			searchHandler.search(text, true);
		}
	}

	public void cancelSearch() {
		if (searchHandler != null) {
			searchHandler.cancelSearch();
		}
	}

	public void refresh(Component component) {
		refresh(component, null, true, false);
	}

	public void refresh(Component component, Map<Component, Rectangle> anchoredElementBounds) {
		refresh(component, anchoredElementBounds, true, false);
	}

	public abstract void refresh(Component component, Map<Component, Rectangle> anchoredElementBounds, boolean resetYPolicy, boolean revalidateScrollPane);

	protected abstract boolean isEditable();

	protected abstract void setEditable(boolean editable);

	public void focusContentContainer() {

	}

	public void deselectAll() {
		focusContentContainer();
		selection.deselectAll();
	}

	@Override
	public void eventDispatched(AWTEvent event) {

		if (event instanceof MouseEvent) {

			if (!isEditable()) {

				if (event.getID() == MouseEvent.MOUSE_CLICKED && ((MouseEvent) event).getClickCount() >= 2) {
					setEditable(true);
				} else {
					// ((MouseEvent) event).consume();
				}

			} else {

				switch (event.getID()) {

				case MouseEvent.MOUSE_PRESSED:
				case MouseEvent.MOUSE_RELEASED:
				case MouseEvent.MOUSE_CLICKED:
				case MouseEvent.MOUSE_ENTERED:
				case MouseEvent.MOUSE_EXITED:
					handleMouseEvent((MouseEvent) event);
					break;
				case MouseEvent.MOUSE_MOVED:
				case MouseEvent.MOUSE_DRAGGED:
					handleMouseMotionEvent((MouseEvent) event);
					break;
				case MouseEvent.MOUSE_WHEEL:
					// handleMouseWheelEvent((MouseWheelEvent) event);
					break;

				}

			}

		}

	}

	protected void handleMouseEvent(MouseEvent e) {

	}

	protected void handleMouseMotionEvent(MouseEvent e) {

	}

	protected void showMenu(Component component, int x, int y) {

	}

}
