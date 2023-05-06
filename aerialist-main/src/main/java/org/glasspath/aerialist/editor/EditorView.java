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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;

import javax.swing.JPanel;

import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.HeightPolicy;
import org.glasspath.aerialist.YPolicy;
import org.glasspath.aerialist.layout.VerticalLayout;
import org.glasspath.aerialist.swing.view.ElementContainer;
import org.glasspath.aerialist.swing.view.ISwingElementView;
import org.glasspath.aerialist.swing.view.PageView;
import org.glasspath.aerialist.swing.view.TableCellView;
import org.glasspath.aerialist.swing.view.TableView;

public class EditorView<T extends EditorPanel<T>> {

	public static final int HANDLE_UNKNOWN = -1;
	public static final int HANDLE_NW = 0;
	public static final int HANDLE_N = 1;
	public static final int HANDLE_NE = 2;
	public static final int HANDLE_W = 3;
	public static final int HANDLE_E = 4;
	public static final int HANDLE_SW = 5;
	public static final int HANDLE_S = 6;
	public static final int HANDLE_SE = 7;

	public static final Stroke SELECTION_STROKE = new BasicStroke(1.0F);
	public static final Color SELECTION_COLOR = new Color(75, 150, 255);
	public static final Stroke MULTI_SELECTION_STROKE = new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0, new float[] { 3, 2 }, 0);
	public static final Color MULTI_SELECTION_COLOR = new Color(100, 100, 150);
	public static final Stroke PAGE_SELECTION_STROKE = new BasicStroke(1.0F);
	public static final Color PAGE_SELECTION_COLOR = new Color(75, 150, 255);
	public static final Stroke TABLE_CELL_SELECTION_STROKE = new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0, new float[] { 3, 2 }, 0);
	public static final Color TABLE_CELL_SELECTION_COLOR = new Color(75, 150, 255);
	public static final Stroke SPRING_STROKE = new BasicStroke(1.0F);
	public static final Color SPRING_COLOR = new Color(75, 150, 255);
	public static final Stroke ANCHOR_RECT_STROKE = new BasicStroke(1.0F);
	public static final Color ANCHOR_RECT_COLOR = new Color(150, 195, 255);

	public static final int HANDLE_SIZE = 6;

	protected final T context;

	protected Rectangle mouseRect = new Rectangle();
	protected Rectangle selectionRect = null;
	protected Rectangle selectionOuterRect = null;
	protected Rectangle selectionInnerRect = null;
	protected Rectangle[] selectionRectHandles = new Rectangle[8];
	protected boolean selectionRectHandlesActive = false;

	public EditorView(T context) {

		this.context = context;

		for (int i = 0; i < selectionRectHandles.length; i++) {
			selectionRectHandles[i] = new Rectangle();
			selectionRectHandles[i].width = HANDLE_SIZE;
			selectionRectHandles[i].height = HANDLE_SIZE;
		}

	}

	public void drawEditorBackground(Graphics2D g2d, JPanel pageContainer, boolean editable) {

	}

	public void drawEditorForeground(Graphics2D g2d, JPanel pageContainer, boolean editable) {

	}

	public void drawLayerView(Graphics2D g2d, PageView pageView, PageView layerView, boolean editable) {

	}

	protected void drawSelectionRectangle(Graphics2D g2d, boolean editable) {

		selectionRect = null;
		selectionOuterRect = null;
		selectionInnerRect = null;
		selectionRectHandlesActive = false;

		if (context.selection.size() > 0) {

			Rectangle rect;
			Container parent;

			rect = context.selection.get(0).getBounds();
			parent = context.selection.get(0).getParent();

			Rectangle cellRect = null;
			if (context.selection.size() == 1 && context.selection.get(0) instanceof TableCellView) {

				cellRect = context.selection.get(0).getBounds();

				if (cellRect.x > 0) {
					cellRect.x -= 1;
				} else {
					cellRect.width -= 1;
				}

			}

			while (parent != null) {

				if (cellRect != null) {
					cellRect.translate(parent.getX(), parent.getY());
				}

				if (parent instanceof PageView) {

					if (cellRect != null) {

						g2d.setStroke(TABLE_CELL_SELECTION_STROKE);
						g2d.setColor(TABLE_CELL_SELECTION_COLOR);
						g2d.draw(cellRect);

					}

					rect.translate(parent.getX(), parent.getY());

					if (context.selection.size() == 1) {

						if (editable) {

							Component elementView = AerialistUtils.getElementViewAsComponent(context.selection.get(0));
							if (elementView != null) {

								drawVerticalAnchors(g2d, (PageView) parent, elementView);

								layoutSelectionRectHandles(rect, AerialistUtils.getHeightPolicy(elementView));
								selectionRectHandlesActive = true;

								selectionOuterRect = new Rectangle(rect);
								selectionOuterRect.grow(5, 5);

								selectionInnerRect = new Rectangle(rect);
								selectionInnerRect.grow(-5, -5);

							}

						}

						g2d.setStroke(SELECTION_STROKE);
						g2d.setColor(SELECTION_COLOR);

					} else {
						g2d.setStroke(MULTI_SELECTION_STROKE);
						g2d.setColor(MULTI_SELECTION_COLOR);
					}

					g2d.draw(rect);

					break;

				}

				rect = parent.getBounds();
				parent = parent.getParent();

			}

			selectionRect = rect;

			for (int i = 1; i < context.selection.size(); i++) {

				rect = context.selection.get(i).getBounds();
				parent = context.selection.get(i).getParent();

				while (parent != null) {

					if (parent instanceof PageView) {

						rect.translate(parent.getX(), parent.getY());
						g2d.setStroke(MULTI_SELECTION_STROKE);
						g2d.setColor(MULTI_SELECTION_COLOR);
						g2d.draw(rect);

						selectionRect.add(rect);

						break;

					}

					rect = parent.getBounds();
					parent = parent.getParent();

				}

			}

			if (context.selection.size() > 1) {
				selectionRect.grow(2, 2);
				g2d.draw(selectionRect);
			}

			if (selectionRectHandlesActive) {
				drawSelectionRectHandles(g2d);
			}

		}

	}

	protected void drawVerticalAnchors(Graphics2D g2d, PageView pageView, Component component) {

		if (component instanceof ISwingElementView<?>) {

			ISwingElementView<?> elementView = (ISwingElementView<?>) component;
			VerticalLayout<ElementContainer, ISwingElementView<?>>.AnchorList anchorList = pageView.getAnchorList(elementView);

			if (elementView.getYPolicy() == YPolicy.DEFAULT && anchorList.anchors != null && anchorList.anchors.size() > 0) {

				int x = pageView.getX() + component.getX() + (component.getWidth() / 2);
				int yMargin = pageView.getY() + component.getY() - anchorList.margin;

				int xMin = x;
				int xMax = x;

				g2d.setStroke(SPRING_STROKE);
				g2d.setColor(SPRING_COLOR);
				drawVerticalSpring(g2d, x, pageView.getY() + component.getY(), yMargin);

				Rectangle rect;
				for (ISwingElementView<?> anchor : anchorList.anchors) {

					int xAnchor = pageView.getX() + anchor.getX() + (anchor.getWidth() / 2);

					g2d.setStroke(SPRING_STROKE);
					g2d.setColor(SPRING_COLOR);
					drawVerticalSpring(g2d, xAnchor, yMargin, pageView.getY() + anchor.getY() + anchor.getHeight());

					if (xAnchor < xMin) {
						xMin = xAnchor;
					}
					if (xAnchor > xMax) {
						xMax = xAnchor;
					}

					rect = new Rectangle(anchor.getX(), anchor.getY(), anchor.getWidth(), anchor.getHeight());
					rect.translate(pageView.getX(), pageView.getY());

					g2d.setStroke(ANCHOR_RECT_STROKE);
					g2d.setColor(ANCHOR_RECT_COLOR);
					g2d.draw(rect);

				}

				if (xMax > xMin) {
					g2d.drawLine(xMax, yMargin, xMin, yMargin);
				}

			}

		}

	}

	protected void drawVerticalSpring(Graphics2D g2d, int x, int yBottom, int yTop) {

		if (yBottom > yTop + 10) {

			int y = yBottom;

			g2d.drawLine(x, y, x, y - 3);
			g2d.drawLine(x, y - 3, x - 3, y - 4);
			y -= 4;

			boolean leftToRight = true;
			while (y > yTop + 4) {

				if (leftToRight) {
					g2d.drawLine(x - 3, y, x + 3, y - 2);
				} else {
					g2d.drawLine(x + 3, y, x - 3, y - 2);
				}

				leftToRight = !leftToRight;
				y -= 2;

			}

			if (leftToRight) {
				g2d.drawLine(x - 3, y, x, y - 1);
			} else {
				g2d.drawLine(x + 3, y, x, y - 1);
			}

			g2d.drawLine(x, y - 1, x, yTop);

		} else if (yBottom != yTop) {
			g2d.drawLine(x, yBottom, x, yTop);
		}

	}

	private void layoutSelectionRectHandles(Rectangle rect, HeightPolicy heightPolicy) {

		setSelectionRectHandleLocation(HANDLE_W, rect.x, rect.y + (rect.height / 2));
		setSelectionRectHandleLocation(HANDLE_E, rect.x + rect.width, rect.y + (rect.height / 2));

		if (heightPolicy == HeightPolicy.DEFAULT) {

			setSelectionRectHandleLocation(HANDLE_NW, rect.x, rect.y);
			setSelectionRectHandleLocation(HANDLE_N, rect.x + (rect.width / 2), rect.y);
			setSelectionRectHandleLocation(HANDLE_NE, rect.x + rect.width, rect.y);

			setSelectionRectHandleLocation(HANDLE_SW, rect.x, rect.y + rect.height);
			setSelectionRectHandleLocation(HANDLE_S, rect.x + (rect.width / 2), rect.y + rect.height);
			setSelectionRectHandleLocation(HANDLE_SE, rect.x + rect.width, rect.y + rect.height);

		} else {

			disableSelectionRectHandle(HANDLE_NW);
			disableSelectionRectHandle(HANDLE_N);
			disableSelectionRectHandle(HANDLE_NE);

			disableSelectionRectHandle(HANDLE_SW);
			disableSelectionRectHandle(HANDLE_S);
			disableSelectionRectHandle(HANDLE_SE);

		}

	}

	private void setSelectionRectHandleLocation(int index, int x, int y) {
		selectionRectHandles[index].x = x - (HANDLE_SIZE / 2);
		selectionRectHandles[index].y = y - (HANDLE_SIZE / 2);
	}

	private void disableSelectionRectHandle(int index) {
		selectionRectHandles[index].x = Integer.MIN_VALUE;
	}

	private boolean isSelectionRectHandleEnabled(Rectangle handle) {
		return handle.x != Integer.MIN_VALUE;
	}

	private void drawSelectionRectHandles(Graphics2D g2d) {
		g2d.setColor(SELECTION_COLOR);
		for (Rectangle handle : selectionRectHandles) {
			if (isSelectionRectHandleEnabled(handle)) {
				g2d.fill(handle);
			}
		}
	}

	private void updateMouseRect(Point location) {
		mouseRect.x = location.x - 5;
		mouseRect.y = location.y - 5;
		mouseRect.width = 10;
		mouseRect.height = 10;
	}

	protected int getHandleAtPoint(Point p) {

		if (selectionRectHandlesActive && p != null) {

			updateMouseRect(p);

			for (int i = 0; i < selectionRectHandles.length; i++) {
				if (mouseRect.intersects(selectionRectHandles[i])) {
					return i;
				}
			}

		}

		return HANDLE_UNKNOWN;

	}

	protected boolean isPointOverSelectionEdge(Point p) {
		if (selectionOuterRect != null && selectionInnerRect != null && p != null) {
			return selectionOuterRect.contains(p) && !selectionInnerRect.contains(p);
		} else {
			return false;
		}
	}

	protected Component getHorizontalResizable(Point p) {

		if (selectionOuterRect != null && p != null) {

			updateMouseRect(p);

			if (context.selection.size() == 1) {

				ISwingElementView<?> elementView = AerialistUtils.getElementView(context.selection.get(0));
				if (elementView instanceof TableView) {

					TableView tableView = (TableView) elementView;
					if (tableView.getParent() instanceof PageView) {

						Component component;
						Rectangle rightBounds;
						for (int i = 0; i < tableView.getComponentCount(); i++) {

							component = tableView.getComponent(i);
							if (component instanceof TableCellView) {

								rightBounds = component.getBounds();
								rightBounds.x += rightBounds.width - 1;
								rightBounds.width = 2;

								rightBounds.translate(tableView.getParent().getX(), tableView.getParent().getY());
								rightBounds.translate(tableView.getX(), tableView.getY());

								if (rightBounds.intersects(mouseRect)) {
									return component;
								}

							}

						}

					}

				}

			}

		}

		return null;

	}

}
