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
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoableEdit;

import org.glasspath.aerialist.swing.BufferedImageMediaCache;
import org.glasspath.common.swing.search.UISearchHandler;
import org.glasspath.common.swing.selection.Selection;
import org.glasspath.common.swing.undo.DefaultUndoManager;

public abstract class EditorPanel<T extends EditorPanel<T>> extends JPanel implements AWTEventListener {

	public static final long MOUSE_EVENTS_MASK = AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK;

	private final EditorContext<T> editorContext;
	private final DefaultUndoManager undoManager;
	protected final Selection<Component> selection;

	private UISearchHandler searchHandler = null;
	private BufferedImageMediaCache mediaCache = null;

	public EditorPanel(EditorContext<T> editorContext) {

		this.editorContext = editorContext;
		this.undoManager = new DefaultUndoManager();
		this.selection = new Selection<>();

	}

	public Frame getFrame() {
		return null;
	}

	public abstract Component getContentContainer();

	public abstract EditorView<? extends EditorPanel<T>> getView();

	public abstract MouseOperationHandler<? extends EditorPanel<T>> getMouseOperationHandler();

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

	public EditorContext<T> getEditorContext() {
		return editorContext;
	}

	public void refresh(Component component) {
		refresh(component, true);
	}

	public void refresh(Component component, boolean resetYPolicy) {

	}

	protected void setEditable(boolean editable) {
		if (editorContext != null) {
			editorContext.setEditable(editable);
		}
	}

	public void focusContentContainer() {

	}

	public void deselectAll() {
		focusContentContainer();
		selection.deselectAll();
	}

	protected void createSearchHandler(Container container) {
		searchHandler = new UISearchHandler(container) {

			@Override
			public void textFound(JTextComponent component, String text, int index) {

				selection.clear();
				selection.select(component);

				component.select(index, index + text.length());

				repaint();

			}
		};
	}

	public void searchNext(String text) {
		if (searchHandler != null) {
			searchHandler.searchNext(text);
		}
	}

	public void searchPrevious(String text) {

	}

	public void cancelSearch() {
		if (searchHandler != null) {
			searchHandler.cancelSearch();
		}
	}

	@Override
	public void eventDispatched(AWTEvent event) {

		if (event instanceof MouseEvent) {

			if (editorContext != null && !editorContext.isEditable()) {

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
