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
package org.glasspath.aerialist.layout;

import org.glasspath.aerialist.Element;
import org.glasspath.aerialist.HeightPolicy;
import org.glasspath.aerialist.IElementContainer;
import org.glasspath.aerialist.YPolicy;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.Bounds;

public class DefaultVerticalLayout extends VerticalLayout<IElementContainer, Element> {

	public DefaultVerticalLayout(ILayoutContext<?> layoutContext, ILayoutMetrics<Element> metrics) {
		super(layoutContext, metrics);
	}

	public DefaultVerticalLayout(ILayoutContext<?> layoutContext, ILayoutMetrics<Element> metrics, IElementContainer container) {
		super(layoutContext, metrics, container);
	}

	@Override
	public YPolicy getYPolicy(Element element) {
		return YPolicy.get(element.getYPolicy());
	}

	@Override
	public HeightPolicy getHeightPolicy(Element element) {
		return HeightPolicy.get(element.getHeightPolicy());
	}

	@Override
	public int getElementCount() {
		return container.getElements().size();
	}

	@Override
	public Element getElement(int i) {
		return container.getElements().get(i);
	}

	@Override
	public Bounds getBounds(Element element) {
		return new Bounds(element.getX(), element.getY(), element.getWidth(), element.getHeight());
	}

	@Override
	public void setBounds(Element element, Bounds bounds) {
		element.setX(bounds.x);
		element.setY(bounds.y);
		element.setWidth(bounds.width);
		element.setHeight(bounds.height);
	}

	@Override
	public int getX(Element element) {
		return element.getX();
	}

	@Override
	public int getY(Element element) {
		return element.getY();
	}

	@Override
	public int getWidth(Element element) {
		return element.getWidth();
	}

	@Override
	public int getHeight(Element element) {
		return element.getHeight();
	}

}
