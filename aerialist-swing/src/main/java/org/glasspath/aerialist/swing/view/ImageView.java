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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.glasspath.aerialist.Alignment;
import org.glasspath.aerialist.Border;
import org.glasspath.aerialist.FitPolicy;
import org.glasspath.aerialist.HeightPolicy;
import org.glasspath.aerialist.Image;
import org.glasspath.aerialist.Radius;
import org.glasspath.aerialist.YPolicy;

public class ImageView extends JComponent implements ISwingElementView<Image>, IScalableView {

	private final ISwingViewContext viewContext;
	private YPolicy yPolicy = YPolicy.DEFAULT;
	private HeightPolicy heightPolicy = HeightPolicy.DEFAULT;
	private Color backgroundColor = null;
	private Radius radius = new Radius();
	private final List<Border> borders = new ArrayList<>();
	private String src = ""; //$NON-NLS-1$
	private float scale = 1.0F;
	private Alignment alignment = Alignment.DEFAULT;
	private FitPolicy fitPolicy = FitPolicy.DEFAULT;

	private BufferedImage image = null;

	public ImageView(ISwingViewContext viewContext) {

		this.viewContext = viewContext;

		ISwingViewContext.installSelectionHandler(this, viewContext);

	}

	@Override
	public void init(Image element) {

		yPolicy = YPolicy.get(element.getYPolicy());
		heightPolicy = HeightPolicy.get(element.getHeightPolicy());

		setBackgroundColor(ColorUtils.fromHex(element.getBackground()));

		radius.parse(element.getRadius());

		borders.clear();
		for (Border border : element.getBorders()) {
			borders.add(new Border(border));
		}

		src = element.getSrc();
		scale = element.getScale();
		alignment = Alignment.get(element.getAlignment());
		fitPolicy = FitPolicy.get(element.getFit());

		image = viewContext.getMediaCache().getImage(src);

	}

	public void setImage(String src, BufferedImage image) {
		this.src = src;
		this.image = image;
	}

	public String getSrc() {
		return src;
	}

	@Override
	public float getScale() {
		return scale;
	}

	@Override
	public void setScale(float scale) {
		this.scale = scale;
	}

	@Override
	public Alignment getAlignment() {
		return alignment;
	}

	@Override
	public void setAlignment(Alignment alignment) {
		this.alignment = alignment;
	}

	@Override
	public FitPolicy getFitPolicy() {
		return fitPolicy;
	}

	@Override
	public void setFitPolicy(FitPolicy fitPolicy) {
		this.fitPolicy = fitPolicy;
	}

	@Override
	public YPolicy getYPolicy() {
		return yPolicy;
	}

	@Override
	public void setYPolicy(YPolicy yPolicy) {
		this.yPolicy = yPolicy;
	}

	@Override
	public HeightPolicy getHeightPolicy() {
		return heightPolicy;
	}

	@Override
	public void setHeightPolicy(HeightPolicy heightPolicy) {
		this.heightPolicy = heightPolicy;
	}

	@Override
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	@Override
	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	@Override
	public Radius getRadius() {
		return radius;
	}

	@Override
	public void setRadius(Radius radius) {
		this.radius = radius;
	}

	@Override
	public List<Border> getBorders() {
		return borders;
	}

	@Override
	public Dimension getPreferredSize() {

		Dimension size = super.getPreferredSize();

		if (image != null) {

			if (heightPolicy == HeightPolicy.AUTO) {

				if (fitPolicy == FitPolicy.WIDTH) {
					size.height = (int) (image.getHeight() * ((double) getWidth() / (double) image.getWidth()));
				} else {
					size.height = image.getHeight();
				}

			}

		}

		return size;

	}

	@Override
	public Image toElement() {

		Image image = new Image();
		ISwingElementView.copyProperties(this, image);
		image.setSrc(src);
		image.setScale(scale);
		image.setAlignment(alignment.stringValue);
		image.setFit(fitPolicy.stringValue);

		return image;

	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		Rectangle rect = new Rectangle(0, 0, getWidth(), getHeight());
		PaintUtils.paintBackground(g2d, rect, backgroundColor, borders, radius);
		PaintUtils.paintImage(g2d, getWidth(), getHeight(), image, scale, alignment, fitPolicy);
		PaintUtils.paintBorders(g2d, borders, rect, radius);

	}

}
