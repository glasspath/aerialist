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

import org.glasspath.aerialist.Element;
import org.glasspath.aerialist.HeightPolicy;
import org.glasspath.aerialist.Padding;
import org.glasspath.aerialist.Page;
import org.glasspath.aerialist.Page.PageSize;
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

	public List<PageLayoutInfo> paginate(PageLayoutInfo pageLayoutInfo, int yMin, int yMax) {

		List<PageLayoutInfo> newPages = new ArrayList<>();

		if (pageLayoutInfo.layoutInfo != null) {

			Page page = pageLayoutInfo.page;

			PageLayoutInfo newPageLayoutInfo = null;

			// Move elements to new page, this has to be done before splitting elements
			// so the VerticalLayout can do it's work
			List<Element> moveElements = new ArrayList<>();
			for (int i = 0; i < page.getElements().size(); i++) {

				Element element = page.getElements().get(i);

				if (element.getY() > yMax) {
					moveElements.add(element);
				} else if (element.getY() > yMin && element.getY() + element.getHeight() > yMax && !isSplittable(element)) {
					moveElements.add(element);
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

				if (element.getY() <= yMax && element.getY() + element.getHeight() > yMax) {

					if (HeightPolicy.get(element.getHeightPolicy()) == HeightPolicy.AUTO) {

						if (newPageLayoutInfo == null) {

							newPageLayoutInfo = new PageLayoutInfo(new Page(PageSize.A4)); // TODO
							newPageLayoutInfo.layoutInfo = new LayoutInfo();

							newPages.add(newPageLayoutInfo);

							layout.setContainer(newPageLayoutInfo.page, false);

						}

						Element newElement = split(element, pageLayoutInfo, yMin, yMax, newPageLayoutInfo);
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
					newPages.addAll(paginate(newPageLayoutInfo, yMin, yMax));
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

	protected boolean isSplittable(Element element) {

		if (HeightPolicy.get(element.getHeightPolicy()) == HeightPolicy.AUTO) {
			if (element instanceof TextBox) {
				return true;
			} else if (element instanceof Table) {
				return true;
			}
		}

		return false;

	}

	protected Element split(Element element, PageLayoutInfo pageLayoutInfo, int yMin, int yMax, PageLayoutInfo newPageLayoutInfo) {

		Element newElement = null;

		// TODO: Implement on all elements that can span multiple pages
		if (element instanceof TextBox) {
			newElement = splitTextBox((TextBox) element, pageLayoutInfo, yMin, yMax, newPageLayoutInfo);
		} else if (element instanceof Table) {
			newElement = splitTable((Table) element, pageLayoutInfo, yMin, yMax, newPageLayoutInfo);
		}

		return newElement;

	}

	protected TextBox splitTextBox(TextBox textBox, PageLayoutInfo pageLayoutInfo, int yMin, int yMax, PageLayoutInfo newPageLayoutInfo) {

		TextBoxLayoutInfo layoutInfo = pageLayoutInfo.layoutInfo.textBoxes.get(textBox);
		if (layoutInfo != null && layoutInfo.textLayout != null && layoutInfo.textLayout.lines != null && textBox.getY() + textBox.getHeight() > yMax) {

			int splitAtLineIndex = -1;
			Padding padding = new Padding(textBox.getPadding());

			// Determine at which line the txt-box is to be split
			for (int i = 0; i < layoutInfo.textLayout.lines.length; i++) {

				Rect lineBounds = layoutInfo.textLayout.lines[i].lineBounds;

				float yBottom = textBox.getY() + lineBounds.y + lineBounds.height;
				if (yBottom + padding.bottom > yMax) {

					splitAtLineIndex = i;

					break;

				}

			}

			if (splitAtLineIndex > 0) {

				Line splitAtLine = layoutInfo.textLayout.lines[splitAtLineIndex];

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
				newLayoutInfo.textLayout.lines = Arrays.copyOfRange(layoutInfo.textLayout.lines, splitAtLineIndex, layoutInfo.textLayout.lines.length);
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
				layoutInfo.textLayout.lines = Arrays.copyOfRange(layoutInfo.textLayout.lines, 0, splitAtLineIndex);

				removeStyles = new ArrayList<>();

				for (TextStyle style : textBox.getStyles()) {

					if (style.start < startCorrection && style.end >= startCorrection) {
						style.end = startCorrection;
					} else if (style.start >= startCorrection) {
						removeStyles.add(style);
					}

				}

				textBox.getStyles().removeAll(removeStyles);

				return newTextBox;

			}

		}

		return null;

	}

	protected Table splitTable(Table table, PageLayoutInfo pageLayoutInfo, int yMin, int yMax, PageLayoutInfo newPageLayoutInfo) {

		TableLayoutInfo layoutInfo = pageLayoutInfo.layoutInfo.tables.get(table);
		if (layoutInfo != null && layoutInfo.rowCount > 1 && layoutInfo.rowBounds != null && table.getY() + table.getHeight() > yMax) {

			// Text layout info can be null (SwingLayoutMetrics)
			boolean updateTextLayouts = layoutInfo.textLayouts != null;

			// TODO: For now we only support 1 header row which is repeated on each page by default
			int headerRowHeight = layoutInfo.rowBounds[0].height;

			// Determine at which row the table is to be split (splitAtRow is 1-based, rowBounds[i] is 0-based)
			// Start at 1 because rowBounds[0] contains the bounds of the header row
			int splitAtRow = -1;
			int yBottom = table.getY() + headerRowHeight;
			for (int i = 1; i < layoutInfo.rowCount; i++) {

				yBottom += layoutInfo.rowBounds[i].height;
				if (yBottom > yMax) {

					// This row's bottom is over the max and has to be moved to a new table, add 1 to convert to 1-based row number
					splitAtRow = i + 1;

					break;

				}

			}

			// Get the range of rows for the new table, fromRow is inclusive, toRow is exclusive, row numbers are 1-based
			int fromRow = splitAtRow;
			int toRow = layoutInfo.rowCount + 1;

			if (fromRow > 1 && toRow > fromRow) {

				// Create new table by duplicating the original table and removing all the rows that are not in the range fromRow - toRow
				Table newTable = new Table(table);

				// Create new layout info
				TableLayoutInfo newLayoutInfo = new TableLayoutInfo();
				newLayoutInfo.rowCount = (toRow - fromRow) + 1; // TODO: Make repeating of header row configurable?

				List<TextLayout> textLayouts = new ArrayList<>();
				List<TableCell> removeTableCells = new ArrayList<>();

				for (int i = 0; i < newTable.getTableCells().size(); i++) {

					TableCell tableCell = newTable.getTableCells().get(i);

					// For text layouts we need to include the header also (we can use the same TextLayout instance)
					if (updateTextLayouts) {
						if (tableCell.getRow() == 1 || (tableCell.getRow() >= fromRow && tableCell.getRow() < toRow)) {
							textLayouts.add(layoutInfo.textLayouts[i]);
						}
					}

					// Skip the header row, we don't have to change it's row number and we also don't have to remove it
					if (tableCell.getRow() > 1) {

						// For example: rows 10 - 19 have to be moved to new table (fromRow = 10, toRow = 20)
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

				if (updateTextLayouts) {
					newLayoutInfo.textLayouts = textLayouts.toArray(new TextLayout[0]);
				}

				newTable.getTableCells().removeAll(removeTableCells);

				newTable.setY(yMin);

				// Calculate height of the new table, subtract 1 because rowBounds is 0-based (bounds of row 1 are stored in rowBounds[0]),
				// toRow is exclusive, so we need to subtract 2, for example: if last row number is 100 then toRow is 101, bounds of
				// row number 100 are stored in rowBounds[99] so we need to use toRow - 2
				newTable.setHeight(headerRowHeight + ((layoutInfo.rowBounds[toRow - 2].y + layoutInfo.rowBounds[toRow - 2].height) - layoutInfo.rowBounds[fromRow - 1].y));

				// Update height of original table before row-bounds are changed
				table.setHeight(layoutInfo.rowBounds[fromRow - 1].y);

				int yCorrection = layoutInfo.rowBounds[fromRow - 1].y - layoutInfo.rowBounds[0].height;

				// Copy row bounds from original layout info
				newLayoutInfo.rowBounds = new Bounds[newLayoutInfo.rowCount];
				newLayoutInfo.rowBounds[0] = layoutInfo.rowBounds[0];
				for (int i = 1; i < newLayoutInfo.rowBounds.length; i++) {
					newLayoutInfo.rowBounds[i] = layoutInfo.rowBounds[i + (fromRow - 2)];
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
				layoutInfo.rowCount = splitAtRow - 1;

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

				return newTable;

			}

		}

		return null;

	}

	private void fireStatusChanged(String status) {
		if (listener != null) {
			listener.statusChanged(status);
		}
	}

}
