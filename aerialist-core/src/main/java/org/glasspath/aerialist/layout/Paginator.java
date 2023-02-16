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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glasspath.aerialist.Document;
import org.glasspath.aerialist.Element;
import org.glasspath.aerialist.HeightPolicy;
import org.glasspath.aerialist.Padding;
import org.glasspath.aerialist.Page;
import org.glasspath.aerialist.Page.PageSize;
import org.glasspath.aerialist.Pagination;
import org.glasspath.aerialist.Table;
import org.glasspath.aerialist.TableCell;
import org.glasspath.aerialist.TextBox;
import org.glasspath.aerialist.TextStyle;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.Bounds;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.LayoutInfo;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.PageLayoutInfo;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.Rect;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.TableLayoutInfo;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.TextBoxLayoutInfo;
import org.glasspath.aerialist.text.TextLayout;
import org.glasspath.aerialist.text.TextLayout.Line;
import org.glasspath.aerialist.text.TextLayout.Span;

public class Paginator {

	private final DefaultVerticalLayout layout;
	private final LayoutListener listener;

	public Paginator(DefaultVerticalLayout layout, LayoutListener listener) {
		this.layout = layout;
		this.listener = listener;
	}

	public List<PageLayoutInfo> paginate(Document document, Page sourcePage, PageLayoutInfo pageLayoutInfo) {

		int yMin = document.getHeaderHeight();
		int yMax = sourcePage.getHeight() - document.getFooterHeight();

		List<PageLayoutInfo> newPages = new ArrayList<>();

		if (pageLayoutInfo.layoutInfo != null) {

			Page page = pageLayoutInfo.page;

			PageLayoutInfo newPageLayoutInfo = null;

			// Move elements to new page, this has to be done before splitting elements
			// so the VerticalLayout can do it's work
			List<Element> moveElements = new ArrayList<>();
			for (int i = 0; i < page.getElements().size(); i++) {

				Element element = page.getElements().get(i);

				// TODO: Implement pagination settings on pages and elements also?
				Pagination pagination = document.getPagination();

				if (element.getY() > yMax) {

					// Element is beyond page border, so move it
					moveElements.add(element);

				} else if (element.getY() > yMin // No need to move to new page if it is already at yMin
						&& element.getY() + element.getHeight() > yMax) {

					SplitInfo splitInfo = getSplitInfo(element, pagination, pageLayoutInfo, yMax);
					if (!splitInfo.splitPossible) {

						// Element extends over page border, but it cannot be split, so move it
						moveElements.add(element);

					} else if (pagination != null && (splitInfo.firstElementHeight < pagination.getMinHeight() || splitInfo.secondElementHeight < pagination.getMinHeight())) {

						// Splitting element would result in a element that is too short, so move it
						moveElements.add(element);

					}

				}

			}

			if (moveElements.size() > 0) {

				if (newPageLayoutInfo == null) {

					newPageLayoutInfo = new PageLayoutInfo(new Page(PageSize.A4)); // TODO
					newPageLayoutInfo.layoutInfo = new LayoutInfo();

					newPages.add(newPageLayoutInfo);

					layout.setContainer(newPageLayoutInfo.page, false);

				}

				for (Element element : moveElements) {
					move(element, pageLayoutInfo, newPageLayoutInfo);
				}

			}

			// Split elements across new pages
			Map<Element, Element> splitElements = new HashMap<>();
			for (int i = 0; i < page.getElements().size(); i++) {

				Element element = page.getElements().get(i);

				// TODO: Implement pagination settings on pages and elements also?
				Pagination pagination = document.getPagination();

				if (element.getY() <= yMax && element.getY() + element.getHeight() > yMax) {

					SplitInfo splitInfo = getSplitInfo(element, pagination, pageLayoutInfo, yMax);
					if (splitInfo.splitPossible) {

						if (newPageLayoutInfo == null) {

							newPageLayoutInfo = new PageLayoutInfo(new Page(PageSize.A4)); // TODO
							newPageLayoutInfo.layoutInfo = new LayoutInfo();

							newPages.add(newPageLayoutInfo);

							layout.setContainer(newPageLayoutInfo.page, false);

						}

						Element newElement = split(splitInfo, element, pageLayoutInfo, yMin, newPageLayoutInfo);
						if (newElement != null) {
							splitElements.put(element, newElement);
						}

					}

				}

			}

			if (newPageLayoutInfo != null) {

				boolean layoutUpdated = false;

				if (splitElements.size() > 0) {

					Map<Element, Element> updatedAnchors = new HashMap<>();
					for (Element element : moveElements) {
						updatedAnchors.put(element, element);
					}
					updatedAnchors.putAll(splitElements);
					layout.replaceAnchors(updatedAnchors);

					for (Element element : splitElements.values()) {
						layout.updateLayout(element);
					}

					layoutUpdated = true;

				} else if (moveElements.size() > 0) {

					int yElementMin = Integer.MAX_VALUE;

					for (Element element : moveElements) {
						if (element.getY() < yElementMin) {
							yElementMin = element.getY();
						}
					}

					if (yElementMin < Integer.MAX_VALUE && yElementMin > yMin) {

						int yOffset = yElementMin - yMin;

						for (Element element : moveElements) {
							element.setY(element.getY() - yOffset);
						}

					}

					layoutUpdated = true;

				}

				if (layoutUpdated) {
					newPages.addAll(paginate(document, sourcePage, newPageLayoutInfo));
				} else {
					System.err.println("TODO: Paginator, invalid attempt to paginate");
				}

			}

		}

		return newPages;

	}

	protected void move(Element element, PageLayoutInfo pageLayoutInfo, PageLayoutInfo newPageLayoutInfo) {

		pageLayoutInfo.layoutInfo.move(element, newPageLayoutInfo.layoutInfo);

		pageLayoutInfo.page.getElements().remove(element);

		newPageLayoutInfo.page.getElements().add(element);

	}

	protected SplitInfo getSplitInfo(Element element, Pagination pagination, PageLayoutInfo pageLayoutInfo, int yMax) {

		SplitInfo splitInfo = new SplitInfo();

		if (HeightPolicy.get(element.getHeightPolicy()) == HeightPolicy.AUTO) {
			if (element instanceof TextBox) {
				getTextBoxSplitInfo(splitInfo, (TextBox) element, pagination, pageLayoutInfo, yMax);
			} else if (element instanceof Table) {
				getTableSplitInfo(splitInfo, (Table) element, pagination, pageLayoutInfo, yMax);
			}
		}

		return splitInfo;

	}

	protected Element split(SplitInfo splitInfo, Element element, PageLayoutInfo pageLayoutInfo, int yMin, PageLayoutInfo newPageLayoutInfo) {

		Element newElement = null;

		if (element instanceof TextBox) {
			newElement = splitTextBox(splitInfo, (TextBox) element, pageLayoutInfo, yMin, newPageLayoutInfo);
		} else if (element instanceof Table) {
			newElement = splitTable(splitInfo, (Table) element, pageLayoutInfo, yMin, newPageLayoutInfo);
		}

		return newElement;

	}

	protected void getTextBoxSplitInfo(SplitInfo splitInfo, TextBox textBox, Pagination pagination, PageLayoutInfo pageLayoutInfo, int yMax) {

		TextBoxLayoutInfo layoutInfo = pageLayoutInfo.layoutInfo.textBoxes.get(textBox);
		if (layoutInfo != null && layoutInfo.textLayout != null && layoutInfo.textLayout.lines != null && textBox.getY() + textBox.getHeight() > yMax) {

			Padding padding = new Padding(textBox.getPadding());

			// Determine at which line the text-box is to be split
			// Start at 1 because we want to keep at least 1 line in the original text-box
			for (int i = 1; i < layoutInfo.textLayout.lines.length; i++) {

				Rect lineBounds = layoutInfo.textLayout.lines[i].lineBounds;

				float textBoxHeight = padding.top + lineBounds.y + lineBounds.height + padding.bottom;
				if (textBox.getY() + textBoxHeight > yMax) {

					// At this line the bottom of the text-box is over the max
					splitInfo.splitPossible = true;
					splitInfo.splitIndex = i;

					break;

				} else if (pagination != null && textBox.getHeight() - textBoxHeight < pagination.getMinHeight()) {

					// Remaining text-box would be too short, so split here
					splitInfo.splitPossible = true;
					splitInfo.splitIndex = i;

					break;

				} else {

					splitInfo.firstElementHeight = (int) textBoxHeight;
					splitInfo.secondElementHeight = padding.top + (textBox.getHeight() - (int) textBoxHeight) + padding.bottom;

				}

			}

		}

	}

	protected TextBox splitTextBox(SplitInfo splitInfo, TextBox textBox, PageLayoutInfo pageLayoutInfo, int yMin, PageLayoutInfo newPageLayoutInfo) {

		TextBoxLayoutInfo layoutInfo = pageLayoutInfo.layoutInfo.textBoxes.get(textBox);
		if (layoutInfo != null && layoutInfo.textLayout != null && layoutInfo.textLayout.lines != null) {

			if (splitInfo.splitIndex > 0) {

				Line splitAtLine = layoutInfo.textLayout.lines[splitInfo.splitIndex];

				int startCorrection = splitAtLine.start;
				float yCorrection = splitAtLine.lineBounds.y;

				TextBox newTextBox = new TextBox(textBox);
				newTextBox.setText(newTextBox.getText().substring(splitAtLine.start));

				List<TextStyle> removeStyles = new ArrayList<>();

				for (TextStyle style : newTextBox.getStyles()) {

					if (style.start < startCorrection && style.end >= startCorrection) {
						style.start = 0;
						style.end -= startCorrection;
					} else if (style.start >= startCorrection) {
						style.start -= startCorrection;
						style.end -= startCorrection;
					} else {
						removeStyles.add(style);
					}

				}

				newTextBox.getStyles().removeAll(removeStyles);

				TextBoxLayoutInfo newLayoutInfo = new TextBoxLayoutInfo();
				newLayoutInfo.preferredHeight = layoutInfo.preferredHeight - (int) yCorrection;
				newLayoutInfo.textLayout = new TextLayout();
				newLayoutInfo.textLayout.lines = Arrays.copyOfRange(layoutInfo.textLayout.lines, splitInfo.splitIndex, layoutInfo.textLayout.lines.length);
				newLayoutInfo.textLayout.preferredWidth = layoutInfo.textLayout.preferredWidth;
				newLayoutInfo.textLayout.preferredHeight = layoutInfo.textLayout.preferredHeight - yCorrection;

				for (Line line : newLayoutInfo.textLayout.lines) {

					line.start -= startCorrection;
					line.baseline -= yCorrection;
					line.lineBounds.y -= yCorrection;

					if (line.spans != null) {

						for (Span span : line.spans) {

							span.style = new TextStyle(span.style);
							span.style.start -= startCorrection;
							span.style.end -= startCorrection;

							span.start -= startCorrection;
							span.end -= startCorrection;

						}

					}

				}

				newTextBox.setY(yMin);
				newTextBox.setHeight(newTextBox.getHeight() - (int) yCorrection);

				// Add new text-box to new page
				newPageLayoutInfo.page.getElements().add(newTextBox);
				newPageLayoutInfo.layoutInfo.textBoxes.put(newTextBox, newLayoutInfo);

				// Finally we have to update the original text-box
				textBox.setText(textBox.getText().substring(0, startCorrection));

				// Correct height
				Rect lastLineBounds = layoutInfo.textLayout.lines[layoutInfo.textLayout.lines.length - 1].lineBounds;
				yCorrection = (lastLineBounds.y + lastLineBounds.height) - splitAtLine.lineBounds.y;
				textBox.setHeight(textBox.getHeight() - (int) yCorrection);

				layoutInfo.preferredHeight = textBox.getHeight();
				layoutInfo.textLayout.lines = Arrays.copyOfRange(layoutInfo.textLayout.lines, 0, splitInfo.splitIndex);

				removeStyles = new ArrayList<>();

				for (TextStyle style : textBox.getStyles()) {

					if (style.start < startCorrection && style.end >= startCorrection) {
						style.end = startCorrection;
					} else if (style.start >= startCorrection) {
						removeStyles.add(style);
					}

				}

				textBox.getStyles().removeAll(removeStyles);

				/* TODO: Create unit test
				if (textBox.getHeight() != splitInfo.firstElementHeight) {
					System.err.println("TextBox height: " + textBox.getHeight() + " != " + splitInfo.firstElementHeight);
				}
				if (newTextBox.getHeight() != splitInfo.secondElementHeight) {
					System.err.println("New textBox height: " + newTextBox.getHeight() + " != " + splitInfo.secondElementHeight);
				}
				*/

				return newTextBox;

			}

		}

		return null;

	}

	protected void getTableSplitInfo(SplitInfo splitInfo, Table table, Pagination pagination, PageLayoutInfo pageLayoutInfo, int yMax) {

		TableLayoutInfo layoutInfo = pageLayoutInfo.layoutInfo.tables.get(table);
		if (layoutInfo != null && layoutInfo.rowCount > 1 && layoutInfo.rowBounds != null && table.getY() + table.getHeight() > yMax) {

			splitInfo.headerHeight = 0;
			for (int i = 0; i < table.getHeaderRows() && i < layoutInfo.rowBounds.length; i++) {
				splitInfo.headerHeight += layoutInfo.rowBounds[i].height;
			}

			// Determine at which row the table is to be split (splitAtRow is 1-based, rowBounds[i] is 0-based)
			// Skip the header rows (header cannot be split for now)
			int tableHeight = splitInfo.headerHeight;
			for (int i = table.getHeaderRows(); i < layoutInfo.rowCount; i++) {

				tableHeight += layoutInfo.rowBounds[i].height;

				if (i > table.getHeaderRows() && table.getY() + tableHeight > yMax) {

					// This row's bottom is over the max and has to be moved to a new table (add 1 to convert to 1-based row number)
					splitInfo.splitPossible = true;
					splitInfo.splitIndex = i + 1;

					break;

				} else if (i > table.getHeaderRows() && pagination != null && (table.getHeight() - tableHeight) + splitInfo.headerHeight < pagination.getMinHeight()) {

					// Remaining table will be too short, so stop here (add 1 to convert to 1-based row number)
					splitInfo.splitPossible = true;
					splitInfo.splitIndex = i + 1;

					break;

				} else {

					splitInfo.firstElementHeight = tableHeight;
					splitInfo.secondElementHeight = (table.getHeight() - tableHeight) + splitInfo.headerHeight;

				}

			}

		}

	}

	protected Table splitTable(SplitInfo splitInfo, Table table, PageLayoutInfo pageLayoutInfo, int yMin, PageLayoutInfo newPageLayoutInfo) {

		TableLayoutInfo layoutInfo = pageLayoutInfo.layoutInfo.tables.get(table);
		if (layoutInfo != null && layoutInfo.rowCount > 1 && layoutInfo.rowBounds != null) {

			// Text layout info can be null (SwingLayoutMetrics)
			boolean updateTextLayouts = layoutInfo.textLayouts != null;

			// Get the range of rows for the new table, fromRow is inclusive, toRow is exclusive, row numbers are 1-based
			int fromRow = splitInfo.splitIndex;
			int toRow = layoutInfo.rowCount + 1;

			if (fromRow > table.getHeaderRows() && toRow > fromRow) {

				// Create new table by duplicating the original table and removing all the rows that are not in the range fromRow - toRow
				Table newTable = new Table(table);

				// Create new layout info
				TableLayoutInfo newLayoutInfo = new TableLayoutInfo();
				newLayoutInfo.rowCount = (toRow - fromRow) + table.getHeaderRows();

				List<TextLayout> textLayouts = new ArrayList<>();
				List<TableCell> removeTableCells = new ArrayList<>();

				for (int i = 0; i < newTable.getTableCells().size(); i++) {

					TableCell tableCell = newTable.getTableCells().get(i);

					// For text layouts we need to include the header rows also (we can use the same TextLayout instance)
					if (updateTextLayouts) {
						if (tableCell.getRow() <= table.getHeaderRows() || (tableCell.getRow() >= fromRow && tableCell.getRow() < toRow)) {
							textLayouts.add(layoutInfo.textLayouts[i]);
						}
					}

					// Skip the header rows, we don't have to change their row numbers and we also don't have to remove them
					if (tableCell.getRow() > table.getHeaderRows()) {

						// For example: rows 10 - 19 have to be moved to new table (fromRow = 10, toRow = 20)
						// When we find row 10 we have to change it's row number to 2 (header row is 1)
						// (10 - 10) + 1 + 1 = 2 (add 1 for header row and add 1 because rows start at 1)
						if (tableCell.getRow() >= fromRow && tableCell.getRow() < toRow) {
							tableCell.setRow((tableCell.getRow() - fromRow) + table.getHeaderRows() + 1);
						} else {
							// All other rows need to be removed from this new table
							removeTableCells.add(tableCell);
						}

					}

				}

				if (updateTextLayouts) {
					newLayoutInfo.textLayouts = textLayouts.toArray(new TextLayout[0]);
				}

				newTable.getTableCells().removeAll(removeTableCells);

				newTable.setY(yMin);

				// Calculate height of the new table, subtract 1 because rowBounds is 0-based (bounds of row 11 are stored in rowBounds[10]),
				// toRow is exclusive, so we need to subtract 2, for example: if last row number is 100 then toRow is 101, bounds of
				// row number 100 are stored in rowBounds[99] so we need to use toRow - 2
				newTable.setHeight(splitInfo.headerHeight + ((layoutInfo.rowBounds[toRow - 2].y + layoutInfo.rowBounds[toRow - 2].height) - layoutInfo.rowBounds[fromRow - 1].y));

				// Update height of original table before row bounds are changed
				table.setHeight(layoutInfo.rowBounds[fromRow - 1].y);

				int yCorrection = layoutInfo.rowBounds[fromRow - 1].y - splitInfo.headerHeight;

				// Copy header row bounds from original layout info
				newLayoutInfo.rowBounds = new Bounds[newLayoutInfo.rowCount];
				for (int i = 0; i < table.getHeaderRows() && i < layoutInfo.rowBounds.length; i++) {
					newLayoutInfo.rowBounds[i] = layoutInfo.rowBounds[i];
				}

				// Copy row bounds from original layout info
				// For example 1 header row, table is split at row 11 (original table will have total of 10 rows)
				// rowBounds[10] contains first row that needs to be copied to new rowBounds[1]
				// i will be 1 here, fromRow will be 11, so 1 + 11 = 12 needs to be corrected to 10
				// we have to subtract 1 because fromRow is 1-based and we need to subtract the header row count
				for (int i = table.getHeaderRows(); i < newLayoutInfo.rowBounds.length; i++) {
					newLayoutInfo.rowBounds[i] = layoutInfo.rowBounds[i + (fromRow - (table.getHeaderRows() + 1))];
					newLayoutInfo.rowBounds[i].y -= yCorrection;
				}

				// Copy column bounds from original layout info
				newLayoutInfo.columnBounds = new Bounds[layoutInfo.columnBounds.length];
				for (int i = 0; i < layoutInfo.columnBounds.length; i++) {
					newLayoutInfo.columnBounds[i] = new Bounds(layoutInfo.columnBounds[i]);
					newLayoutInfo.columnBounds[i].height = newTable.getHeight();
				}

				// Add new table to new page
				newPageLayoutInfo.page.getElements().add(newTable);
				newPageLayoutInfo.layoutInfo.tables.put(newTable, newLayoutInfo);

				// Finally we have to update the original table
				// Remove all the cells that are outside the first tables range
				textLayouts = new ArrayList<>();
				removeTableCells = new ArrayList<>();

				for (int i = 0; i < table.getTableCells().size(); i++) {

					TableCell tableCell = table.getTableCells().get(i);

					if (tableCell.getRow() >= fromRow) {
						removeTableCells.add(tableCell);
					} else if (updateTextLayouts) {
						textLayouts.add(layoutInfo.textLayouts[i]);
					}

				}

				if (updateTextLayouts) {
					layoutInfo.textLayouts = textLayouts.toArray(new TextLayout[0]);
				}

				table.getTableCells().removeAll(removeTableCells);

				layoutInfo.preferredHeight = table.getHeight();
				layoutInfo.rowCount = fromRow - 1;

				// Update row bounds
				Bounds[] rowBounds = new Bounds[layoutInfo.rowCount];
				for (int i = 0; i < fromRow - 1; i++) {
					rowBounds[i] = layoutInfo.rowBounds[i];
				}
				layoutInfo.rowBounds = rowBounds;

				// Set height values for column-bounds
				for (int i = 0; i < layoutInfo.columnBounds.length; i++) {
					layoutInfo.columnBounds[i].height = table.getHeight();
				}

				/* TODO: Create unit test
				if (table.getHeight() != splitInfo.firstElementHeight) {
					System.err.println("Table height: " + table.getHeight() + " != " + splitInfo.firstElementHeight);
				}
				if (newTable.getHeight() != splitInfo.secondElementHeight) {
					System.err.println("New table height: " + newTable.getHeight() + " != " + splitInfo.secondElementHeight);
				}
				*/

				return newTable;

			}

		}

		return null;

	}

	protected static class SplitInfo {

		protected boolean splitPossible = false;
		protected int splitIndex = 0;
		protected int headerHeight = 0; // TODO? Only used for tables..
		protected int firstElementHeight = 0;
		protected int secondElementHeight = 0;

		protected SplitInfo() {

		}

	}

	private void fireStatusChanged(String status) {
		if (listener != null) {
			listener.statusChanged(status);
		}
	}

}
