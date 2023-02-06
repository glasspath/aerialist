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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.undo.UndoableEdit;

import org.glasspath.aerialist.Aerialist;
import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.Page;
import org.glasspath.aerialist.editor.actions.ActionUtils;
import org.glasspath.aerialist.media.MediaCache;
import org.glasspath.aerialist.swing.view.PageContainer;
import org.glasspath.aerialist.swing.view.PageView;
import org.glasspath.aerialist.swing.view.TableCellView;
import org.glasspath.aerialist.text.font.FontCache;
import org.glasspath.common.swing.keyboard.KeyboardUtils;
import org.glasspath.common.swing.selection.SelectionListener;

public class DocumentEditorPanel extends EditorPanel<DocumentEditorPanel> {

	private final Aerialist context;
	protected final DocumentEditorView view;
	protected final MouseOperationHandler<DocumentEditorPanel> mouseOperationHandler;
	protected final EditorPageContainer pageContainer;
	private final JScrollPane mainScrollPane;

	private boolean gridEnabled = true;
	private boolean gridVisible = true;
	private int gridSpacing = 10;
	private boolean guidesVisible = true;
	private boolean layoutLocked = false;

	private boolean scrollLock = false;

	public DocumentEditorPanel(Aerialist context, DocumentEditorContext editorContext) {

		super(editorContext);

		this.context = context;

		if (editorContext != null) {
			gridVisible = editorContext.isGridDefaultVisible();
			guidesVisible = editorContext.isGuidesDefaultVisible();
		}

		setLayout(new BorderLayout());

		view = new DocumentEditorView(this);
		mouseOperationHandler = new MouseOperationHandler<DocumentEditorPanel>(this) {

			@Override
			public void mousePressed(MouseEvent e, Point p) {

				if (operation == null) {

					if (handleAtMouse != DocumentEditorView.HANDLE_UNKNOWN) {
						operation = new DragHandleOperation(context, handleAtMouse);
					} else if (mouseOverSelectionEdge) {
						operation = new MoveSelectionOperation(context);
					} else if (horizontalResizable instanceof TableCellView) {
						operation = new ResizeTableCellOperation(context, (TableCellView) horizontalResizable);
					}

				}

				super.mousePressed(e, p);

			}
		};

		pageContainer = new EditorPageContainer();

		mainScrollPane = new JScrollPane(pageContainer);
		mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
		mainScrollPane.getVerticalScrollBar().setUnitIncrement(25);
		add(mainScrollPane, BorderLayout.CENTER);

		selection.addSelectionListener(new SelectionListener() {

			@Override
			public void selectionChanged() {
				context.getMainPanel().updateEditMenu();
				context.getTextFormatTools().textSelectionChanged();
			}
		});

		createSearchHandler(pageContainer);

	}

	public Aerialist getContext() {
		return context;
	}

	@Override
	public Component getContentContainer() {
		return pageContainer;
	}

	@Override
	public EditorView<DocumentEditorPanel> getView() {
		return view;
	}

	@Override
	public MouseOperationHandler<DocumentEditorPanel> getMouseOperationHandler() {
		return mouseOperationHandler;
	}

	@Override
	public void undoableEditHappened(UndoableEdit edit) {
		super.undoableEditHappened(edit);
		context.setContentChanged(true);
	}

	public EditorPageContainer getPageContainer() {
		return pageContainer;
	}

	public boolean isGridEnabled() {
		return gridEnabled;
	}

	public void setGridEnabled(boolean gridEnabled) {
		this.gridEnabled = gridEnabled;
	}

	public boolean isGridVisible() {
		return gridVisible;
	}

	public void setGridVisible(boolean gridVisible) {
		this.gridVisible = gridVisible;
	}

	public int getGridSpacing() {
		return gridSpacing;
	}

	public void setGridSpacing(int gridSpacing) {
		this.gridSpacing = gridSpacing;
	}

	public boolean isGuidesVisible() {
		return guidesVisible;
	}

	public void setGuidesVisible(boolean guidesVisible) {
		this.guidesVisible = guidesVisible;
	}

	public boolean isLayoutLocked() {
		return layoutLocked;
	}

	public void setLayoutLocked(boolean layoutLocked) {
		this.layoutLocked = layoutLocked;
		pageContainer.setYPolicyEnabled(!layoutLocked);
	}

	public void setScrollLock(boolean scrollLock) {
		this.scrollLock = scrollLock;
	}

	@Override
	public void handleMouseEvent(MouseEvent e) {

		mouseOperationHandler.processMouseEvent(e);

		Component component = e.getComponent();
		if (e.getID() == MouseEvent.MOUSE_PRESSED && SwingUtilities.isRightMouseButton(e) && e.getComponent() != null) {

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					showMenu(component, e.getX(), e.getY());
				}
			});

		}

	}

	@Override
	public void handleMouseMotionEvent(MouseEvent e) {
		mouseOperationHandler.processMouseMotionEvent(e);
	}

	@Override
	public void focusContentContainer() {
		pageContainer.requestFocusInWindow();
	}

	@Override
	public void refresh(Component component, boolean resetYPolicy) {

		if (component != null) {

			component.invalidate();
			component.validate();
			component.repaint();

			if (component == pageContainer.getHeaderView() || component == pageContainer.getFooterView()) {
				pageContainer.repaint();
			}

		} else {

			pageContainer.invalidate();
			pageContainer.validate();
			pageContainer.repaint();

			// If a page was added/removed we need to re-validate the scroll-pane
			mainScrollPane.revalidate();

		}

		if (resetYPolicy) {

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					pageContainer.setYPolicyEnabled(!layoutLocked);
				}
			});

		}

	}

	@Override
	protected void setEditable(boolean editable) {
		super.setEditable(editable);

		context.showTools(null); // TODO?

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				context.getGlassPane().updateLayout(true);
			}
		});

	}

	public Point convertPointToPage(Point p, PageView pageView, boolean snap) {

		Point point = SwingUtilities.convertPoint(pageContainer, p, pageView);

		if (snap) {
			snapToGrid(point);
		}

		return point;

	}

	public void snapToGrid(Point p) {

		if (gridEnabled) {

			int xOffset = p.x % gridSpacing;
			if (xOffset > (gridSpacing) / 2) {
				p.x += gridSpacing - xOffset;
			} else {
				p.x -= xOffset;
			}

			int yOffset = p.y % gridSpacing;
			if (yOffset > (gridSpacing) / 2) {
				p.y += gridSpacing - yOffset;
			} else {
				p.y -= yOffset;
			}

		}

	}

	public void scrollToTop() {
		mainScrollPane.getVerticalScrollBar().setValue(0);
	}

	@Override
	protected void showMenu(Component component, int x, int y) {

		JMenu menu = new JMenu();

		if (selection.size() > 1) {
			ActionUtils.populateMenu(menu, this, selection);
		} else {
			ActionUtils.populateMenu(menu, this, component);
		}

		JPopupMenu popupMenu = menu.getPopupMenu();

		if (popupMenu.getComponentCount() > 0 && popupMenu.getComponent(menu.getPopupMenu().getComponentCount() - 1) instanceof JPopupMenu.Separator) {
			popupMenu.remove(menu.getPopupMenu().getComponentCount() - 1);
		}

		popupMenu.show(component, x, y);

	}

	public void populateEditMenu(JMenu menu) {
		if (selection.size() > 1) {
			ActionUtils.populateMenu(menu, this, selection);
		} else if (selection.size() == 1 && selection.get(0) instanceof Component) { // TODO?
			ActionUtils.populateMenu(menu, this, (Component) selection.get(0));
		}
	}

	public class EditorPageContainer extends PageContainer {

		private boolean editingHeader = false;
		private boolean editingFooter = false;
		private PageView replacedPageView = null;
		private int replacedPageViewIndex = -1;

		public EditorPageContainer() {
			super();

			setFocusable(true);
			addFocusListener(new FocusAdapter() {

				@Override
				public void focusGained(FocusEvent e) {
					if (!getMouseOperationHandler().isOperationActive()) {
						selection.deselectAll();
					}
				}
			});
			addMouseListener(new MouseAdapter() {

				@Override
				public void mousePressed(MouseEvent e) {
					requestFocusInWindow();
				}
			});

		}

		@Override
		public void setLocation(int x, int y) {
			// TODO: Is there a better way to lock a JScrollPane?
			if (!scrollLock) {
				super.setLocation(x, y);
			}
		}

		@Override
		public FontCache<Font> getFontCache() {
			return null;
		}

		@Override
		public MediaCache<BufferedImage> getMediaCache() {
			return DocumentEditorPanel.this.getMediaCache();
		}

		@Override
		public void setPageViews(List<PageView> pageViews) {

			int scrollPosition = mainScrollPane.getVerticalScrollBar().getValue();

			selection.clear();

			super.setPageViews(pageViews);

			loadPageViews();
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {

					mainScrollPane.getVerticalScrollBar().setValue(scrollPosition);
					selection.fireSelectionChanged();

				}
			});

		}

		@Override
		public boolean isRightMouseSelectionAllowed() {
			return selection.size() <= 1;
		}

		@Override
		public void focusGained(FocusEvent e) {

			Component component = e.getComponent();
			PageView pageView = AerialistUtils.getPageView(component);
			if (pageView != null) {

				if (KeyboardUtils.isControlDown()) {

					if (selection.contains(component)) {
						selection.remove(component);
					} else {
						selection.add(component);
					}

					List<Component> removeComponents = new ArrayList<>();
					if (component instanceof PageView) {
						for (Component c : selection) {
							if (!(c instanceof PageView)) {
								removeComponents.add(c);
							}
						}
					} else {
						for (Component c : selection) {
							if (c instanceof PageView) {
								removeComponents.add(c);
							}
						}
					}
					selection.removeAll(removeComponents);

					focusContentContainer();

				} else {

					selection.clear();
					selection.add(component);

				}

				selection.fireSelectionChanged();

				repaint();

			}

		}

		@Override
		public void focusLost(FocusEvent e) {

		}

		@Override
		public void caretUpdate(CaretEvent e) {
			context.getTextFormatTools().textSelectionChanged();
		}

		@Override
		public void undoableEditHappened(UndoableEdit edit) {
			DocumentEditorPanel.this.undoableEditHappened(edit);
		}

		@Override
		public void refresh(Component component) {
			DocumentEditorPanel.this.refresh(component);
		}

		@Override
		public Color getDefaultForeground() {
			return Color.black;
		}

		@Override
		public void paint(Graphics g) {

			Graphics2D g2d = (Graphics2D) g;

			view.drawEditorBackground(g2d, this);

			super.paint(g);

			view.drawEditorForeground(g2d, this);
			view.drawSelectionRectangle(g2d);

		}

		public void editHeaderView(PageView replacePageView) {

			if (!editingHeader && !editingFooter && replacedPageView == null) {

				editingHeader = true;

				for (int i = 0; i < getComponentCount(); i++) {

					if (getComponent(i) == replacePageView) {

						replacedPageView = replacePageView;
						replacedPageViewIndex = i;

						remove(i);

						PageView headerView = getHeaderView();
						if (headerView == null) {
							headerView = createPageView(new Page(replacePageView.getWidth(), replacePageView.getHeight()), pageContainer);
							setHeaderView(headerView);
						}

						add(headerView, i);

						validate();
						repaint();

						break;

					}

				}

				if (replacedPageView == null) {
					editingHeader = false;
				}

				context.getMainPanel().updateEditMenu();

			}

		}

		public void editFooterView(PageView replacePageView) {

			if (!editingHeader && !editingFooter && replacedPageView == null) {

				editingFooter = true;

				for (int i = 0; i < getComponentCount(); i++) {

					if (getComponent(i) == replacePageView) {

						replacedPageView = replacePageView;
						replacedPageViewIndex = i;

						remove(i);

						PageView footerView = getFooterView();
						if (footerView == null) {
							footerView = createPageView(new Page(replacePageView.getWidth(), replacePageView.getHeight()), pageContainer);
							setFooterView(footerView);
						}

						add(footerView, i);

						validate();
						repaint();

						break;

					}

				}

				if (replacedPageView == null) {
					editingFooter = false;
				}

				context.getMainPanel().updateEditMenu();

			}

		}

		public void stopEditingHeaderView() {

			if (editingHeader && replacedPageView != null) {

				remove(replacedPageViewIndex);
				add(replacedPageView, replacedPageViewIndex);

				updateLayers();

				validate();
				repaint();

				replacedPageView = null;
				replacedPageViewIndex = -1;
				editingHeader = false;

				context.getMainPanel().updateEditMenu();

			}

		}

		public void stopEditingFooterView() {

			if (editingFooter && replacedPageView != null) {

				remove(replacedPageViewIndex);
				add(replacedPageView, replacedPageViewIndex);

				updateLayers();

				validate();
				repaint();

				replacedPageView = null;
				replacedPageViewIndex = -1;
				editingFooter = false;

				context.getMainPanel().updateEditMenu();

			}

		}

		public boolean isEditingHeader() {
			return editingHeader;
		}

		public boolean isEditingFooter() {
			return editingFooter;
		}

	}

}
