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
package org.glasspath.aerialist.swing;

import java.awt.Component;

import org.glasspath.aerialist.Element;
import org.glasspath.aerialist.Group;
import org.glasspath.aerialist.HeightPolicy;
import org.glasspath.aerialist.Image;
import org.glasspath.aerialist.QrCode;
import org.glasspath.aerialist.Table;
import org.glasspath.aerialist.TextBox;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.ElementLayoutInfo;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.ImageLayoutInfo;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.LayoutInfo;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.TableLayoutInfo;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.TextBoxLayoutInfo;
import org.glasspath.aerialist.layout.IElementLayoutMetrics;
import org.glasspath.aerialist.media.MediaCache.ImageSize;
import org.glasspath.aerialist.swing.view.GroupView;
import org.glasspath.aerialist.swing.view.ISwingViewContext;
import org.glasspath.aerialist.swing.view.QrCodeView;
import org.glasspath.aerialist.swing.view.TableCellView;
import org.glasspath.aerialist.swing.view.TableView;
import org.glasspath.aerialist.swing.view.TableView.ComponentProxy;
import org.glasspath.aerialist.swing.view.TextBoxView;

public class SwingLayoutMetrics implements IElementLayoutMetrics {

	private final ISwingViewContext viewContext;
	private LayoutInfo layoutInfo = new LayoutInfo();
	private TextBoxView textBoxView = null;
	private TableView tableView = null;
	private TableCellView tableCellView = null;
	private QrCodeView qrCodeView = null;
	private GroupView groupView = null;

	public SwingLayoutMetrics(ISwingViewContext viewContext) {
		this.viewContext = viewContext;
	}

	@Override
	public int getPreferredHeight(Element element) {
		return getElementLayoutInfo(element).preferredHeight;
	}

	@Override
	public LayoutInfo getLayoutInfo() {
		return layoutInfo;
	}

	@Override
	public void setLayoutInfo(LayoutInfo layoutInfo) {
		this.layoutInfo = layoutInfo;
	}

	@Override
	public ElementLayoutInfo getElementLayoutInfo(Element element) {
		if (element instanceof TextBox) {
			return getTextBoxLayoutInfo((TextBox) element);
		} else if (element instanceof Table) {
			return getTableLayoutInfo((Table) element);
		} else if (element instanceof Image) {
			return getImageLayoutInfo((Image) element);
		} else if (element instanceof QrCode) {
			return getQrCodeLayoutInfo((QrCode) element);
		} else if (element instanceof Group) {
			return getGroupLayoutInfo((Group) element);
		} else {
			return new ElementLayoutInfo(element.getHeight());
		}
	}

	protected ElementLayoutInfo getTextBoxLayoutInfo(TextBox textBox) {

		TextBoxLayoutInfo info = layoutInfo.textBoxes.get(textBox);

		if (info == null) {

			if (textBoxView == null) {
				textBoxView = new TextBoxView(viewContext);
			}

			textBoxView.init(textBox);
			textBoxView.setBounds(textBox.getX(), textBox.getY(), textBox.getWidth(), textBox.getHeight());

			info = textBoxView.getTextBoxLayoutInfo();

			layoutInfo.textBoxes.put(textBox, info);

		}

		return info;

	}

	protected TableLayoutInfo getTableLayoutInfo(Table table) {

		TableLayoutInfo info = layoutInfo.tables.get(table);

		if (info == null) {

			if (tableView == null) {
				tableView = new TableView(viewContext);
				tableCellView = new TableCellView(viewContext);
			}

			tableView.init(table, new ComponentProxy() {

				@Override
				public int getComponentCount() {
					return table.getTableCells().size();
				}

				@Override
				public Component getComponent(int n) {
					tableCellView.init(table.getTableCells().get(n));
					return tableCellView;
				}
			});
			// Bounds are set by init()
			// tableView.setBounds(table.getX(), table.getY(), table.getWidth(), table.getHeight());
			tableView.getTableLayout().layoutContainer(tableView);

			info = tableView.getTableLayoutInfo(HeightPolicy.get(table.getHeightPolicy()) == HeightPolicy.AUTO);
			layoutInfo.tables.put(table, info);

		}

		return info;

	}

	protected ImageLayoutInfo getImageLayoutInfo(Image image) {

		ImageLayoutInfo info = layoutInfo.images.get(image);

		if (info == null) {

			info = new ImageLayoutInfo();
			info.preferredHeight = image.getHeight();

			ImageSize imageSize = viewContext.getMediaCache().getImageSize(image.getSrc());
			if (imageSize != null) {

				info.imageWidth = imageSize.width;
				info.imageHeight = imageSize.height;

				if (HeightPolicy.get(image.getHeightPolicy()) == HeightPolicy.AUTO) {
					info.preferredHeight = info.imageHeight;
				}

			}

			layoutInfo.images.put(image, info);

		}

		return info;

	}

	protected ElementLayoutInfo getQrCodeLayoutInfo(QrCode qrCode) {

		if (qrCodeView == null) {
			qrCodeView = new QrCodeView(viewContext);
		}

		qrCodeView.init(qrCode);
		qrCodeView.setBounds(qrCode.getX(), qrCode.getY(), qrCode.getWidth(), qrCode.getHeight());

		return new ElementLayoutInfo(qrCodeView.getPreferredSize().height);

	}

	protected ElementLayoutInfo getGroupLayoutInfo(Group group) {

		if (groupView == null) {
			groupView = new GroupView(viewContext);
		}

		groupView.init(group);
		groupView.setBounds(group.getX(), group.getY(), group.getWidth(), group.getHeight());

		return new ElementLayoutInfo(groupView.getPreferredSize().height);

	}

}
