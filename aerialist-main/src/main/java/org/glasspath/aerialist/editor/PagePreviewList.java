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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractListModel;
import javax.swing.CellRendererPane;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.glasspath.aerialist.Aerialist;
import org.glasspath.aerialist.swing.view.PageView;
import org.glasspath.common.swing.SwingUtils;
import org.glasspath.common.swing.theme.Theme;

public abstract class PagePreviewList extends JList<PageView> {

	public static final boolean TODO_PERFORM_DOUBLE_VALIDATION_HACK = true;
	public static final double DEFAULT_PREVIEW_SCALE = 0.2;

	private final PagePreviewListModel model;
	private final CellRendererPane cellRendererPane;
	private final Map<PageView, PreviewImage> previewImageCache = new HashMap<>();

	public PagePreviewList() {

		if (Theme.isDark()) {
			setBackground(new Color(54, 56, 58));
		} else {
			setBackground(new Color(245, 245, 245));
		}

		model = new PagePreviewListModel();
		setModel(model);

		setCellRenderer(new PagePreviewListCellRenderer());

		addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {

				int index = getSelectedIndex();
				if (index != getPageIndex()) {
					setPageIndex(index);
				}

			}
		});

		// TODO: Do we really need a CellRendererPane? and can we add it like this?
		cellRendererPane = new CellRendererPane();
		add(cellRendererPane);

	}

	@Override
	public void paint(Graphics g) {
		g.clearRect(0, 0, getWidth(), getHeight());
		super.paint(g);
	}

	public abstract int getPageIndex();

	public abstract void setPageIndex(int index);

	public abstract List<PageView> getPageViews();

	protected PreviewImage getPreviewImage(int index) {

		PreviewImage previewImage = null;

		PageView pageView = getPageViews().get(index);
		if (pageView != null) {

			previewImage = previewImageCache.get(pageView);

			if (previewImage == null || previewImage.created < pageView.getLastUpdate()) {

				try {

					BufferedImage image = new BufferedImage(pageView.getWidth(), pageView.getHeight(), BufferedImage.TYPE_INT_ARGB);
					Graphics2D g2d = image.createGraphics();

					if (pageView.getParent() == null) {

						// TODO: After many attempts this was the first way to get the layout of tables to update correctly..
						if (TODO_PERFORM_DOUBLE_VALIDATION_HACK) {
							cellRendererPane.add(pageView);
							pageView.validate();
							cellRendererPane.remove(pageView);
						}

						// TODO: Do we really need a CellRendererPane?
						cellRendererPane.add(pageView);
						pageView.validate(); // TODO: Tables are not updated correctly.. (for now fixed with TODO_PERFORM_DOUBLE_VALIDATION_HACK)
						pageView.print(g2d);
						cellRendererPane.remove(pageView);

					} else {
						pageView.print(g2d);
					}

					g2d.dispose();

					previewImage = new PreviewImage();
					previewImage.image = image.getScaledInstance((int) (pageView.getWidth() * DEFAULT_PREVIEW_SCALE), (int) (pageView.getHeight() * DEFAULT_PREVIEW_SCALE), Image.SCALE_SMOOTH);
					previewImage.created = System.currentTimeMillis();
					previewImageCache.put(pageView, previewImage);

				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		}

		return previewImage;

	}

	public void refresh() {
		refresh(false);
	}

	public void refresh(boolean clearCache) {

		if (clearCache) {
			previewImageCache.clear();
		} else {

			List<PageView> removePreviewImages = new ArrayList<>();
			for (PageView pageView : previewImageCache.keySet()) {
				if (!getPageViews().contains(pageView)) {
					removePreviewImages.add(pageView);
				}
			}
			previewImageCache.keySet().removeAll(removePreviewImages);

		}

		model.refresh();

		setSelectedIndex(getPageIndex());

	}

	protected class PagePreviewListModel extends AbstractListModel<PageView> {

		public PagePreviewListModel() {

		}

		@Override
		public int getSize() {
			return getPageViews().size();
		}

		@Override
		public PageView getElementAt(int index) {
			return getPageViews().get(index);
		}

		protected void refresh() {
			fireContentsChanged(this, 0, getSize()); // TODO: This is a quick hack..
		}

	}

	protected class PagePreviewListCellRenderer extends DefaultListCellRenderer {

		private int index = 0;
		private PreviewImage previewImage = null;

		public PagePreviewListCellRenderer() {

			setOpaque(false);
			setPreferredSize(new Dimension(190, 182));

		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			setText(""); //$NON-NLS-1$

			this.index = index;
			this.previewImage = getPreviewImage(index);

			if (previewImage != null && previewImage.image != null) {
				setPreferredSize(new Dimension(previewImage.image.getWidth(null) + 70, previewImage.image.getHeight(null) + 14));
			}

			return this;

		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			String s = "" + (index + 1); //$NON-NLS-1$
			Rectangle2D fontRect = g2d.getFontMetrics().getStringBounds(s, g2d);
			g2d.setColor(new Color(175, 175, 175));
			SwingUtils.drawString(this, g2d, s, 35 - (int) fontRect.getWidth(), 11);

			if (Aerialist.TODO_TEST_SHEET_MODE) {
				g2d.setColor(Theme.isDark() ? new Color(75, 75, 75) : new Color(254, 254, 254));
			} else {
				g2d.setColor(Color.white);
			}

			Rectangle rect;
			if (previewImage != null && previewImage.image != null) {

				rect = new Rectangle(45, 0, previewImage.image.getWidth(null), previewImage.image.getHeight(null));
				g2d.fill(rect);

				g2d.drawImage(previewImage.image, 45, 0, null);

			} else {
				rect = new Rectangle(45, 0, 115, 155);
				g2d.fill(rect);
			}

			if (index == getPageIndex()) {
				g2d.setColor(DocumentEditorView.SELECTION_COLOR);
				g2d.draw(rect);
			}

			g2d.dispose();

		}

	}

	private static class PreviewImage {

		private Image image = null;
		private long created = 0L;

	}

}
