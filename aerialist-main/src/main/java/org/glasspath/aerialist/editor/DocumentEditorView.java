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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import javax.swing.CellRendererPane;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.glasspath.aerialist.Aerialist;
import org.glasspath.aerialist.Document;
import org.glasspath.aerialist.Margin;
import org.glasspath.aerialist.swing.view.FooterPageView;
import org.glasspath.aerialist.swing.view.LayeredPageView;
import org.glasspath.aerialist.swing.view.PageContainer;
import org.glasspath.aerialist.swing.view.PageView;
import org.glasspath.common.swing.color.ColorUtils;
import org.glasspath.common.swing.graphics.NinePatch;
import org.glasspath.common.swing.theme.Theme;

public class DocumentEditorView extends EditorView<DocumentEditorPanel> {

	public static final Color GRID_COLOR = new Color(200, 200, 200);
	public static final Color GUIDE_COLOR = new Color(150, 195, 255, 75);
	public static final Color HEADER_FOOTER_GUIDE_COLOR = new Color(150, 195, 255, 150);

	private final NinePatch shadow = new NinePatch(new ImageIcon(getClass().getClassLoader().getResource("org/glasspath/common/swing/graphics/shadow.png")).getImage(), 10, 10); //$NON-NLS-1$
	private final CellRendererPane cellRendererPane = new CellRendererPane();

	protected BufferedImage bgImage = null;

	public DocumentEditorView(DocumentEditorPanel context) {
		super(context);
	}

	@Override
	public void drawEditorBackground(Graphics2D g2d, JPanel pageContainer, boolean editable) {

		if (Theme.isDark()) {
			g2d.setColor(new Color(48, 50, 53));
		} else {
			g2d.setColor(new Color(242, 242, 242));
		}
		g2d.fillRect(0, 0, pageContainer.getWidth(), pageContainer.getHeight());

		Component component;
		PageView pageView;
		Rectangle bounds;
		for (int i = 0; i < pageContainer.getComponentCount(); i++) {

			component = pageContainer.getComponent(i);
			if (component instanceof PageView) {

				pageView = (PageView) component;

				bounds = component.getBounds();

				shadow.paintNinePatch(g2d, bounds.x - 7, bounds.y - 7, bounds.width + 14, bounds.height + 14);

				if (Aerialist.TODO_TEST_SHEET_MODE) {

					if (context.getEditorContext() instanceof DocumentEditorContext && ((DocumentEditorContext) context.getEditorContext()).getBackgroundImage() != null) {

						BufferedImage backgroundImage = ((DocumentEditorContext) context.getEditorContext()).getBackgroundImage();

						// TODO: For now we only fit the width of the image
						if (backgroundImage.getWidth() != pageView.getWidth()) {

							double scale = (double) pageView.getWidth() / (double) backgroundImage.getWidth();

							AffineTransform originalTransform = g2d.getTransform();
							g2d.scale(scale, scale);
							g2d.drawImage(backgroundImage, (int) (bounds.x / scale), (int) (bounds.y / scale), null);
							g2d.setTransform(originalTransform);

						} else {
							g2d.drawImage(backgroundImage, bounds.x, bounds.y, null);
						}

						Composite originalComposite = g2d.getComposite();
						g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25F));
						g2d.setColor(Color.black);
						g2d.fill(bounds);
						g2d.setComposite(originalComposite);

					} else if (context.isGridVisible()) {

						g2d.drawImage(getBgImage(pageView), bounds.x, bounds.y, null);

						if (Theme.isDark()) {
							g2d.setColor(Color.black);
							g2d.draw(bounds);
						}

					} else {
						g2d.setColor(Theme.isDark() ? ColorUtils.GRAY_75 : ColorUtils.GRAY_250);
						g2d.fill(bounds);
					}

				} else {
					g2d.setColor(Color.white);
					g2d.fill(bounds);
				}

				if (context.getPageContainer().isEditingHeader()) {

					if (pageView == context.getPageContainer().getHeaderPageView()) {

						Rectangle bolowHeaderBounds = new Rectangle(bounds);
						bolowHeaderBounds.y += context.getPageContainer().getHeaderHeight();
						bolowHeaderBounds.height -= context.getPageContainer().getHeaderHeight();

						g2d.setColor(Aerialist.TODO_TEST_SHEET_MODE ? new Color(0, 0, 0, 35) : new Color(248, 248, 248));
						g2d.fill(bolowHeaderBounds);

					}

				} else if (context.getPageContainer().isEditingFooter()) {

					if (pageView == context.getPageContainer().getFooterPageView()) {

						Rectangle aboveFooterBounds = new Rectangle(bounds);
						aboveFooterBounds.height -= context.getPageContainer().getFooterHeight();

						g2d.setColor(Aerialist.TODO_TEST_SHEET_MODE ? new Color(0, 0, 0, 35) : new Color(248, 248, 248));
						g2d.fill(aboveFooterBounds);

					}

				} else {

					if (pageView instanceof LayeredPageView) {
						for (PageView layerView : ((LayeredPageView) pageView).getLayers()) {
							drawLayerView(g2d, pageView, layerView, editable);
						}
					}

				}

				if (context.isGridVisible() && !Aerialist.TODO_TEST_SHEET_MODE) {
					drawGrid(g2d, pageView);
				}

				if (context.isGuidesVisible()) {
					drawGuides(g2d, context.getPageContainer(), pageView);
				}

			}

		}

	}

	@Override
	public void drawEditorForeground(Graphics2D g2d, JPanel pageContainer, boolean editable) {

		Component component;
		Rectangle bounds;
		for (int i = 0; i < pageContainer.getComponentCount(); i++) {

			component = pageContainer.getComponent(i);
			if (component instanceof PageView) {

				bounds = component.getBounds();

				if (!Theme.isDark()) {
					g2d.setColor(ColorUtils.GRAY_192);
					g2d.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
				}

				if (editable && context.selection.contains(component)) {
					g2d.setStroke(PAGE_SELECTION_STROKE);
					g2d.setColor(PAGE_SELECTION_COLOR);
					g2d.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
				}

			}

		}

	}

	@Override
	public void drawLayerView(Graphics2D g2d, PageView pageView, PageView layerView, boolean editable) {

		Dimension size = layerView.getPreferredSize();

		int x = pageView.getX() + ((pageView.getWidth() - size.width) / 2);
		int y = pageView.getY();

		// Align footer to bottom
		if (layerView instanceof FooterPageView) {
			y += pageView.getHeight() - size.height;
		}

		Shape oldClip = g2d.getClip();
		g2d.setClip(pageView.getX(), pageView.getY(), pageView.getWidth(), pageView.getHeight());

		Composite oldComposite = g2d.getComposite();
		if (editable) {
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25F));
		}

		cellRendererPane.add(layerView);
		context.getPageContainer().add(cellRendererPane);
		cellRendererPane.paintComponent(g2d, layerView, context.getPageContainer(), x, y, size.width, size.height, true);
		context.getPageContainer().remove(cellRendererPane);
		cellRendererPane.remove(layerView);

		g2d.setComposite(oldComposite);
		g2d.setClip(oldClip);

	}

	protected BufferedImage getBgImage(PageView pageView) {

		int w = pageView.getWidth();
		int h = pageView.getHeight();

		if (bgImage == null || bgImage.getWidth() != w || bgImage.getHeight() != h) {

			// TODO: For now we create a transparency background as seen in image editors

			bgImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
			WritableRaster raster = bgImage.getRaster();

			int color1, color2;
			if (Theme.isDark()) {
				color1 = 55;
				color2 = 75;
			} else {
				color1 = 251;
				color2 = 233;
			}

			for (int x = 0; x < w; x++) {

				for (int y = 0; y < h; y++) {

					if (x % 20 < 10) {
						if (y % 20 < 10) {
							raster.setSample(x, y, 0, color1);
						} else {
							raster.setSample(x, y, 0, color2);
						}
					} else {
						if (y % 20 > 10) {
							raster.setSample(x, y, 0, color1);
						} else {
							raster.setSample(x, y, 0, color2);
						}
					}

				}

			}

			bgImage.setAccelerationPriority(1.0F);

		}

		return bgImage;

	}

	protected void drawGrid(Graphics2D g2d, PageView pageView) {

		g2d.setColor(GRID_COLOR);

		for (int x = context.getGridSpacing(); x < pageView.getWidth() - (context.getGridSpacing() / 2); x += context.getGridSpacing()) {
			for (int y = context.getGridSpacing(); y < pageView.getHeight() - (context.getGridSpacing() / 2); y += context.getGridSpacing()) {
				g2d.fillRect(pageView.getX() + x, pageView.getY() + y, 1, 1);
			}
		}

	}

	protected void drawGuides(Graphics2D g2d, PageContainer pageContainer, PageView pageView) {

		int top = Document.DEFAULT_MARGIN_TOP;
		int right = Document.DEFAULT_MARGIN_RIGHT;
		int bottom = Document.DEFAULT_MARGIN_BOTTOM;
		int left = Document.DEFAULT_MARGIN_LEFT;

		Margin margin = null;

		if (pageContainer.getMargin() != null) {
			margin = new Margin(pageContainer.getMargin());
		} else if (pageView.getMargin() != null) {
			margin = new Margin(pageView.getMargin());
		}

		if (margin != null) {
			top = margin.top;
			right = margin.right;
			bottom = margin.bottom;
			left = margin.left;
		}

		int x = pageView.getX();
		int y = pageView.getY();
		int w = pageView.getWidth();
		int h = pageView.getHeight();

		g2d.setColor(GUIDE_COLOR);
		g2d.drawLine(x + left, y, x + left, y + h);
		g2d.drawLine(x + w - right, y, x + w - right, y + h);
		g2d.drawLine(x, y + top, x + w, y + top);
		g2d.drawLine(x, y + h - bottom, x + w, y + h - bottom);

		top = pageContainer.getHeaderHeight();
		bottom = pageContainer.getFooterHeight();

		g2d.setColor(HEADER_FOOTER_GUIDE_COLOR);
		g2d.drawLine(x, y + top, x + w, y + top);
		g2d.drawLine(x, y + h - bottom, x + w, y + h - bottom);

	}

}
