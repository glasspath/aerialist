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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JPanel;

import org.glasspath.aerialist.Border;
import org.glasspath.aerialist.BorderType;
import org.glasspath.aerialist.ColStyle;
import org.glasspath.aerialist.HeightPolicy;
import org.glasspath.aerialist.Padding;
import org.glasspath.aerialist.RowStyle;
import org.glasspath.aerialist.Table;
import org.glasspath.aerialist.TableCell;
import org.glasspath.aerialist.YPolicy;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.Bounds;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.TableLayoutInfo;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

public class TableView extends JPanel implements ISwingElementView<Table> {

	private final ISwingViewContext viewContext;
	private YPolicy yPolicy = YPolicy.DEFAULT;
	private HeightPolicy heightPolicy = HeightPolicy.DEFAULT;
	private Color backgroundColor = null;
	private final List<Border> borders = new ArrayList<>();
	private List<ColStyle> colStyles = new ArrayList<>();
	private final List<RowStyle> rowStyles = new ArrayList<>();
	private final List<TableCellView> tableCellViews = new ArrayList<>();
	private Padding cellPadding = new Padding();
	private boolean verticalFillEnabled = false;
	private int rowCount = 0;
	private int columnCount = 0;
	private TableViewLayout tableLayout = null;
	private Rectangle[] rowBounds = null;
	private Rectangle[] columnBounds = null;
	private ComponentProxy componentProxy = null;

	public TableView(ISwingViewContext viewContext) {

		this.viewContext = viewContext;

		setOpaque(false);

		setFocusable(true);
		addFocusListener(new FocusAdapter() {

			@Override
			public void focusGained(FocusEvent e) {
				viewContext.focusGained(TableView.this);
			}
		});
		addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				requestFocusInWindow();
			}
		});

	}

	@Override
	public void init(Table element) {
		init(element, null);
	}

	public void init(Table element, ComponentProxy componentProxy) {

		yPolicy = YPolicy.get(element.getYPolicy());
		heightPolicy = HeightPolicy.get(element.getHeightPolicy());

		setBackgroundColor(ColorUtils.fromHex(element.getBackground()));

		borders.clear();
		for (Border border : element.getBorders()) {
			borders.add(new Border(border));
		}

		// Bounds are also set here because layoutTableCells() needs to know the width of the table
		setBounds(element.getX(), element.getY(), element.getWidth(), element.getHeight());

		colStyles.clear();
		for (ColStyle colStyle : element.getColStyles()) {
			colStyles.add(new ColStyle(colStyle));
		}

		rowStyles.clear();
		for (RowStyle rowStyle : element.getRowStyles()) {
			rowStyles.add(new RowStyle(rowStyle));
		}

		cellPadding.parse(element.getCellPadding());

		createTableCells(element);
		layoutTableCells();

	}

	@Override
	public YPolicy getYPolicy() {
		return yPolicy;
	}

	@Override
	public void setYPolicy(YPolicy yPolicy) {
		this.yPolicy = yPolicy;
	}

	@Override
	public HeightPolicy getHeightPolicy() {
		return heightPolicy;
	}

	@Override
	public void setHeightPolicy(HeightPolicy heightPolicy) {
		this.heightPolicy = heightPolicy;
	}

	@Override
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	@Override
	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	@Override
	public List<Border> getBorders() {
		return borders;
	}

	@Override
	public Table toElement() {

		Table table = new Table();
		ISwingElementView.copyProperties(this, table);

		table.getColStyles().addAll(updateColStyles());
		table.getRowStyles().addAll(getRowStylesCopy());

		for (TableCellView tableCellView : tableCellViews) {
			table.getTableCells().add(tableCellView.toTableCell());
		}

		table.setCellPadding(cellPadding.toString());

		Collections.sort(table.getTableCells(), new Comparator<TableCell>() {

			@Override
			public int compare(TableCell cell1, TableCell cell2) {
				if (cell1.getRow() == cell2.getRow()) {
					return Integer.compare(cell1.getCol(), cell2.getCol());
				} else {
					return Integer.compare(cell1.getRow(), cell2.getRow());
				}
			}
		});

		return table;

	}

	public TableLayoutInfo getTableLayoutInfo(boolean updateHeight) {

		TableLayoutInfo info = new TableLayoutInfo();
		info.preferredHeight = getPreferredSize().height;
		info.rowCount = rowCount;

		if (updateHeight) {
			Rectangle bounds = getBounds();
			bounds.height = info.preferredHeight;
			setBounds(bounds);
		}

		if (rowCount > 0) {

			updateCellBounds();

			info.rowBounds = new Bounds[rowBounds.length];
			for (int i = 0; i < rowBounds.length; i++) {
				info.rowBounds[i] = new Bounds(rowBounds[i].x, rowBounds[i].y, rowBounds[i].width, rowBounds[i].height);
			}

			info.columnBounds = new Bounds[columnBounds.length];
			for (int i = 0; i < columnBounds.length; i++) {
				info.columnBounds[i] = new Bounds(columnBounds[i].x, columnBounds[i].y, columnBounds[i].width, columnBounds[i].height);
			}

		}

		return info;

	}

	public void removeColStyle(int col) {

		int indexToRemove = -1;

		for (int i = 0; i < colStyles.size(); i++) {

			ColStyle colStyle = colStyles.get(i);
			if (colStyle.col == col) {
				indexToRemove = i;
			} else if (colStyle.col > col) {
				colStyle.col--;
			}

		}

		if (indexToRemove >= 0) {
			colStyles.remove(indexToRemove);
		}

	}

	public List<ColStyle> updateColStyles() {

		if (!tableLayout.isSizeArrayValid()) {
			doLayout();
		}

		List<ColStyle> oldColStyles = new ArrayList<>();
		oldColStyles.addAll(colStyles);
		colStyles.clear();

		for (int col = 0; col < columnCount - 1; col++) {

			ColStyle colStyle = null;

			for (ColStyle cs : oldColStyles) {
				if (cs.col == col + 1) {
					colStyle = cs;
					break;
				}
			}

			if (colStyle == null) {

				colStyle = new ColStyle();
				colStyle.col = col + 1;

			}

			colStyle.width = tableLayout.getColumnWidth(col);
			colStyles.add(colStyle);

		}

		return getColStylesCopy();

	}

	public List<ColStyle> getColStyles() {
		return colStyles;
	}

	public List<ColStyle> getColStylesCopy() {

		List<ColStyle> colStylesCopy = new ArrayList<>();
		for (ColStyle colStyle : colStyles) {
			colStylesCopy.add(new ColStyle(colStyle));
		}

		return colStylesCopy;

	}

	public void setColStyles(List<ColStyle> colStyles) {
		this.colStyles = colStyles;
	}

	public List<RowStyle> getRowStyles() {
		return rowStyles;
	}

	public List<RowStyle> getRowStylesCopy() {
		List<RowStyle> rowStylesCopy = new ArrayList<>();
		for (RowStyle rowStyle : rowStyles) {
			rowStylesCopy.add(new RowStyle(rowStyle));
		}
		return rowStylesCopy;
	}

	public void applyRowStyles(List<RowStyle> rowStyles) {
		this.rowStyles.clear();
		for (RowStyle rowStyle : rowStyles) {
			this.rowStyles.add(new RowStyle(rowStyle));
		}
	}

	public ISwingViewContext getViewContext() {
		return viewContext;
	}

	public List<TableCellView> getTableCellViews() {
		return tableCellViews;
	}

	public Padding getCellPadding() {
		return cellPadding;
	}

	public void applyCellPadding(Padding cellPadding) {

		this.cellPadding.from(cellPadding);

		for (TableCellView tableCell : tableCellViews) {
			tableCell.applyCellPadding(cellPadding);
		}

		layoutTableCells();

	}

	public boolean isVerticalFillEnabled() {
		return verticalFillEnabled;
	}

	public void setVerticalFillEnabled(boolean verticalFillEnabled) {
		this.verticalFillEnabled = verticalFillEnabled;
	}

	public int getRowCount() {
		return rowCount;
	}

	public int getColumnCount() {
		return columnCount;
	}

	public TableViewLayout getTableLayout() {
		return tableLayout;
	}

	private void createTableCells(Table table) {

		tableCellViews.clear();

		if (componentProxy == null) {

			TableCellView tableCellView;
			for (TableCell tableCell : table.getTableCells()) {
				tableCellView = new TableCellView(viewContext);
				tableCellView.init(tableCell, cellPadding);
				tableCellViews.add(tableCellView);
			}

		} else {

			rowCount = 0;
			columnCount = 0;

			int row, col;
			for (TableCell tableCell : table.getTableCells()) {

				row = tableCell.getRow() + (tableCell.getRowSpan() - 1);
				if (row > rowCount) {
					rowCount = row;
				}

				col = tableCell.getCol() + (tableCell.getColSpan() - 1);
				if (col > columnCount) {
					columnCount = col;
				}

			}

		}

	}

	public void layoutTableCells() {

		removeAll();

		int row, col;

		if (componentProxy == null) {

			rowCount = 0;
			columnCount = 0;

			for (TableCellView tableCellView : tableCellViews) {

				row = tableCellView.getRow() + (tableCellView.getRowSpan() - 1);
				if (row > rowCount) {
					rowCount = row;
				}

				col = tableCellView.getCol() + (tableCellView.getColSpan() - 1);
				if (col > columnCount) {
					columnCount = col;
				}

			}

		}

		if (rowCount > 0 && columnCount > 0) {

			double[] rowHeights = new double[rowCount];
			double[] columnWidths = new double[columnCount];

			for (row = 0; row < rowCount; row++) {
				rowHeights[row] = verticalFillEnabled ? TableLayout.FILL : TableLayout.PREFERRED;
			}

			int width = getBounds().width;
			int columnWidth = width / columnCount;
			int remainingWidth = width;

			for (col = 0; col < columnCount; col++) {

				if (col == columnCount - 1) {
					columnWidth = remainingWidth;
				}

				columnWidths[col] = columnWidth;
				remainingWidth -= columnWidth;

			}

			columnWidths[columnCount - 1] = TableLayout.FILL;

			if (colStyles != null) {

				for (ColStyle colStyle : colStyles) {

					col = colStyle.col - 1;
					if (col >= 0 && col < columnCount - 1 && colStyle.width > 0) {
						columnWidths[col] = colStyle.width;
					}

				}

			}

			tableLayout = new TableViewLayout(columnWidths, rowHeights);
			setLayout(tableLayout);

			if (componentProxy == null) {

				for (TableCellView tableCellView : tableCellViews) {

					row = tableCellView.getRow() - 1;
					col = tableCellView.getCol() - 1;

					add(tableCellView, new TableLayoutConstraints(col, row, col + (tableCellView.getColSpan() - 1), row + (tableCellView.getRowSpan() - 1)));

				}

			}

			/* First attempt to own implementation, colSpans and rowSpans make things a little harder
			
			setLayout(null);
			
			rowHeights = new int[rowCount];
			columnWidths = new int[columnCount];
			
			rowOffsets = new int[rowCount];
			columnOffsets = new int[columnCount];
			
			// TODO
			int width = getBounds().width;
			int columnWidth = width / columnCount;
			int remainingWidth = width;
			
			for (col = 0; col < columnCount; col++) {
			
				if (col == columnCount - 1) {
					columnWidth = remainingWidth;
				}
			
				columnWidths[col] = columnWidth;
				remainingWidth -= columnWidth;
			
				if (col < columnCount - 1) {
					columnOffsets[col + 1] = columnOffsets[col] + columnWidth;
				}
			
			}
			
			Rectangle bounds;
			int x, y, rowHeight;
			
			for (TableCellView tableCellView : tableCellViews) {
			
				row = tableCellView.getRow() - 1;
				col = tableCellView.getCol() - 1;
			
				if (row >= 0 && row < rowCount && col >= 0 && col < columnCount) {
			
					x = columnOffsets[col];
			
					columnWidth = columnWidths[col];
					for (int i = col; i < col + (tableCellView.getColSpan() - 1); i++) {
						columnWidth += columnWidths[i];
					}
			
					bounds = new Rectangle(x, 0, columnWidth, 10);
					//System.out.println(bounds);
			
					tableCellView.setBounds(bounds);
			
					rowHeight = tableCellView.getPreferredSize().height;
					System.out.println(rowHeight);
					if (rowHeight > rowHeights[row]) {
						rowHeights[row] = rowHeight;
					}
			
					add(tableCellView);
			
				}
			
			}
			
			for (row = 0; row < rowCount - 1; row++) {
				rowOffsets[row + 1] = rowOffsets[row] + rowHeights[row];
			}
			
			for (TableCellView tableCellView : tableCellViews) {
			
				row = tableCellView.getRow() - 1;
				col = tableCellView.getCol() - 1;
			
				if (row >= 0 && row < rowCount && col >= 0 && col < columnCount) {
			
					y = rowOffsets[row];
			
					bounds = tableCellView.getBounds();
					bounds.y = y;
					bounds.height = rowHeights[row];
					//System.out.println(bounds);
			
					tableCellView.setBounds(bounds);
			
				}
			
			}
			*/

		}

	}

	@Override
	public void doLayout() {
		super.doLayout();
		updateCellBounds();
	}

	public void updateCellBounds() {

		if (rowCount > 0) {

			rowBounds = new Rectangle[rowCount];

			int w = getWidth();
			int h = 0;
			int y = 0;

			for (int row = 0; row < rowCount; row++) {
				h = tableLayout.getRowHeight(row);
				if (row >= 0 && row < rowBounds.length) {
					rowBounds[row] = new Rectangle(0, y, w, h);
				}
				y += h;
			}

		} else {
			rowBounds = null;
		}

		if (columnCount > 0) {

			columnBounds = new Rectangle[columnCount];

			int w = 0;
			int h = getHeight();
			int x = 0;

			for (int col = 0; col < columnCount; col++) {
				w = tableLayout.getColumnWidth(col);
				if (col >= 0 && col < columnBounds.length) {
					columnBounds[col] = new Rectangle(x, 0, w, h);
				}
				x += w;
			}

		} else {
			columnBounds = null;
		}

	}

	@Override
	public int getComponentCount() {
		if (componentProxy != null) {
			return componentProxy.getComponentCount();
		} else {
			return super.getComponentCount();
		}
	}

	@Override
	public Component getComponent(int n) {
		if (componentProxy != null) {
			return componentProxy.getComponent(n);
		} else {
			return super.getComponent(n);
		}
	}

	@Override
	public void paint(Graphics g) {

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (backgroundColor != null) {
			g2d.setColor(backgroundColor);
			g2d.fillRect(0, 0, getWidth(), getHeight());
		}

		// TODO! (For now vertical fill is only used for email layout)
		if (!verticalFillEnabled) {

			/*
			if (g2d.getClip() instanceof Rectangle2D.Double) {
			
				Rectangle2D.Double clip = (Rectangle2D.Double) g2d.getClip();
				clip.x -= 1;
				clip.y -= 1;
				clip.width += 2;
				clip.height += 2;
				g2d.setClip(clip);
			
			} else if (g2d.getClip() instanceof Rectangle) {
			
				Rectangle clip = (Rectangle) g2d.getClip();
				clip.x -= 1;
				clip.y -= 1;
				clip.width += 2;
				clip.height += 2;
				g2d.setClip(clip);
			
			}
			*/

			if (rowBounds != null && columnBounds != null) {

				// Row background

				// Repeating row background should be painted first
				List<RowStyle> sortedRowStyles = new ArrayList<>();
				sortedRowStyles.addAll(rowStyles);
				Collections.sort(sortedRowStyles, new Comparator<RowStyle>() {

					@Override
					public int compare(RowStyle rs1, RowStyle rs2) {
						return Integer.compare(rs2.repeat, rs1.repeat);
					}
				});

				for (RowStyle rowStyle : sortedRowStyles) {

					int row = rowStyle.row - 1;
					if (row >= 0) {

						Color color = ColorUtils.fromHex(rowStyle.background);
						if (color != null) {

							g2d.setColor(color);

							if (rowStyle.repeat > 0) {

								for (int i = row; i < rowBounds.length; i += rowStyle.repeat) {
									if (rowBounds[i] != null) {
										g2d.fill(rowBounds[i]);
									}
								}

							} else if (row < rowBounds.length && rowBounds[row] != null) {
								g2d.fill(rowBounds[row]);
							}

						}

					}

				}

				Rectangle rect = new Rectangle(0, 0, getWidth(), getHeight());
				BorderUtils.paintBorders(g2d, borders, rect);

				// Table borders
				for (Border border : borders) {

					Color color = ColorUtils.fromHex(border.color);
					if (color != null && border.width > 0.0) {

						g2d.setStroke(new BasicStroke(border.width));
						g2d.setColor(color);

						switch (BorderType.get(border.type)) {

						case VERTICAL:
							for (int col = 0; col < columnBounds.length - 1; col++) {
								rect = columnBounds[col];
								if (rect != null) {
									g2d.drawLine(rect.x + (rect.width - 1), rect.y, rect.x + (rect.width - 1), rect.y + (rect.height - 1));
								}
							}
							break;

						case HORIZONTAL:
							if (rowBounds != null) {
								for (int row = 0; row < rowBounds.length - 1; row++) {
									rect = rowBounds[row];
									if (rect != null) {
										g2d.drawLine(rect.x, rect.y + (rect.height - 1), rect.x + (rect.width - 1), rect.y + (rect.height - 1));
									}
								}
							}
							break;

						default:
							break;
						}

					}

				}

				// Column borders
				for (ColStyle colStyle : colStyles) {

					int col = colStyle.col - 1;
					if (col >= 0 && col < columnBounds.length && columnBounds[col] != null) {

						rect = columnBounds[col];
						if (col > 0) {
							rect.x += 1;
							rect.width -= 1;
						}

						BorderUtils.paintBorders(g2d, colStyle.borders, columnBounds[col]);

					}

				}

			}

			super.paint(g);

		} else {
			super.paint(g);
		}

	}

	public class TableViewLayout extends TableLayout {

		public TableViewLayout(double[] col, double[] row) {
			super(col, row);
		}

		@Override
		protected void calculateSize(Container container) {
			super.calculateSize(container);
		}

		public boolean isSizeArrayValid() {
			if (crSize != null && crSize.length == 2 && crSize[0] != null && crSize[1] != null) {
				return true;
			} else {
				return false;
			}
		}

		public int getColumnWidth(int column) {
			if (column >= 0 && column < crSize[C].length) {
				return crSize[C][column];
			} else {
				return -1;
			}
		}

		public int getRowHeight(int row) {
			if (row >= 0 && row < crSize[R].length) {
				return crSize[R][row];
			} else {
				return -1;
			}
		}

		public void setColumnOffset(int column, int offset) {

			if (column > 0 && column < columnCount) {

				int columnWidth = getColumnWidth(column);
				int leftColumnWidth = getColumnWidth(column - 1);

				int currentOffset = 0;
				for (int col = 0; col < column; col++) {
					currentOffset += getColumnWidth(col);
				}

				// TODO: TableLayout returns width = 0 for last column
				if (column == columnCount - 1) {
					columnWidth = getWidth() - currentOffset;
				}

				int delta = offset - currentOffset;
				columnWidth -= delta;
				leftColumnWidth += delta;

				if (columnWidth >= 5 && leftColumnWidth >= 5) {

					if (column < columnCount - 1) {
						setColumn(column, columnWidth);
					}
					setColumn(column - 1, leftColumnWidth);

				}

			}

		}

	}

	public interface ComponentProxy {

		public int getComponentCount();

		public Component getComponent(int n);

	}

}
