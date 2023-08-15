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
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
import javax.swing.undo.UndoableEdit;

import org.glasspath.aerialist.Aerialist;
import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.Page;
import org.glasspath.aerialist.editor.actions.ActionUtils;
import org.glasspath.aerialist.editor.actions.CopyAction;
import org.glasspath.aerialist.editor.actions.PasteAction;
import org.glasspath.aerialist.layout.ILayoutContext.ExportPhase;
import org.glasspath.aerialist.media.MediaCache;
import org.glasspath.aerialist.swing.view.FooterPageView;
import org.glasspath.aerialist.swing.view.HeaderPageView;
import org.glasspath.aerialist.swing.view.ISwingViewContext;
import org.glasspath.aerialist.swing.view.PageContainer;
import org.glasspath.aerialist.swing.view.PageContainer.PageListener;
import org.glasspath.aerialist.swing.view.PageView;
import org.glasspath.aerialist.swing.view.TableCellView;
import org.glasspath.aerialist.text.font.FontCache;
import org.glasspath.common.swing.border.HidpiMatteBorder;
import org.glasspath.common.swing.keyboard.KeyboardUtils;
import org.glasspath.common.swing.search.UISearchHandler;
import org.glasspath.common.swing.selection.SelectionListener;
import org.glasspath.common.swing.splitpane.InvisibleSplitPane;
import org.glasspath.common.swing.theme.Theme;
import org.glasspath.common.swing.undo.DefaultUndoManager.UndoManagerListener;

public class DocumentEditorPanel extends EditorPanel<DocumentEditorPanel> {

	public static final int DEFAULT_PAGE_PREVIEW_WIDTH = 200;
	public static final int MAX_PAGE_PREVIEW_WIDTH = 500;

	private final Aerialist context;
	protected final DocumentEditorView view;
	protected final MouseOperationHandler<DocumentEditorPanel> mouseOperationHandler;
	protected final EditorPageContainer pageContainer;
	private final JScrollPane pageContainerScrollPane;
	private final PagePreviewList pagePreviewList;
	private final JScrollPane pagePreviewScrollPane;
	private final InvisibleSplitPane mainSplitPane;
	private final CopyAction copyAction;
	private final PasteAction pasteAction;

	private boolean mainSplitPaneInited = false;
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

		pageContainerScrollPane = new JScrollPane(pageContainer);
		pageContainerScrollPane.setBorder(BorderFactory.createEmptyBorder());
		pageContainerScrollPane.getVerticalScrollBar().setUnitIncrement(25);

		pagePreviewList = new PagePreviewList() {

			@Override
			public int getPageIndex() {
				return pageContainer.getPageIndex();
			}

			@Override
			public void setPageIndex(int index) {
				DocumentEditorPanel.this.setPageIndex(index, false);
			}

			@Override
			public List<PageView> getPageViews() {
				return pageContainer.getPageViews();
			}
		};
		pagePreviewList.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
		pageContainer.addPageListener(new PageListener() {

			@Override
			public void pagesLoaded() {
				refresh(false);

				if (!mainSplitPaneInited) {

					if (pageContainer.getPageMode() == PageContainer.PAGE_MODE_SINGLE) {

						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								mainSplitPane.setDividerLocation(getPreferredPagePreviewWidth());
							}
						});

					}

					mainSplitPaneInited = true;

				}

			}

			@Override
			public void pagesAdded() {
				refresh(true);
			}

			@Override
			public void pagesRemoved() {
				refresh(true);
			}

			private void refresh(boolean refreshPageContainer) {

				if (pageContainer.getPageMode() == PageContainer.PAGE_MODE_SINGLE) {

					if (refreshPageContainer) {

						int index = pageContainer.getPageIndex();
						if (index >= pageContainer.getPageViews().size()) {
							index = pageContainer.getPageViews().size() - 1;
						}
						if (index < 0) {
							index = 0;
						}

						setPageIndex(index, false);

					}

					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							pagePreviewList.refresh();
						}
					});

				}

			}
		});

		pagePreviewScrollPane = new JScrollPane(pagePreviewList);
		pagePreviewScrollPane.setBorder(BorderFactory.createEmptyBorder());
		pagePreviewScrollPane.getVerticalScrollBar().setUnitIncrement(25);
		pagePreviewScrollPane.setBorder(new HidpiMatteBorder(new Insets(0, 0, 0, 1), Theme.isDark() ? new Color(42, 42, 42) : new Color(225, 225, 225)));

		mainSplitPane = new InvisibleSplitPane();
		mainSplitPane.setLeftComponent(pagePreviewScrollPane);

		pageContainerScrollPane.addMouseWheelListener(new MouseWheelListener() {

			private long lastScroll = 0;

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {

				if (pageContainer.getPageMode() == PageContainer.PAGE_MODE_SINGLE) {

					if (pageContainer.isEditingHeader()) {
						// Don't automatically scroll to other page when editing header
					} else if (pageContainer.isEditingFooter()) {
						// Don't automatically scroll to other page when editing footer
					} else if (e.getWheelRotation() < 0 && pageContainerScrollPane.getVerticalScrollBar().getValue() == 0) {

						if (System.currentTimeMillis() > lastScroll + 500) { // TODO: Move minimum interval between scrolls to constant

							if (pageContainer.getPageIndex() > 0) {

								setPageIndex(pageContainer.getPageIndex() - 1, true);

								pagePreviewList.refresh();

								lastScroll = System.currentTimeMillis();

							}

						}

					} else if (e.getWheelRotation() > 0 && pageContainerScrollPane.getVerticalScrollBar().getValue() + pageContainerScrollPane.getVerticalScrollBar().getVisibleAmount() >= pageContainerScrollPane.getVerticalScrollBar().getMaximum()) {

						if (System.currentTimeMillis() > lastScroll + 500) { // TODO: Move minimum interval between scrolls to constant

							if (pageContainer.getPageIndex() < pageContainer.getPageViews().size() - 1) {

								setPageIndex(pageContainer.getPageIndex() + 1, false);

								pagePreviewList.refresh();

								lastScroll = System.currentTimeMillis();

							}

						}

					} else {
						lastScroll = System.currentTimeMillis();
					}

				}

			}
		});

		if (pageContainer.getPageMode() == PageContainer.PAGE_MODE_SINGLE) {

			mainSplitPane.setRightComponent(pageContainerScrollPane);
			add(mainSplitPane, BorderLayout.CENTER);

			mainSplitPane.setDividerLocation(DEFAULT_PAGE_PREVIEW_WIDTH);

		} else {
			add(pageContainerScrollPane, BorderLayout.CENTER);
		}

		copyAction = new CopyAction(this);
		pasteAction = new PasteAction(this, copyAction);

		selection.addSelectionListener(new SelectionListener() {

			@Override
			public void selectionChanged() {
				context.getMainPanel().updateEditMenu();
				context.getTextFormatTools().textSelectionChanged();
			}
		});

		getUndoManager().addListener(new UndoManagerListener() {

			@Override
			public void editAdded(UndoableEdit edit) {
				update(false);
			}

			@Override
			public void undoPerformed() {
				update(true);
			}

			@Override
			public void redoPerformed() {
				update(true);
			}

			private void update(boolean updateAll) {

				context.setContentChanged(true);

				// TODO: This is a quick hack, we should probably implement this in the undo/redo mechanism,
				// for now this is only used for refreshing page preview images in single page layout mode..
				if (getPageMode() == PageContainer.PAGE_MODE_SINGLE) {

					if (updateAll) {

						// Undo/redo might be performed while other page is visible, so for now update all..
						for (PageView pageView : pageContainer.getPageViews()) {
							pageView.setLastUpdate(System.currentTimeMillis());
						}

					} else if (pageContainer.getPageIndex() >= 0 && pageContainer.getPageIndex() < pageContainer.getPageViews().size()) {
						pageContainer.getPageViews().get(pageContainer.getPageIndex()).setLastUpdate(System.currentTimeMillis());
					}

				}

			}
		});

		UISearchHandler searchHandler = new UISearchHandler(pageContainer) {

			@Override
			public void textFound(JTextComponent component, String text, int index) {

				selection.clear();
				selection.select(component);

				component.select(index, index + text.length());

				repaint();

			}
		};

		// TODO? Search handler doesn't clear highlights immediately, so for now we use
		// custom HighlightPainters to prevent highlights from being exported to PDF..
		searchHandler.setOccurenceHighlighter(new DefaultHighlightPainter(UISearchHandler.OCCURENCE_COLOR) {

			@Override
			public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
				if (pageContainer.getExportPhase() != ExportPhase.EXPORT) {
					return super.paintLayer(g, offs0, offs1, bounds, c, view);
				} else {
					return null;
				}
			}
		});
		searchHandler.setSearchHighlighter(new DefaultHighlightPainter(UISearchHandler.HIGHLIGHT_COLOR) {

			@Override
			public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
				if (pageContainer.getExportPhase() != ExportPhase.EXPORT) {
					return super.paintLayer(g, offs0, offs1, bounds, c, view);
				} else {
					return null;
				}
			}
		});

		setSearchHandler(searchHandler);

	}

	public Aerialist getContext() {
		return context;
	}

	@Override
	public Frame getFrame() {
		return context.getFrame();
	}

	@Override
	public Preferences getPreferences() {
		return context.getPreferences();
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

	public EditorPageContainer getPageContainer() {
		return pageContainer;
	}

	public int getPageMode() {
		return pageContainer.getPageMode();
	}

	public void setPageMode(int pageMode) {

		if (pageMode != pageContainer.getPageMode()) {

			pageContainer.setPageMode(pageMode);

			removeAll();

			if (pageContainer.getPageMode() == PageContainer.PAGE_MODE_SINGLE) {

				mainSplitPane.setRightComponent(pageContainerScrollPane);
				add(mainSplitPane, BorderLayout.CENTER);

				if (mainSplitPaneInited) {
					mainSplitPane.setDividerLocation(getPreferredPagePreviewWidth());
				} else {
					mainSplitPane.setDividerLocation(DEFAULT_PAGE_PREVIEW_WIDTH);
				}

				setPageIndex(pageContainer.getPageIndex(), false);

			} else {

				PageView scrollToPageView;
				if (pageContainer.getPageIndex() >= 0 && pageContainer.getPageIndex() < pageContainer.getPageViews().size()) {
					scrollToPageView = pageContainer.getPageViews().get(pageContainer.getPageIndex());
				} else {
					scrollToPageView = null;
				}

				add(pageContainerScrollPane, BorderLayout.CENTER);

				pageContainer.loadPageViews();

				if (scrollToPageView != null) {

					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							pageContainerScrollPane.getVerticalScrollBar().setValue(scrollToPageView.getY() - 20);
						}
					});

				}

			}

			// TODO: Create page mode listener?
			context.getViewTools().updatePageModeComponents();

			invalidate();
			revalidate();
			repaint();

		}

	}

	private int getPreferredPagePreviewWidth() {

		int width = pagePreviewScrollPane.getPreferredSize().width;
		if (width < DEFAULT_PAGE_PREVIEW_WIDTH) {
			width = DEFAULT_PAGE_PREVIEW_WIDTH;
		} else if (width > MAX_PAGE_PREVIEW_WIDTH) {
			width = MAX_PAGE_PREVIEW_WIDTH;
		}

		return width;

	}

	private void setPageIndex(int index, boolean scrollToBottom) {

		pageContainer.setPageIndex(index);
		pageContainer.loadPageViews();

		if (selection.size() > 0) {
			selection.deselectAll();
			pageContainer.requestFocusInWindow();
		}

		if (scrollToBottom) {
			pageContainerScrollPane.getVerticalScrollBar().setValue(Integer.MAX_VALUE);
		} else {
			pageContainerScrollPane.getVerticalScrollBar().setValue(0);
		}

		pageContainer.invalidate();
		pageContainer.validate();
		pageContainer.repaint();

	}

	public JScrollPane getPageContainerScrollPane() {
		return pageContainerScrollPane;
	}

	public CopyAction getCopyAction() {
		return copyAction;
	}

	public PasteAction getPasteAction() {
		return pasteAction;
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
		if (e.getID() == MouseEvent.MOUSE_RELEASED && SwingUtilities.isRightMouseButton(e) && e.getComponent() != null) {

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
	public void refresh(Component component, boolean resetYPolicy, boolean revalidateScrollPane) {

		if (component != null) {
			component.invalidate();
			component.validate();
			// component.repaint();
		}

		if (!pageContainer.isEditingHeader() && pageContainer.getHeaderPageView() != null && (component == pageContainer.getHeaderPageView() || AerialistUtils.getPageView(component) == pageContainer.getHeaderPageView())) {

			// TODO: This is a bit of a hack to get refreshing of the header working during undo/redo
			if (pageContainer.getPageViews().size() > 0) {
				if (pageContainer.getPageMode() == PageContainer.PAGE_MODE_SINGLE && pageContainer.getPageIndex() >= 0 && pageContainer.getPageIndex() < pageContainer.getPageViews().size()) {
					pageContainer.editHeaderPageView(pageContainer.getPageViews().get(pageContainer.getPageIndex()));
				} else {
					pageContainer.editHeaderPageView(pageContainer.getPageViews().get(0));
				}
				pageContainer.stopEditingHeaderView();
			}

		} else if (!pageContainer.isEditingFooter() && pageContainer.getFooterPageView() != null && (component == pageContainer.getFooterPageView() || AerialistUtils.getPageView(component) == pageContainer.getFooterPageView())) {

			// TODO: This is a bit of a hack to get refreshing of the footer working during undo/redo
			if (pageContainer.getPageViews().size() > 0) {
				if (pageContainer.getPageMode() == PageContainer.PAGE_MODE_SINGLE && pageContainer.getPageIndex() >= 0 && pageContainer.getPageIndex() < pageContainer.getPageViews().size()) {
					pageContainer.editFooterPageView(pageContainer.getPageViews().get(pageContainer.getPageIndex()));
				} else {
					pageContainer.editFooterPageView(pageContainer.getPageViews().get(0));
				}
				pageContainer.stopEditingFooterView();
			}

		} else if (component != null) {

			// TODO? For now we repaint the container because when a component was partially outside the
			// page and then moved inside the page the selection rectangle is not repainted correctly..
			pageContainer.repaint();

		} else {

			pageContainer.invalidate();
			pageContainer.validate();
			pageContainer.repaint();

		}

		// If a page was added/removed we need to re-validate the scroll-pane
		if (revalidateScrollPane) {
			pageContainerScrollPane.revalidate();
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

		context.updateToolBars();

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
		pageContainerScrollPane.getVerticalScrollBar().setValue(0);
	}

	@Override
	protected void showMenu(Component component, int x, int y) {

		JMenu menu = new JMenu();

		if (selection.size() > 1) {
			ActionUtils.populateMenu(menu, this, selection);
		} else {
			ActionUtils.populateMenu(menu, this, component, false);
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
			ActionUtils.populateMenu(menu, this, (Component) selection.get(0), true);
		}
	}

	public class EditorPageContainer extends PageContainer {

		private boolean editingHeader = false;
		private boolean editingFooter = false;
		private PageView replacedPageView = null;
		private int replacedPageViewIndex = -1;

		public EditorPageContainer() {
			super();

			if (Aerialist.TODO_TEST_SHEET_MODE) {
				setPageMode(PageContainer.PAGE_MODE_SINGLE);
			}

			setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
			setFocusable(true);
			addFocusListener(new FocusAdapter() {

				@Override
				public void focusGained(FocusEvent e) {

					// TODO: Page container is focused when selecting multiple components, so we don't
					// want to clear the selection here, for now we simply check if control key is down..
					if (!getMouseOperationHandler().isOperationActive() && !KeyboardUtils.controlDown) {
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

			int scrollPosition = pageContainerScrollPane.getVerticalScrollBar().getValue();

			selection.clear();

			super.setPageViews(pageViews);

			loadPageViews();
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {

					pageContainerScrollPane.getVerticalScrollBar().setValue(scrollPosition);
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
		public void viewEventHappened(ViewEvent viewEvent) {
			if (viewEvent.id == ViewEvent.EVENT_NOTHING_COPIED) {
				copyAction.actionPerformed(null);
			}
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
			if (getEditorContext() != null) {
				return getEditorContext().getDefaultForeground();
			} else {
				return Color.black;
			}
		}

		@Override
		public int getContainerPaintFlags() {

			int paintFlags = 0;

			if (isEditable() && getExportPhase() == ExportPhase.IDLE) {

				paintFlags |= ISwingViewContext.CONTAINER_PAINT_FLAG_EDITABLE;

			}

			return paintFlags;

		}

		@Override
		public int getViewPaintFlags(Component view) {

			int paintFlags = 0;

			if (isEditable() && getExportPhase() == ExportPhase.IDLE) {

				paintFlags |= ISwingViewContext.VIEW_PAINT_FLAG_DECORATE_FIELDS;

				if (selection.size() == 1 && selection.get(0) == view) {
					paintFlags |= ISwingViewContext.VIEW_PAINT_FLAG_SELECTED_PRIMARY;
				}

			}

			return paintFlags;

		}

		@Override
		public void paint(Graphics g) {

			Graphics2D g2d = (Graphics2D) g;

			boolean editable = ISwingViewContext.getContainerPaintFlag(this, ISwingViewContext.CONTAINER_PAINT_FLAG_EDITABLE);

			view.drawEditorBackground(g2d, this, editable);

			super.paint(g);

			view.drawEditorForeground(g2d, this, editable);
			view.drawSelectionRectangle(g2d, editable);

		}

		public void editHeaderPageView(PageView replacePageView) {

			if (!editingHeader && !editingFooter && replacedPageView == null) {

				editingHeader = true;

				selection.clear();

				for (int i = 0; i < getComponentCount(); i++) {

					if (getComponent(i) == replacePageView) {

						replacedPageView = replacePageView;
						replacedPageViewIndex = i;

						remove(i);

						HeaderPageView headerPageView = getHeaderPageView();
						if (headerPageView == null) {
							headerPageView = createHeaderPageView(new Page(replacePageView.getWidth(), replacePageView.getHeight()), pageContainer);
							setHeaderPageView(headerPageView);
						}

						add(headerPageView, i);
						selection.add(headerPageView);

						validate();
						repaint();

						break;

					}

				}

				if (replacedPageView == null) {
					editingHeader = false;
				}

				selection.fireSelectionChanged();

			}

		}

		public void editFooterPageView(PageView replacePageView) {

			if (!editingHeader && !editingFooter && replacedPageView == null) {

				editingFooter = true;

				selection.clear();

				for (int i = 0; i < getComponentCount(); i++) {

					if (getComponent(i) == replacePageView) {

						replacedPageView = replacePageView;
						replacedPageViewIndex = i;

						remove(i);

						FooterPageView footerPageView = getFooterPageView();
						if (footerPageView == null) {
							footerPageView = createFooterPageView(new Page(replacePageView.getWidth(), replacePageView.getHeight()), pageContainer);
							setFooterPageView(footerPageView);
						}

						add(footerPageView, i);
						selection.add(footerPageView);

						validate();
						repaint();

						break;

					}

				}

				if (replacedPageView == null) {
					editingFooter = false;
				}

				selection.fireSelectionChanged();

			}

		}

		public void stopEditingHeaderView() {

			if (editingHeader && replacedPageView != null) {

				selection.clear();

				remove(replacedPageViewIndex);
				add(replacedPageView, replacedPageViewIndex);

				updateLayers();

				validate();
				repaint();

				replacedPageView = null;
				replacedPageViewIndex = -1;
				editingHeader = false;

				selection.fireSelectionChanged();

			}

		}

		public void stopEditingFooterView() {

			if (editingFooter && replacedPageView != null) {

				selection.clear();

				remove(replacedPageViewIndex);
				add(replacedPageView, replacedPageViewIndex);

				updateLayers();

				validate();
				repaint();

				replacedPageView = null;
				replacedPageViewIndex = -1;
				editingFooter = false;

				selection.fireSelectionChanged();

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
