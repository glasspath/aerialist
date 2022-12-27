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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.plaf.TextUI;
import javax.swing.plaf.basic.BasicTextUI;
import javax.swing.text.ParagraphView;
import javax.swing.text.View;

import org.glasspath.aerialist.Border;
import org.glasspath.aerialist.HeightPolicy;
import org.glasspath.aerialist.Padding;
import org.glasspath.aerialist.TextBox;
import org.glasspath.aerialist.YPolicy;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.Rect;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.TextBoxLayoutInfo;
import org.glasspath.aerialist.text.TextLayout;
import org.glasspath.aerialist.text.TextLayout.Line;

public class TextBoxView extends TextView implements ISwingElementView<TextBox> {

	private YPolicy yPolicy = YPolicy.DEFAULT;
	private HeightPolicy heightPolicy = HeightPolicy.DEFAULT;
	private Color backgroundColor = null;
	private final List<Border> borders = new ArrayList<>();
	private Padding padding = new Padding();

	public TextBoxView(ISwingViewContext viewContext) {
		super(viewContext);

		setOpaque(false);

		addFocusListener(new FocusAdapter() {

			@Override
			public void focusGained(FocusEvent e) {
				viewContext.focusGained(TextBoxView.this);
			}
		});

	}

	@Override
	public void init(TextBox element) {
		super.init(element);

		yPolicy = YPolicy.get(element.getYPolicy());
		heightPolicy = HeightPolicy.get(element.getHeightPolicy());

		setBackgroundColor(ColorUtils.fromHex(element.getBackground()));

		borders.clear();
		for (Border border : element.getBorders()) {
			borders.add(new Border(border));
		}

		padding.parse(element.getPadding());
		applyPadding(padding);

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
	public List<Border> getBorders() {
		return borders;
	}

	public Padding getPadding() {
		return padding;
	}

	public void applyPadding(Padding padding) {
		this.padding.from(padding);
		setBorder(BorderFactory.createEmptyBorder(padding.top, padding.left, padding.bottom, padding.right));
	}

	@Override
	public TextBox toElement() {

		TextBox textBox = new TextBox();
		ISwingElementView.copyProperties(this, textBox);
		textBox.setPadding(padding.toString());
		toText(textBox);

		return textBox;

	}

	public TextBoxLayoutInfo getTextBoxLayoutInfo() {

		TextBoxLayoutInfo info = new TextBoxLayoutInfo();
		info.preferredHeight = getPreferredSize().height;

		List<Line> lines = new ArrayList<>();
		parseViews(lines);
		info.textLayout = new TextLayout(lines.toArray(new Line[0]), 0, 0); // TODO: Calculate width and height?

		return info;

	}

	private void parseViews(List<Line> lines) {

		TextUI textUI = getUI();
		if (textUI instanceof BasicTextUI) {

			BasicTextUI ui = (BasicTextUI) textUI;

			View rootView = ui.getRootView(this);
			if (rootView != null) {
				parseView(rootView, lines);
			}

		}

	}

	private void parseView(View view, List<Line> lines) {

		if (view instanceof ParagraphView) {
			parseParagraphView((ParagraphView) view, lines);
		}

		for (int i = 0; i < view.getViewCount(); i++) {
			parseView(view.getView(i), lines);
		}

	}

	private void parseParagraphView(ParagraphView view, List<Line> lines) {

		for (int i = 0; i < view.getViewCount(); i++) {

			View v = view.getView(i);

			// TODO: Check if it is really a paragraph row? (class is not public so we cannot use instanceof)

			Rect lineBounds = parseParagraphRowView(v);
			if (lineBounds != null) {

				lineBounds.x -= padding.left;
				lineBounds.y -= padding.top;

				lines.add(new Line(v.getStartOffset(), null, 0, lineBounds));
				// System.out.println(v.getStartOffset() + ": y = " + lineBounds.y + ", h = " + lineBounds.height);

			} else {
				System.err.println("TextBoxView.parseParagraphView() Could not determine line bounds"); //$NON-NLS-1$
			}

		}

	}

	private Rect parseParagraphRowView(View view) {

		Rect lineBounds = null;

		for (int i = 0; i < view.getViewCount(); i++) {

			View v = view.getView(i);

			try {

				Rectangle2D rect2D = modelToView2D(v.getStartOffset());

				if (lineBounds == null) {
					lineBounds = new Rect((float) rect2D.getX(), (float) rect2D.getY(), (float) rect2D.getWidth(), (float) rect2D.getHeight());
				} else {

					if (rect2D.getY() < lineBounds.y) {

						float newHeight = (lineBounds.y + lineBounds.height) - (float) rect2D.getY();
						if (rect2D.getHeight() > newHeight) {
							newHeight = (float) rect2D.getHeight();
						}

						lineBounds.y = (float) rect2D.getY();
						lineBounds.height = newHeight;

					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return lineBounds;

	}

	@Override
	public void paint(Graphics g) {

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (backgroundColor != null) {
			g2d.setColor(backgroundColor);
			g2d.fillRect(0, 0, getWidth(), getHeight());
		}

		BorderUtils.paintBorders(g2d, borders, new Rectangle(0, 0, getWidth(), getHeight()));

		super.paint(g);

	}

}
