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

import org.glasspath.aerialist.ColStyle;
import org.glasspath.aerialist.Element;
import org.glasspath.aerialist.Group;
import org.glasspath.aerialist.HeightPolicy;
import org.glasspath.aerialist.Image;
import org.glasspath.aerialist.Padding;
import org.glasspath.aerialist.QrCode;
import org.glasspath.aerialist.Table;
import org.glasspath.aerialist.TableCell;
import org.glasspath.aerialist.TextBox;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.Bounds;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.ElementLayoutInfo;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.ImageLayoutInfo;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.LayoutInfo;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.TableLayoutInfo;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.TextBoxLayoutInfo;
import org.glasspath.aerialist.media.MediaCache.ImageSize;
import org.glasspath.aerialist.text.TextLayout;
import org.glasspath.aerialist.text.TextUtils;

public class DefaultLayoutMetrics implements IElementLayoutMetrics {

	private final ILayoutContext<?> layoutContext;
	private LayoutInfo layoutInfo = new LayoutInfo();

	public DefaultLayoutMetrics(ILayoutContext<?> layoutContext) {
		this.layoutContext = layoutContext;
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

			info = new TextBoxLayoutInfo();
			info.preferredHeight = textBox.getHeight();

			if (layoutContext.getFontCache() != null) {

				Padding padding = new Padding(textBox.getPadding());

				info.textLayout = TextUtils.createTextLayout(textBox, layoutContext.getFontCache(), textBox.getWidth() - (padding.left + padding.right));

				if (HeightPolicy.get(textBox.getHeightPolicy()) == HeightPolicy.AUTO) {
					// TODO: Round values like 9.1 up to 10?
					info.preferredHeight = (int) (padding.top + info.textLayout.preferredHeight + padding.bottom);
				}

			}

			layoutInfo.textBoxes.put(textBox, info);

		}

		return info;

	}

	protected TableLayoutInfo getTableLayoutInfo(Table table) {

		TableLayoutInfo info = layoutInfo.tables.get(table);

		if (info == null) {

			info = new TableLayoutInfo();
			info.preferredHeight = table.getHeight();

			if (layoutContext.getFontCache() != null && table.getTableCells().size() > 0 && table.getColStyles().size() > 0) {

				// Get total number of rows and columns
				int lastRow = 0;
				int lastCol = 0;
				for (TableCell tableCell : table.getTableCells()) {

					int row = tableCell.getRow() + (tableCell.getRowSpan() - 1);
					if (row > lastRow) {
						lastRow = row;
					}

					int col = tableCell.getCol() + (tableCell.getColSpan() - 1);
					if (col > lastCol) {
						lastCol = col;
					}

				}

				// Set row count
				info.rowCount = lastRow;

				if (lastRow > 0 && lastCol > 0) {

					Padding cellPadding = new Padding(table.getCellPadding());

					info.rowBounds = new Bounds[lastRow];
					for (int i = 0; i < info.rowBounds.length; i++) {
						info.rowBounds[i] = new Bounds();
					}

					info.columnBounds = new Bounds[lastCol];
					for (int i = 0; i < info.columnBounds.length; i++) {
						info.columnBounds[i] = new Bounds();
					}

					// Set the column widths
					for (ColStyle colStyle : table.getColStyles()) {

						int col = colStyle.col - 1;
						if (col >= 0 && col < info.columnBounds.length) {
							info.columnBounds[col] = new Bounds();
							info.columnBounds[col].width = colStyle.width;
						}

					}

					// Set x values for column-bounds
					for (int i = 1; i < info.columnBounds.length; i++) {
						info.columnBounds[i].x = info.columnBounds[i - 1].x + info.columnBounds[i - 1].width;
					}

					// Check if the last column width was set
					int availableWidth = table.getWidth() - info.columnBounds[info.columnBounds.length - 1].x;
					if (availableWidth > info.columnBounds[info.columnBounds.length - 1].width) {
						info.columnBounds[info.columnBounds.length - 1].width = availableWidth;
					}

					// Create text layouts, pass column width as width limit
					info.textLayouts = new TextLayout[table.getTableCells().size()];
					for (int i = 0; i < table.getTableCells().size(); i++) {

						TableCell tableCell = table.getTableCells().get(i);

						int col = tableCell.getCol() - 1;
						if (col >= 0 && col < info.columnBounds.length) {
							info.textLayouts[i] = TextUtils.createTextLayout(tableCell, layoutContext.getFontCache(), info.columnBounds[col].width - (cellPadding.left + cellPadding.right));
						}

					}

					// Find the max. height for each row and use it as row height
					for (int i = 0; i < table.getTableCells().size(); i++) {

						TableCell tableCell = table.getTableCells().get(i);
						int row = tableCell.getRow() - 1;
						int col = tableCell.getCol() - 1;

						if (row >= 0 && row < info.rowBounds.length && col >= 0 && col < info.columnBounds.length) {

							TextLayout textLayout = info.textLayouts[i];

							float height = cellPadding.top + textLayout.preferredHeight + cellPadding.bottom;
							if (height > info.rowBounds[row].height) {
								info.rowBounds[row].height = (int) height; // TODO: Round?
							}

							if (tableCell.getColSpan() > 1) {
								System.err.println("TODO: DefaultLayoutMetrics, calculate row height for cell's with colSpan > 1");
							}

						}

					}

					// Set width and y values for row-bounds
					for (int i = 0; i < info.rowBounds.length; i++) {
						info.rowBounds[i].width = table.getWidth();
						if (i > 0) {
							info.rowBounds[i].y = info.rowBounds[i - 1].y + info.rowBounds[i - 1].height;
						}
					}

					// Get total height from the last row bounds y + height
					info.preferredHeight = info.rowBounds[info.rowBounds.length - 1].y + info.rowBounds[info.rowBounds.length - 1].height;

					// Set height values for column-bounds
					for (int i = 0; i < info.columnBounds.length; i++) {
						info.columnBounds[i].height = info.preferredHeight;
					}

				}

			}

			layoutInfo.tables.put(table, info);

		}

		return info;

	}

	protected ImageLayoutInfo getImageLayoutInfo(Image image) {

		ImageLayoutInfo info = layoutInfo.images.get(image);

		if (info == null) {

			info = new ImageLayoutInfo();
			info.preferredHeight = image.getHeight();

			ImageSize imageSize = layoutContext.getMediaCache().getImageSize(image.getSrc());
			if (imageSize != null) {

				info.imageWidth = imageSize.width;
				info.imageHeight = imageSize.height;
				info.preferredHeight = IElementLayoutMetrics.getPreferredImageHeight(image, imageSize.width, imageSize.height);

			}

			layoutInfo.images.put(image, info);

		}

		return info;

	}

	protected ElementLayoutInfo getQrCodeLayoutInfo(QrCode qrCode) {
		return new ElementLayoutInfo(qrCode.getHeight());
	}

	protected ElementLayoutInfo getGroupLayoutInfo(Group group) {
		return new ElementLayoutInfo(group.getHeight());
	}

}
