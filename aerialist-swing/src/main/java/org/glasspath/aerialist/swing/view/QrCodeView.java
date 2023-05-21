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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.glasspath.aerialist.Alignment;
import org.glasspath.aerialist.Border;
import org.glasspath.aerialist.FitPolicy;
import org.glasspath.aerialist.HeightPolicy;
import org.glasspath.aerialist.QrCode;
import org.glasspath.aerialist.Radius;
import org.glasspath.aerialist.YPolicy;

import io.nayuki.qrcodegen.QrSegment;

public class QrCodeView extends TextView implements ISwingElementView<QrCode>, IScalableView {

	private YPolicy yPolicy = YPolicy.DEFAULT;
	private HeightPolicy heightPolicy = HeightPolicy.DEFAULT;
	private Color backgroundColor = null;
	private Radius radius = new Radius();
	private final List<Border> borders = new ArrayList<>();
	private float scale = 1.0F;
	private FitPolicy fitPolicy = FitPolicy.DEFAULT;

	private BufferedImage image = null;
	private boolean imageValid = false;

	public QrCodeView(ISwingViewContext viewContext) {
		super(viewContext);

		// TODO: Implement padding for QrCode?
		setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

		getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				imageValid = false;
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				imageValid = false;
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				imageValid = false;
			}
		});

	}

	@Override
	public void init(QrCode element) {
		super.init(element);

		yPolicy = YPolicy.get(element.getYPolicy());
		heightPolicy = HeightPolicy.get(element.getHeightPolicy());

		setBackgroundColor(ColorUtils.fromHex(element.getBackground()));

		radius.parse(element.getRadius());

		borders.clear();
		for (Border border : element.getBorders()) {
			borders.add(new Border(border));
		}

		scale = element.getScale();
		fitPolicy = FitPolicy.get(element.getFit());

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
		imageValid = false;
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
	public float getScale() {
		return scale;
	}

	@Override
	public void setScale(float scale) {
		this.scale = scale;
	}

	@Override
	public Alignment getAlignment() {
		return Alignment.get(getTextAlignment());
	}

	@Override
	public void setAlignment(Alignment alignment) {
		setTextAlignment(alignment.intValue);
	}

	@Override
	public FitPolicy getFitPolicy() {
		return fitPolicy;
	}

	@Override
	public void setFitPolicy(FitPolicy fitPolicy) {
		this.fitPolicy = fitPolicy;
	}

	protected void updateImage() {

		if (!imageValid) {

			try {

				List<QrSegment> segments = QrSegment.makeSegments(getText());

				// TODO: Which parameters to use?
				io.nayuki.qrcodegen.QrCode qrCode = io.nayuki.qrcodegen.QrCode.encodeSegments(segments, io.nayuki.qrcodegen.QrCode.Ecc.HIGH, 1, 40, -1, false);

				image = toImage(qrCode, 4, 1, backgroundColor != null ? backgroundColor.getRGB() : Color.white.getRGB(), Color.black.getRGB());

			} catch (Exception e) {
				e.printStackTrace(); // TODO?
			}

			imageValid = true;

		}

	}

	@Override
	public Dimension getPreferredSize() {

		Dimension size = super.getPreferredSize();

		updateImage();

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
	public QrCode toElement() {

		QrCode qrCode = new QrCode();
		ISwingElementView.copyProperties(this, qrCode);
		toText(qrCode);

		qrCode.setScale(scale);
		qrCode.setFit(fitPolicy.stringValue);

		return qrCode;

	}

	@Override
	public void paint(Graphics g) {

		boolean editMode = ISwingViewContext.getViewPaintFlag(viewContext, this, ISwingViewContext.VIEW_PAINT_FLAG_SELECTED_PRIMARY);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		Rectangle rect = new Rectangle(0, 0, getWidth(), getHeight());
		PaintUtils.paintBackground(g2d, rect, backgroundColor, borders, radius);

		updateImage();

		if (image != null) {

			Composite oldComposite = g2d.getComposite();
			if (editMode) {
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05F));
			}

			PaintUtils.paintImage(g2d, getWidth(), getHeight(), image, scale, getAlignment(), fitPolicy);

			g2d.setComposite(oldComposite);

		}

		if (editMode) {
			super.paint(g);
		}

		PaintUtils.paintBorders(g2d, borders, rect, radius);

	}

	// Copied (and slightly modified) from https://github.com/nayuki/QR-Code-generator/blob/master/java/QrCodeGeneratorDemo.java
	public static BufferedImage toImage(io.nayuki.qrcodegen.QrCode qr, int scale, int border, int lightColor, int darkColor) {

		Objects.requireNonNull(qr);

		if (scale <= 0 || border < 0) {
			throw new IllegalArgumentException("Value out of range"); //$NON-NLS-1$
		}

		if (border > Integer.MAX_VALUE / 2 || qr.size + border * 2L > Integer.MAX_VALUE / scale) {
			throw new IllegalArgumentException("Scale or border too large"); //$NON-NLS-1$
		}

		final BufferedImage result = new BufferedImage((qr.size + border * 2) * scale, (qr.size + border * 2) * scale, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < result.getHeight(); y++) {
			for (int x = 0; x < result.getWidth(); x++) {
				boolean color = qr.getModule(x / scale - border, y / scale - border);
				result.setRGB(x, y, color ? darkColor : lightColor);
			}
		}

		return result;

	}

}
