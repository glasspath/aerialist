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
package org.glasspath.aerialist;

public class ContentParser {

	public ContentParser() {

	}

	public void parseRoot(ContentRoot root) {
		if (root instanceof Document) {
			parseDocument((Document) root);
		} else if (root instanceof Email) {
			parseEmail((Email) root);
		}
	}

	public void parseDocument(Document document) {

		if (document.getHeader() != null) {
			parseHeaderPage(document.getHeader());
		}

		if (document.getFooter() != null) {
			parseFooterPage(document.getFooter());
		}

		for (Page page : document.getPages()) {
			parsePage(page);
		}

	}

	public void parseEmail(Email email) {

		if (email.getSubjectTextBox() != null) {
			parseTextBox(email.getSubjectTextBox());
		}

		if (email.getTable() != null) {
			parseTable(email.getTable());
		}

	}

	public void parseHeaderPage(Page page) {
		parseElementContainer(page);
	}

	public void parseFooterPage(Page page) {
		parseElementContainer(page);
	}

	public void parsePage(Page page) {
		parseElementContainer(page);
	}

	public void parseElementContainer(IElementContainer elementContainer) {
		for (Element element : elementContainer.getElements()) {
			parseElement(element);
		}
	}

	public void parseElement(Element element) {
		if (element instanceof Group) {
			parseGroup((Group) element);
		} else if (element instanceof TextBox) {
			parseTextBox((TextBox) element);
		} else if (element instanceof QrCode) {
			parseQrCode((QrCode) element);
		} else if (element instanceof Table) {
			parseTable((Table) element);
		} else if (element instanceof Image) {
			parseImage((Image) element);
		}
	}

	public void parseGroup(Group group) {
		parseElementContainer(group);
	}

	public void parseTextBox(TextBox textBox) {
		parseIText(textBox);
	}

	public void parseQrCode(QrCode qrCode) {
		parseIText(qrCode);
	}

	public void parseTable(Table table) {
		for (TableCell tableCell : table.getTableCells()) {
			parseTableCell(table, tableCell);
		}
	}

	public void parseTableCell(Table table, TableCell tableCell) {
		parseIText(tableCell);
	}

	public void parseIText(IText iText) {

	}

	public void parseImage(Image image) {

	}

}
