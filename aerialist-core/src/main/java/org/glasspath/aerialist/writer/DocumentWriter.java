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
package org.glasspath.aerialist.writer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.glasspath.aerialist.Alignment;
import org.glasspath.aerialist.Border;
import org.glasspath.aerialist.BorderType;
import org.glasspath.aerialist.ColStyle;
import org.glasspath.aerialist.Element;
import org.glasspath.aerialist.FitPolicy;
import org.glasspath.aerialist.Group;
import org.glasspath.aerialist.Image;
import org.glasspath.aerialist.Padding;
import org.glasspath.aerialist.QrCode;
import org.glasspath.aerialist.RowStyle;
import org.glasspath.aerialist.Table;
import org.glasspath.aerialist.TableCell;
import org.glasspath.aerialist.TextBox;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.Bounds;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.ImageLayoutInfo;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.LayoutInfo;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.PageLayoutInfo;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.Rect;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.TableLayoutInfo;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.TextBoxLayoutInfo;
import org.glasspath.aerialist.text.TextLayout;
import org.glasspath.aerialist.text.TextLayout.Line;
import org.glasspath.aerialist.text.TextLayout.Span;

public abstract class DocumentWriter {

	protected final File file;

	public DocumentWriter(File file) {
		this.file = file;
	}

	public void open(int width, int height) throws Exception {
		openDocument(width, height);
		documentOpened();
	}

	public void documentOpened() {

	}

	protected abstract void openDocument(int width, int height) throws Exception;

	public abstract boolean isDocumentOpen();

	public abstract void openPage(int width, int height) throws Exception;

	public void writePage(PageLayoutInfo pageLayoutInfo) throws Exception {
		for (Element element : pageLayoutInfo.page.getElements()) {
			writeElement(element, pageLayoutInfo.layoutInfo);
		}
	}

	protected void writeElement(Element element, LayoutInfo layoutInfo) throws Exception {

		if (element.getBackground() != null) {
			setFillColor(element.getBackground());
			fill(element.getX(), element.getY(), element.getWidth(), element.getHeight());
		}

		if (element instanceof TextBox) {
			writeTextBoxElement((TextBox) element, layoutInfo);
		} else if (element instanceof Table) {
			writeTableElement((Table) element, layoutInfo);
		} else if (element instanceof Image) {
			writeImageElement((Image) element, layoutInfo);
		} else if (element instanceof QrCode) {
			writeQrCodeElement((QrCode) element, layoutInfo);
		} else if (element instanceof Group) {
			writeGroupElement((Group) element, layoutInfo);
		}

	}

	protected void writeTextBoxElement(TextBox textBox, LayoutInfo layoutInfo) throws Exception {

		paintBorders(textBox.getBorders(), textBox.getX(), textBox.getY(), textBox.getWidth(), textBox.getHeight());

		TextBoxLayoutInfo textBoxLayoutInfo = layoutInfo.textBoxes.get(textBox);
		if (textBoxLayoutInfo != null && textBoxLayoutInfo.textLayout != null) {

			Padding padding = new Padding(textBox.getPadding());

			float w = textBox.getWidth() - (padding.left + padding.right);
			boolean clip = textBoxLayoutInfo.textLayout.preferredWidth > w;

			if (clip) {
				saveState();
				clip(textBox.getX() + padding.left, textBox.getY() + padding.top, w, textBox.getHeight() - (padding.top + padding.bottom));
			}

			float x = textBox.getX() + padding.left;
			float y = textBox.getY() + padding.top;

			beginText();

			for (Line line : textBoxLayoutInfo.textLayout.lines) {

				for (Span span : line.spans) {

					String text = prepareText(textBox.getText().substring(span.start, span.end));
					if (text != null) {

						if (span.style.foreground != null) {
							setFillColor(span.style.foreground);
						} else {
							setFillColor(0, 0, 0, 255);
						}

						drawString(text, x + span.x, y + line.baseline, span.fontIndex, span.style.fontSize, span.style.bold, span.style.italic);

					}

				}

				// draw(x + line.lineBounds.x, y + line.lineBounds.y, line.lineBounds.width, line.lineBounds.height);

			}

			endText();

			if (clip) {
				restoreState();
			}

		}

	}

	protected void writeTableElement(Table table, LayoutInfo layoutInfo) throws Exception {

		TableLayoutInfo tableLayoutInfo = layoutInfo.tables.get(table);
		if (tableLayoutInfo != null) {

			if (tableLayoutInfo.rowBounds != null && tableLayoutInfo.columnBounds != null) {

				// Row background

				// Repeating row background should be painted first
				List<RowStyle> sortedRowStyles = new ArrayList<>();
				sortedRowStyles.addAll(table.getRowStyles());
				Collections.sort(sortedRowStyles, new Comparator<RowStyle>() {

					@Override
					public int compare(RowStyle rs1, RowStyle rs2) {
						return Integer.compare(rs2.repeat, rs1.repeat);
					}
				});

				for (RowStyle rowStyle : sortedRowStyles) {

					if (rowStyle.background != null) {

						setFillColor(rowStyle.background);

						int row = rowStyle.row - 1;
						if (row >= 0) {

							if (rowStyle.repeat > 0) {

								for (int i = row; i < tableLayoutInfo.rowBounds.length; i += rowStyle.repeat) {
									if (tableLayoutInfo.rowBounds[i] != null) {
										fill(table.getX(), table.getY(), tableLayoutInfo.rowBounds[i]);
									}
								}

							} else if (row < tableLayoutInfo.rowBounds.length && tableLayoutInfo.rowBounds[row] != null) {
								fill(table.getX(), table.getY(), tableLayoutInfo.rowBounds[row]);
							}

						} else if (row == -1 && table.getHeaderRows() > 0) { // Header rows

							for (int i = 0; i < table.getHeaderRows() && i < tableLayoutInfo.rowBounds.length; i += 1) {
								if (tableLayoutInfo.rowBounds[i] != null) {
									fill(table.getX(), table.getY(), tableLayoutInfo.rowBounds[i]);
								}
							}

						}

					}

				}

				if (table.getBorders() != null) {

					paintBorders(table.getBorders(), table.getX(), table.getY(), table.getWidth(), table.getHeight());

					// Table borders
					for (Border border : table.getBorders()) {

						if (border.color != null && border.width > 0.0) {

							setStroke(border.width);
							setStrokeColor(border.color);

							switch (BorderType.get(border.type)) {

							case VERTICAL:
								for (int col = 0; col < tableLayoutInfo.columnBounds.length - 1; col++) {
									Rect rect = new Rect(tableLayoutInfo.columnBounds[col]);
									rect.x += table.getX();
									rect.y += table.getY();
									if (rect != null) {
										drawLine(rect.x + (rect.width - 1), rect.y, rect.x + (rect.width - 1), rect.y + rect.height);
									}
								}
								break;

							case HORIZONTAL:
								if (tableLayoutInfo.rowBounds != null) {
									for (int row = 0; row < tableLayoutInfo.rowBounds.length - 1; row++) {
										Rect rect = new Rect(tableLayoutInfo.rowBounds[row]);
										rect.x += table.getX();
										rect.y += table.getY();
										if (rect != null) {
											drawLine(rect.x, rect.y + (rect.height - 1), rect.x + rect.width, rect.y + (rect.height - 1));
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
					for (ColStyle colStyle : table.getColStyles()) {

						int col = colStyle.col - 1;
						if (col >= 0 && col < tableLayoutInfo.columnBounds.length && tableLayoutInfo.columnBounds[col] != null) {

							Rect rect = new Rect(tableLayoutInfo.columnBounds[col]);
							if (col > 0) {
								rect.x += 1;
								rect.width -= 1;
							}

							paintBorders(colStyle.borders, rect.x, rect.y, rect.width, rect.height);

						}

					}

				}

				Padding padding = new Padding(table.getCellPadding());

				for (int i = 0; i < table.getTableCells().size(); i++) {

					TableCell tableCell = table.getTableCells().get(i);

					int row = tableCell.getRow() - 1;
					int col = tableCell.getCol() - 1;
					if (row >= 0 && row < tableLayoutInfo.rowBounds.length && col >= 0 && col < tableLayoutInfo.columnBounds.length && i >= 0 && i < tableLayoutInfo.textLayouts.length) {

						TextLayout textLayout = tableLayoutInfo.textLayouts[i];

						Bounds colBounds = tableLayoutInfo.columnBounds[col];
						Bounds cellBounds = new Bounds(tableLayoutInfo.rowBounds[row]);
						cellBounds.x = table.getX() + colBounds.x;
						cellBounds.y += table.getY();
						cellBounds.width = colBounds.width;

						float w = cellBounds.width - (padding.left + padding.right);
						boolean clip = textLayout.preferredWidth > w;

						if (clip) {
							saveState();
							clip(cellBounds.x + padding.left, cellBounds.y + padding.top, w, cellBounds.height - (padding.top + padding.bottom));
						}

						float x = cellBounds.x + padding.left;
						float y = cellBounds.y + padding.top;

						beginText();

						for (Line line : textLayout.lines) {

							for (Span span : line.spans) {

								String text = prepareText(tableCell.getText().substring(span.start, span.end));
								if (text != null) {

									if (span.style.foreground != null) {
										setFillColor(span.style.foreground);
									} else {
										setFillColor(0, 0, 0, 255);
									}

									drawString(text, x + span.x, y + line.baseline, span.fontIndex, span.style.fontSize, span.style.bold, span.style.italic);

								}

							}

						}

						endText();

						if (clip) {
							restoreState();
						}

					}

				}

			}

		}

	}

	protected void writeImageElement(Image image, LayoutInfo layoutInfo) throws Exception {

		ImageLayoutInfo imageLayoutInfo = layoutInfo.images.get(image);
		if (imageLayoutInfo != null) {

			if (imageLayoutInfo.imageWidth > 0 && imageLayoutInfo.imageHeight > 0) {

				FitPolicy fitPolicy = FitPolicy.get(image.getFit());
				Alignment alignment = Alignment.get(image.getAlignment());

				float scale = image.getScale();
				if (fitPolicy == FitPolicy.WIDTH) {
					scale = (float) image.getWidth() / (float) imageLayoutInfo.imageWidth;
				} else if (fitPolicy == FitPolicy.HEIGHT) {
					scale = (float) image.getHeight() / (float) imageLayoutInfo.imageHeight;
				}

				if (scale > 0.0) {

					int x = 0;
					int width = (int) (image.getWidth() / scale);

					if (alignment == Alignment.CENTER) {
						x = (width / 2) - (imageLayoutInfo.imageWidth / 2);
					} else if (alignment == Alignment.RIGHT) {
						x = width - imageLayoutInfo.imageWidth;
					}

					saveState();
					clip(image.getX(), image.getY(), image.getWidth(), image.getHeight());
					drawImage(image.getSrc(), image.getX() + (x * scale), image.getY(), imageLayoutInfo.imageWidth * scale, imageLayoutInfo.imageHeight * scale, image.getSrc());
					restoreState();

				}

			}

		}

		paintBorders(image.getBorders(), image.getX(), image.getY(), image.getWidth(), image.getHeight());

	}

	protected void writeQrCodeElement(QrCode qrCode, LayoutInfo layoutInfo) throws Exception {

	}

	protected void writeGroupElement(Group group, LayoutInfo layoutInfo) throws Exception {

	}

	protected void paintBorders(List<Border> borders, float x, float y, float w, float h) throws Exception {

		if (borders != null) {

			for (Border border : borders) {

				if (border.color != null && border.width > 0.0) {

					setStrokeColor(border.color);
					setStroke(border.width);

					Rect rect = new Rect(x, y, w, h);
					rect.x += border.width / 2;
					rect.y += border.width / 2;
					rect.width -= border.width;
					rect.height -= border.width;

					switch (BorderType.get(border.type)) {

					case DEFAULT:
						draw(rect.x, rect.y, rect.width, rect.height);
						break;

					case TOP:
						drawLine(rect.x, rect.y, rect.x + rect.width, rect.y);
						break;

					case RIGHT:
						drawLine(rect.x + rect.width, rect.y, rect.x + rect.width, rect.y + rect.height);
						break;

					case BOTTOM:
						drawLine(rect.x, rect.y + rect.height, rect.x + rect.width, rect.y + rect.height);
						break;

					case LEFT:
						drawLine(rect.x, rect.y, rect.x, rect.y + rect.height);
						break;

					case VERTICAL:
						// Not implemented
						break;

					case HORIZONTAL:
						// Not implemented
						break;

					default:
						break;
					}

				}

			}

		}

	}

	protected abstract void saveState() throws Exception;

	protected abstract void restoreState() throws Exception;

	protected void setFillColor(String color) throws Exception {
		try {
			int i = Integer.decode(color).intValue();
			setFillColor((i >> 16) & 0xFF, (i >> 8) & 0xFF, i & 0xFF, 255); // TODO: Alpha
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected abstract void setFillColor(int r, int g, int b, int a) throws Exception;

	protected void setStrokeColor(String color) throws Exception {
		try {
			int i = Integer.decode(color).intValue();
			setStrokeColor((i >> 16) & 0xFF, (i >> 8) & 0xFF, i & 0xFF, 255); // TODO: Alpha
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected abstract void setStrokeColor(int r, int g, int b, int a) throws Exception;

	protected abstract void setStroke(float w) throws Exception;

	protected void clip(Rect r) throws Exception {
		clip(r.x, r.y, r.width, r.height);
	}

	protected void fill(Rect r) throws Exception {
		fill(r.x, r.y, r.width, r.height);
	}

	protected void fill(float x, float y, Bounds b) throws Exception {
		fill(x + b.x, y + b.y, b.width, b.height);
	}

	protected abstract void clip(float x, float y, float w, float h) throws Exception;

	protected abstract void fill(float x, float y, float w, float h) throws Exception;

	protected abstract void draw(float x, float y, float w, float h) throws Exception;

	protected abstract void drawLine(float x1, float y1, float x2, float y2) throws Exception;

	protected abstract void drawImage(String key, float x, float y, float w, float h, String name) throws Exception;

	protected String prepareText(String text) {

		if (text != null) {

			// TODO: Should we maybe remove invalid characters from the text earlier? when creating the text-layout for example
			text = text.replace("\n", ""); //$NON-NLS-1$ //$NON-NLS-2$
			text = text.replace("\r", ""); //$NON-NLS-1$ //$NON-NLS-2$

			// Don't draw string which contain only whitespace characters
			// PDF's will be rendered with invalid characters by Preview on MacOS
			if (text.trim().length() > 0) {
				return text;
			}

		}

		return null;

	}

	protected abstract void beginText() throws Exception;

	protected abstract void drawString(String s, float x, float y, int fontIndex, float fontSize, boolean bold, boolean italic) throws Exception;

	protected abstract void endText() throws Exception;

	public abstract void closePage() throws Exception;

	public void close() throws Exception {
		closeDocument();
		documentClosed();
	}

	public void documentClosed() {

	}

	protected abstract void closeDocument() throws Exception;

}
