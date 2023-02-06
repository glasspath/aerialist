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
import java.awt.Cursor;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.glasspath.aerialist.swing.view.ElementContainer;
import org.glasspath.aerialist.swing.view.ISwingElementView;
import org.glasspath.aerialist.swing.view.TableCellView;

public abstract class MouseOperationHandler<T extends EditorPanel<T>> {

	protected final T context;

	protected int handleAtMouse = DocumentEditorView.HANDLE_UNKNOWN;
	protected boolean mouseOverSelectionEdge = false;
	protected Component horizontalResizable = null;

	protected Operation operation = null;

	public MouseOperationHandler(T context) {

		this.context = context;

	}

	public boolean startOperation(Operation operation) {
		if (this.operation == null) {
			this.operation = operation;
			return true;
		} else {
			return false;
		}
	}

	public boolean isOperationActive() {
		return operation != null;
	}

	public void generateMouseMovedEvent(Component component) {

		if (component != null) {

			Point p1 = component.getMousePosition();
			Point p2 = MouseInfo.getPointerInfo().getLocation();
			if (p1 != null && p2 != null) {
				MouseEvent e = new MouseEvent(component, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, p1.x, p1.y, p2.x, p2.y, 0, false, 0);
				mouseMoved(e, p1);
			}

		}

	}

	public void processMouseEvent(MouseEvent e) {

		Point p = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), context.getContentContainer());

		switch (e.getID()) {

		case MouseEvent.MOUSE_PRESSED:
			mousePressed(e, p);
			break;

		case MouseEvent.MOUSE_RELEASED:
			mouseReleased(e, p);
			break;

		case MouseEvent.MOUSE_CLICKED:
			mouseClicked(e, p);
			break;

		default:
			break;
		}

	}

	public void processMouseMotionEvent(MouseEvent e) {

		Point p = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), context.getContentContainer());

		switch (e.getID()) {

		case MouseEvent.MOUSE_ENTERED:
			mouseEntered(e, p);
			break;

		case MouseEvent.MOUSE_EXITED:
			mouseExited(e, p);
			break;

		case MouseEvent.MOUSE_MOVED:
			mouseMoved(e, p);
			break;

		case MouseEvent.MOUSE_DRAGGED:
			mouseDragged(e, p);
			break;

		default:
			break;
		}

	}

	public void mousePressed(MouseEvent e, Point p) {
		if (operation != null) {
			operation.mousePressed(e, p);
			if (operation.isDone()) {
				operation = null;
			}
		}
	}

	public void mouseReleased(MouseEvent e, Point p) {
		if (operation != null) {
			operation.mouseReleased(e, p);
			if (operation.isDone()) {
				operation = null;
			}
		}
	}

	public void mouseClicked(MouseEvent e, Point p) {
		if (operation != null) {
			operation.mouseClicked(e, p);
			if (operation.isDone()) {
				operation = null;
			}
		}
	}

	public void mouseEntered(MouseEvent e, Point p) {

		if (operation != null) {
			operation.mouseEntered(e, p);
		}

		postProcessMouseMotionEvent(e, p);

	}

	public void mouseExited(MouseEvent e, Point p) {

		if (operation != null) {
			operation.mouseExited(e, p);
		}

		postProcessMouseMotionEvent(e, p);

	}

	public void mouseMoved(MouseEvent e, Point p) {

		if (operation != null) {
			operation.mouseMoved(e, p);
		}

		postProcessMouseMotionEvent(e, p);

	}

	public void mouseDragged(MouseEvent e, Point p) {
		if (operation != null) {
			operation.mouseDragged(e, p);
		}
	}

	protected void postProcessMouseMotionEvent(MouseEvent e, Point p) {

		if (!e.isConsumed()) {

			handleAtMouse = DocumentEditorView.HANDLE_UNKNOWN;
			mouseOverSelectionEdge = false;
			horizontalResizable = null;

			if (e.getSource() instanceof JComponent) {

				JComponent source = (JComponent) e.getSource();
				if (source instanceof ElementContainer || source instanceof ISwingElementView<?> || source instanceof TableCellView) {

					if (operation instanceof InsertElementOperation) {
						source.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
					} else {

						handleAtMouse = context.getView().getHandleAtPoint(p);

						switch (handleAtMouse) {

						case DocumentEditorView.HANDLE_UNKNOWN:

							mouseOverSelectionEdge = context.getView().isPointOverSelectionEdge(p);
							if (mouseOverSelectionEdge) {
								source.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
								e.consume();
							} else {

								horizontalResizable = context.getView().getHorizontalResizable(p);
								if (horizontalResizable != null) {

									source.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
									e.consume();

								} else {

									// TODO: How to properly restore the cursor after changing it?
									if (source instanceof JTextComponent && ((JTextComponent) source).isEditable() && ((JTextComponent) source).isEnabled()) {
										source.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
									} else {
										source.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
									}

								}

							}

							break;

						case DocumentEditorView.HANDLE_NW:
							source.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
							e.consume();
							break;
						case DocumentEditorView.HANDLE_N:
							source.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
							e.consume();
							break;
						case DocumentEditorView.HANDLE_NE:
							source.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
							e.consume();
							break;
						case DocumentEditorView.HANDLE_W:
							source.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
							e.consume();
							break;
						case DocumentEditorView.HANDLE_E:
							source.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
							e.consume();
							break;
						case DocumentEditorView.HANDLE_SW:
							source.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
							e.consume();
							break;
						case DocumentEditorView.HANDLE_S:
							source.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
							e.consume();
							break;
						case DocumentEditorView.HANDLE_SE:
							source.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
							e.consume();
							break;
						default:
							break;
						}

					}

				}

			}

		}

	}

}
