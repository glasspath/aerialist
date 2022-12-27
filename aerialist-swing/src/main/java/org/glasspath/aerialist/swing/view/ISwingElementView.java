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
import java.util.List;

import javax.swing.JComponent;

import org.glasspath.aerialist.Border;
import org.glasspath.aerialist.Element;
import org.glasspath.aerialist.Group;
import org.glasspath.aerialist.HeightPolicy;
import org.glasspath.aerialist.Image;
import org.glasspath.aerialist.QrCode;
import org.glasspath.aerialist.Table;
import org.glasspath.aerialist.TextBox;
import org.glasspath.aerialist.YPolicy;

public interface ISwingElementView<T extends Element> {

	public void init(T element);

	public T toElement();

	public int getX();

	public int getY();

	public int getWidth();

	public int getHeight();

	public YPolicy getYPolicy();

	public void setYPolicy(YPolicy yPolicy);

	public HeightPolicy getHeightPolicy();

	public void setHeightPolicy(HeightPolicy heightPolicy);

	public Color getBackgroundColor();

	public void setBackgroundColor(Color color);

	public List<Border> getBorders();

	public static void copyProperties(ISwingElementView<? extends Element> elementView, Element element) {

		element.setX(elementView.getX());
		element.setY(elementView.getY());
		element.setWidth(elementView.getWidth());
		element.setHeight(elementView.getHeight());

		element.setYPolicy(elementView.getYPolicy() != null ? elementView.getYPolicy().stringValue : YPolicy.DEFAULT.stringValue);
		element.setHeightPolicy(elementView.getHeightPolicy() != null ? elementView.getHeightPolicy().stringValue : HeightPolicy.DEFAULT.stringValue);

		element.setBackground(ColorUtils.toHex(elementView.getBackgroundColor()));

		element.getBorders().clear();
		for (Border border : elementView.getBorders()) {
			element.getBorders().add(new Border(border));
		}

	}

	public static JComponent createElementView(Element element, ISwingViewContext viewContext) {

		if (element instanceof TextBox) {
			TextBoxView view = new TextBoxView(viewContext);
			view.init((TextBox) element);
			return view;
		} else if (element instanceof Image) {
			ImageView view = new ImageView(viewContext);
			view.init((Image) element);
			return view;
		} else if (element instanceof QrCode) {
			QrCodeView view = new QrCodeView(viewContext);
			view.init((QrCode) element);
			return view;
		} else if (element instanceof Table) {
			TableView view = new TableView(viewContext);
			view.init((Table) element);
			return view;
		} else if (element instanceof Group) {
			GroupView view = new GroupView(viewContext);
			view.init((Group) element);
			return view;
		} else {
			return null;
		}

	}

}
