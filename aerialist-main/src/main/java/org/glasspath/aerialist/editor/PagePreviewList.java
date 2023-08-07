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
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.glasspath.aerialist.swing.view.PageContainer;
import org.glasspath.aerialist.swing.view.PageView;
import org.glasspath.common.swing.theme.Theme;

public class PagePreviewList extends JList<PageView> {

	private final PageContainer pageContainer;
	private final PagePreviewListModel model;

	public PagePreviewList(PageContainer pageContainer, JScrollPane scrollPane) {

		this.pageContainer = pageContainer;

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
				if (index != pageContainer.getPageIndex()) {

					pageContainer.setPageIndex(index);

					pageContainer.loadPageViews();
					pageContainer.repaint();

					if (scrollPane != null) {
						scrollPane.getVerticalScrollBar().setValue(0);
						scrollPane.getHorizontalScrollBar().setValue(0);
					}

				}

			}
		});

	}

	public void refresh() {

		model.refresh();

		setSelectedIndex(pageContainer.getPageIndex());

	}

	protected class PagePreviewListModel extends AbstractListModel<PageView> {

		public PagePreviewListModel() {

		}

		@Override
		public int getSize() {
			return pageContainer.getPageViews().size();
		}

		@Override
		public PageView getElementAt(int index) {
			return pageContainer.getPageViews().get(index);
		}

		protected void refresh() {
			fireContentsChanged(this, 0, getSize()); // TODO: This is a quick hack..
		}

	}

	protected class PagePreviewListCellRenderer extends DefaultListCellRenderer {

		private boolean selected = false;

		public PagePreviewListCellRenderer() {

			setOpaque(false);
			setPreferredSize(new Dimension(200, 115));

		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			setText(""); //$NON-NLS-1$

			selected = isSelected;

			return this;

		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			Rectangle rect = new Rectangle(50, 15, 150, 95);

			g2d.setColor(Color.white);
			g2d.fill(rect);

			if (selected) {
				g2d.setColor(DocumentEditorView.SELECTION_COLOR);
				g2d.draw(rect);
			}

		}

	}

}
