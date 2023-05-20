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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.glasspath.aerialist.Border;
import org.glasspath.aerialist.Element;
import org.glasspath.aerialist.Group;
import org.glasspath.aerialist.HeightPolicy;
import org.glasspath.aerialist.Radius;
import org.glasspath.aerialist.YPolicy;

public class GroupView extends ElementContainer implements ISwingElementView<Group> {

	private YPolicy yPolicy = YPolicy.DEFAULT;
	private HeightPolicy heightPolicy = HeightPolicy.DEFAULT;
	private Color backgroundColor = null;
	private Radius radius = new Radius();
	private final List<Border> borders = new ArrayList<>();

	public GroupView(ISwingViewContext viewContext) {
		super(viewContext);

		ISwingViewContext.installSelectionHandler(this, viewContext);

	}

	@Override
	public void init(Group group) {

		yPolicy = YPolicy.get(group.getYPolicy());
		heightPolicy = HeightPolicy.get(group.getHeightPolicy());

		setBackgroundColor(ColorUtils.fromHex(group.getBackground()));

		radius.parse(group.getRadius());

		borders.clear();
		for (Border border : group.getBorders()) {
			borders.add(new Border(border));
		}

		JComponent elementView;
		for (Element element : group.getElements()) {

			elementView = ISwingElementView.createElementView(element, viewContext);
			if (elementView != null) {
				elementView.setBounds(element.getX(), element.getY(), element.getWidth(), element.getHeight());
				add(elementView);
			}

		}

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

		if (backgroundColor != null) {
			setBackground(backgroundColor);
			setOpaque(true);
		} else {
			setOpaque(false);
		}

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
	public Group toElement() {

		Group group = new Group();
		ISwingElementView.copyProperties(this, group);

		Component elementView;
		for (int i = 0; i < getComponentCount(); i++) {

			elementView = getComponent(i);
			if (elementView instanceof ISwingElementView) {
				group.getElements().add(((ISwingElementView<?>) elementView).toElement());
			}

		}

		return group;

	}

	@Override
	public Dimension getPreferredSize() {

		Dimension size = super.getPreferredSize();

		if (heightPolicy == HeightPolicy.AUTO && viewContext.isHeightPolicyEnabled()) {

			size.width = 0;
			size.height = 0;

			Component component;
			for (int i = 0; i < getComponentCount(); i++) {

				component = getComponent(i);

				int xMax = component.getX() + component.getWidth();
				if (xMax > size.width) {
					size.width = xMax;
				}

				int yMax = component.getY() + component.getHeight();
				if (yMax > size.height) {
					size.height = yMax;
				}

			}

		}

		return size;

	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Rectangle rect = new Rectangle(0, 0, getWidth(), getHeight());
		PaintUtils.paintBackground(g2d, rect, backgroundColor, radius);
		PaintUtils.paintBorders(g2d, borders, rect, radius);

	}

}
