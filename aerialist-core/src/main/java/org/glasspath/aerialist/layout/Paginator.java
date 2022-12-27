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

import java.util.ArrayList;
import java.util.List;

import org.glasspath.aerialist.Element;
import org.glasspath.aerialist.HeightPolicy;
import org.glasspath.aerialist.Page;
import org.glasspath.aerialist.Table;
import org.glasspath.aerialist.TableCell;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.LayoutInfo;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.PageLayoutInfo;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.TableLayoutInfo;

public class Paginator {

	private final LayoutListener listener;

	public Paginator(LayoutListener listener) {
		this.listener = listener;
	}

	public List<PageLayoutInfo> paginate(PageLayoutInfo pageLayoutInfo, int yMin, int yMax) {

		List<PageLayoutInfo> newPages = new ArrayList<>();

		if (pageLayoutInfo.layoutInfo != null) {

			Page page = pageLayoutInfo.page;

			Page lastAddedPage;
			Element element, lastAddedElement;

			lastAddedPage = null;
			lastAddedElement = null;

			// Split elements across new pages
			for (int i = 0; i < page.getElements().size(); i++) {

				element = page.getElements().get(i);

				if (HeightPolicy.get(element.getHeightPolicy()) == HeightPolicy.AUTO && element.getY() + element.getHeight() > yMax) {

					// System.out.println("element y = " + element.getY() + ", height = " + element.getHeight());

					List<PageLayoutInfo> newPagesForElement = paginate(element, pageLayoutInfo, yMin, yMax);
					if (newPagesForElement != null && newPagesForElement.size() > 0) {

						newPages.addAll(newPagesForElement);

						lastAddedPage = newPagesForElement.get(newPagesForElement.size() - 1).page;
						if (lastAddedPage.getElements().size() == 1) {
							lastAddedElement = lastAddedPage.getElements().get(0);
						} else {
							lastAddedElement = null;
						}

					}

				}

			}

			// TODO
			// Move elements to new pages
			for (int i = 0; i < page.getElements().size(); i++) {

				element = page.getElements().get(i);
				if (element.getY() + element.getHeight() > yMax) {

					if (lastAddedPage != null && lastAddedElement != null) {

					} else {

					}

				}

			}

		}

		return newPages;

	}

	protected List<PageLayoutInfo> paginate(Element element, PageLayoutInfo pageLayoutInfo, int yMin, int yMax) {

		// TODO: Implement on more elements
		if (element instanceof Table) {
			return paginateTable((Table) element, pageLayoutInfo, yMin, yMax);
		} else {
			return null;
		}

	}

	protected List<PageLayoutInfo> paginateTable(Table table, PageLayoutInfo pageLayoutInfo, int yMin, int yMax) {

		List<PageLayoutInfo> newPages = new ArrayList<>();

		TableLayoutInfo tableLayoutInfo = pageLayoutInfo.layoutInfo.tables.get(table);
		if (tableLayoutInfo != null && tableLayoutInfo.rowCount > 1 && tableLayoutInfo.rowBounds != null && table.getY() + table.getHeight() > yMax) {

			// TODO: For now we only support 1 header row which is repeated on each page by default
			int headerRowHeight = tableLayoutInfo.rowBounds[0].height;

			// Determine at which rows the table is to be split (splitAtRows row numbers are 1-based, rowBounds[i] is 0-based)
			// Start at 1 because rowBounds[0] contains the bounds of the header row
			List<Integer> splitAtRows = new ArrayList<>();
			int yBottom = table.getY() + headerRowHeight;
			for (int i = 1; i < tableLayoutInfo.rowCount; i++) {

				yBottom += tableLayoutInfo.rowBounds[i].height;
				if (yBottom > yMax) {

					// This row's bottom is over the max and has to be moved to a new table, add 1 to convert to 1-based row number
					splitAtRows.add(i + 1);

					// The new table now contains the header row and this row
					yBottom = yMin + headerRowHeight + tableLayoutInfo.rowBounds[i].height;

				}

			}

			if (splitAtRows.size() > 0) {

				int fromRow, toRow;
				for (int i = 0; i < splitAtRows.size(); i++) {

					// Get the range of rows for the new table, fromRow is inclusive, toRow is exclusive, row numbers are 1-based
					fromRow = splitAtRows.get(i);
					if (i < splitAtRows.size() - 1) {
						toRow = splitAtRows.get(i + 1);
					} else {
						// Last range, for example: if last row number is 100 then rowCount is also 100
						// So we need to add 1 because toRow is exclusive and 1-based (toRow should become 101)
						toRow = tableLayoutInfo.rowCount + 1;
					}

					// Create new tables by duplicating the original table and removing all the rows that are not in the range fromRow - toRow
					Table newTable = new Table(table);

					List<TableCell> removeTableCells = new ArrayList<>();
					for (TableCell tableCell : newTable.getTableCells()) {

						// Skip the header row, we don't have to change it's row number and we also don't have to remove it
						if (tableCell.getRow() > 1) {

							// For example: rows 10 - 19 have to be moved to next page (fromRow = 10, toRow = 20)
							// When we find row 10 we have to change it's row number to 2 (header row is 1)
							// (10 - 10) + 1 + 1 = 2 (add 1 for header row and add 1 because rows start at 1)
							if (tableCell.getRow() >= fromRow && tableCell.getRow() < toRow) {
								tableCell.setRow((tableCell.getRow() - fromRow) + 1 + 1);
							} else {
								// All other rows need to be removed from this new table
								removeTableCells.add(tableCell);
							}

						}

					}
					newTable.getTableCells().removeAll(removeTableCells);

					newTable.setY(yMin);

					// Calculate height of the new table, subtract 1 because rowBounds is 0-based (bounds of row 1 are stored in rowBounds[0]),
					// toRow is exclusive, so we need to subtract 2, for example: if last row number is 100 then toRow is 101, bounds of
					// row number 100 are stored in rowBounds[99] so we need to use toRow - 2
					newTable.setHeight(headerRowHeight + ((tableLayoutInfo.rowBounds[toRow - 2].y + tableLayoutInfo.rowBounds[toRow - 2].height) - tableLayoutInfo.rowBounds[fromRow - 1].y));

					// Create a new page and add the new table
					fireStatusChanged("Creating new page");

					Page newPage = new Page(pageLayoutInfo.page.getWidth(), pageLayoutInfo.page.getHeight());
					newPage.getElements().add(newTable);

					PageLayoutInfo newPageLayoutInfo = new PageLayoutInfo(newPage);
					newPageLayoutInfo.layoutInfo = new LayoutInfo();

					// TODO: Update and create TableLayoutInfo

					newPages.add(newPageLayoutInfo);

				}

				// Finally we have to update the original table
				toRow = splitAtRows.get(0);

				// We can simply use y of the next range for the height
				table.setHeight(tableLayoutInfo.rowBounds[toRow - 1].y);

				// Remove all the cells that are outside the first range
				List<TableCell> removeTableCells = new ArrayList<>();
				for (TableCell tableCell : table.getTableCells()) {
					if (tableCell.getRow() >= toRow) {
						removeTableCells.add(tableCell);
					}
				}
				table.getTableCells().removeAll(removeTableCells);

			}

		}

		return newPages;

	}

	private void fireStatusChanged(String status) {
		if (listener != null) {
			listener.statusChanged(status);
		}
	}

}
