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

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComponent;

import org.glasspath.aerialist.Element;
import org.glasspath.aerialist.IVisible;
import org.glasspath.aerialist.Page;

public class PageView extends ElementContainer implements IVisible {

	private String visible = null;

	public PageView(ISwingViewContext viewContext) {
		super(viewContext);
	}

	public PageView(ISwingViewContext viewContext, Page page) {
		super(viewContext);

		init(page);

	}

	@Override
	public String getVisible() {
		return visible;
	}

	@Override
	public void setVisible(String visible) {
		this.visible = visible;
	}

	public void init(Page page) {

		setPageSize(page.getWidth(), page.getHeight());

		visible = page.getVisible();

		JComponent elementView;
		for (Element element : page.getElements()) {

			elementView = ISwingElementView.createElementView(element, viewContext);
			if (elementView != null) {
				elementView.setBounds(element.getX(), element.getY(), element.getWidth(), element.getHeight());
				add(elementView);
			}

		}

	}

	public void setPageSize(int width, int height) {

		if (width > 0 && height > 0) {

			Dimension size = new Dimension(width, height);

			setSize(size);
			setPreferredSize(size);
			setMinimumSize(size);
			setMaximumSize(size);

		}

	}

	public Page toPage() {

		Page page = new Page();
		page.setWidth(getWidth());
		page.setHeight(getHeight());
		page.setVisible(visible);

		Component elementView;
		for (int i = 0; i < getComponentCount(); i++) {

			elementView = getComponent(i);
			if (elementView instanceof ISwingElementView) {
				page.getElements().add(((ISwingElementView<?>) elementView).toElement());
			}

		}

		return page;

	}

}
