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

public class PageViewParser {

	public PageViewParser() {

	}

	public void parsePageContainer(PageContainer pageContainer) {

		for (PageView pageView : pageContainer.getPageViews()) {
			if (pageView instanceof LayeredPageView) {
				parseLayeredPageView((LayeredPageView) pageView);
			} else {
				parsePageView(pageView);
			}
		}

	}

	protected void parseLayeredPageView(LayeredPageView layeredPageView) {

		parsePageView(layeredPageView);

		for (PageView layerView : layeredPageView.getLayers()) {
			parsePageView(layerView);
		}

	}

	protected void parsePageView(PageView pageView) {
		parseElementContainer(pageView);
	}

	protected void parseElementContainer(ElementContainer elementContainer) {

		Component component;
		for (int i = 0; i < elementContainer.getComponentCount(); i++) {

			component = elementContainer.getComponent(i);
			if (component instanceof ISwingElementView<?>) {
				parseElementView((ISwingElementView<?>) component);
			}

		}

	}

	protected void parseElementView(ISwingElementView<?> elementView) {

		if (elementView instanceof GroupView) {
			parseGroupView((GroupView) elementView);
		} else if (elementView instanceof TextBoxView) {
			parseTextBoxView((TextBoxView) elementView);
		} else if (elementView instanceof QrCodeView) {
			parseQrCodeView((QrCodeView) elementView);
		} else if (elementView instanceof TableView) {
			parseTableView((TableView) elementView);
		}

	}

	protected void parseGroupView(GroupView groupView) {
		parseElementContainer(groupView);
	}

	protected void parseTextBoxView(TextBoxView textBoxView) {
		parseTextView(textBoxView);
	}

	protected void parseQrCodeView(QrCodeView qrCodeView) {
		parseTextView(qrCodeView);
	}

	protected void parseTableView(TableView tableView) {

		Component component;
		for (int i = 0; i < tableView.getComponentCount(); i++) {

			component = tableView.getComponent(i);
			if (component instanceof TableCellView) {
				parseTableCellView((TableCellView) component);
			}

		}

	}

	protected void parseTableCellView(TableCellView tableCellView) {
		parseTextView(tableCellView);
	}

	protected void parseTextView(TextView textView) {

	}

}
