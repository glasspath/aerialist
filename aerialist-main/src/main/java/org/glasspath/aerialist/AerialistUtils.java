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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.glasspath.aerialist.Page.PageSize;
import org.glasspath.aerialist.editor.EditorPanel;
import org.glasspath.aerialist.swing.view.EmailContainer;
import org.glasspath.aerialist.swing.view.FooterPageView;
import org.glasspath.aerialist.swing.view.GroupView;
import org.glasspath.aerialist.swing.view.ISwingElementView;
import org.glasspath.aerialist.swing.view.LayeredPageView;
import org.glasspath.aerialist.swing.view.PageContainer;
import org.glasspath.aerialist.swing.view.PageView;
import org.glasspath.common.GlasspathSystemProperties;
import org.glasspath.common.os.OsUtils;
import org.glasspath.common.swing.color.ColorUtils;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.DefaultFontMapper.BaseFontParameters;
import com.lowagie.text.pdf.FontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGraphics2D;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

@SuppressWarnings("nls")
public class AerialistUtils {

	private static DefaultFontMapper fontMapper = null;

	private AerialistUtils() {

	}

	public static ISwingElementView<?> getElementView(Component component) {

		Component c = component;
		while (c != null) {

			if (c instanceof ISwingElementView<?> && c.getParent() instanceof PageView) {
				return (ISwingElementView<?>) c;
			}

			c = c.getParent();

		}

		return null;

	}

	public static Component getElementViewAsComponent(Component component) {

		Component c = component;
		while (c != null) {

			if (c instanceof ISwingElementView<?> && c.getParent() instanceof PageView) {
				return c;
			}

			c = c.getParent();

		}

		return null;

	}

	public static ISwingElementView<?> getEmailElementView(Component component) {

		Component c = component;
		while (c != null) {

			if (c instanceof ISwingElementView<?> && c.getParent() instanceof EmailContainer) {
				return (ISwingElementView<?>) c;
			}

			c = c.getParent();

		}

		return null;

	}

	public static PageView getPageView(ISwingElementView<?> elementView) {
		if (((Component) elementView).getParent() instanceof PageView) {
			return (PageView) ((Component) elementView).getParent();
		} else {
			return null;
		}
	}

	public static PageView getPageView(Component component) {

		Component c = component;
		while (c != null) {

			if (c instanceof PageView) {
				return (PageView) c;
			}

			c = c.getParent();

		}

		return null;

	}

	public static GroupView getGroupView(Component component) {

		Component c = component;
		while (c != null) {

			if (c instanceof GroupView) {
				return (GroupView) c;
			}

			c = c.getParent();

		}

		return null;

	}

	public static HeightPolicy getHeightPolicy(Component component) {
		if (component instanceof ISwingElementView<?>) {
			return ((ISwingElementView<?>) component).getHeightPolicy();
		} else {
			return null;
		}
	}

	public static void writeToPDF(PageContainer pageContainer, File file) throws IOException, DocumentException {

		if (pageContainer.getPageViews().size() > 0) {

			PageView firstPageView = pageContainer.getPageViews().get(0);

			com.lowagie.text.Document document = new com.lowagie.text.Document(new Rectangle(firstPageView.getWidth(), firstPageView.getHeight()));

			try {

				FontMapper fontMapper = getFontMapper();

				PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(file));

				document.open();

				PdfContentByte contentByte = pdfWriter.getDirectContent();

				boolean firstPage = true;
				for (PageView pageView : pageContainer.getPageViews()) {

					if (firstPage) {
						firstPage = false;
					} else {
						document.setPageSize(new Rectangle(pageView.getWidth(), pageView.getHeight()));
						document.newPage();
					}

					PdfTemplate template = contentByte.createTemplate(pageView.getWidth(), pageView.getHeight());

					PdfGraphics2D pdfG2d = new PdfGraphics2D(contentByte, pageView.getWidth(), pageView.getHeight(), fontMapper, false, false, 0.0F);

					if (pageView instanceof LayeredPageView) {
						for (PageView layerView : ((LayeredPageView) pageView).getLayers()) {

							Dimension size = layerView.getPreferredSize();
							layerView.setBounds(0, 0, size.width, size.height);

							int x = (pageView.getWidth() - size.width) / 2;
							int y = 0;

							// Align footer to bottom
							if (layerView instanceof FooterPageView) {
								y += pageView.getHeight() - size.height;
							}

							pdfG2d.translate(x, y);
							layerView.print(pdfG2d);
							pdfG2d.translate(-x, -y);

						}
					}

					pageView.print(pdfG2d);

					pdfG2d.dispose();

					contentByte.addTemplate(template, 0, 0);

				}

				document.close();
				pdfWriter.close();

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (document.isOpen()) {
					document.close();
				}
			}

		}

	}

	public static DefaultFontMapper getFontMapper() {

		if (fontMapper == null) {
			fontMapper = createFontMapper();
		}

		return fontMapper;

	}

	private static DefaultFontMapper createFontMapper() {

		DefaultFontMapper mapper = new DefaultFontMapper() {

			@Override
			public BaseFont awtToPdf(Font font) {
				BaseFont baseFont = super.awtToPdf(font);
				// System.out.println(font.getFontName() + " -> " + baseFont.getFullFontName());
				return baseFont;
			}
		};

		String platformFontPath = getPlatformFontPath();
		if (platformFontPath != null && platformFontPath.length() > 0) {

			mapper.insertDirectory(platformFontPath);

			File fontsDir = new File(platformFontPath);
			if (fontsDir.exists()) {
				for (File file : fontsDir.listFiles()) {
					if (file.exists() && file.isDirectory()) {
						mapper.insertDirectory(file.getAbsolutePath());
					}
				}
			}

		}

		HashMap<String, BaseFontParameters> mapping = mapper.getMapper();

		/*
		* Change encoding to the IDENTITY_H for the found fonts from the given platform font path since
		* the default implementation of the insertDirectory method use CP1252 encoding. But we
		* need to display some unicode symbols. Don't know if there is a better way to do this.
		*/
		for (BaseFontParameters parameters : mapping.values()) {
			parameters.encoding = BaseFont.IDENTITY_H;
		}

		return mapper;

	}

	private static String getPlatformFontPath() {

		// TODO: Use reflection?
		// return sun.font.SunFontManager.getInstance().getPlatformFontPath(true);

		// Try default known locations

		File bundledFontsDir = null;
		String bundledFontsPath = System.getProperty(GlasspathSystemProperties.BUNDLED_FONTS_PATH);
		if (bundledFontsPath != null && bundledFontsPath.length() > 0) {
			bundledFontsDir = new File(bundledFontsPath);
		}

		if (bundledFontsDir != null && bundledFontsDir.isDirectory()) {
			return bundledFontsDir.getAbsolutePath();
		} else if (new File("fonts").exists()) {
			return "fonts";
		} else if (new File("C:\\Windows\\Fonts").exists()) {
			return "C:\\Windows\\Fonts";
		} else if (new File("/System/Library/Fonts/Supplemental").exists()) {
			return "/System/Library/Fonts/Supplemental";
		} else if (new File("/usr/share/fonts/truetype/ubuntu").exists()) {
			return "/usr/share/fonts/truetype/ubuntu";
		} else if (new File("/usr/share/fonts").exists()) {
			return "/usr/share/fonts";
		} else if (new File("/usr/lib/share/fonts").exists()) {
			return "/usr/lib/share/fonts";
		}

		if (OsUtils.PLATFORM_WINDOWS) {
			return "C:\\Windows\\Fonts"; // TODO!
		} else {
			return null;
		}

	}

	public static Document createEmptyDocument() {
		return new Document();
	}

	public static Document createDefaultDocument() {

		Document document = new Document();
		document.getPages().add(createDefaultPage());

		return document;

	}

	public static Page createDefaultPage() {
		return new Page(PageSize.A4.getWidth(), PageSize.A4.getHeight());
	}

	public static Page createDefaultPage(int width, int height) {
		return new Page(width, height);
	}

	public static Email createEmptyEmail() {

		Email email = new Email();
		email.setTable(createDefaultEmailTable());

		return email;

	}

	public static Email createDefaultEmail() {

		Email email = new Email();
		email.setSubjectTextBox(createDefaultEmailSubjectTextBox());
		email.setTable(createDefaultEmailTable());

		return email;

	}

	public static TextBox createDefaultEmailSubjectTextBox() {

		TextBox textBox = new TextBox();

		TextStyle textStyle = new TextStyle();
		textStyle.start = 0;
		textStyle.end = 0;
		textStyle.fontSize = 12;
		textStyle.bold = true;
		textBox.getStyles().add(textStyle);

		return textBox;

	}

	public static Table createDefaultEmailTable() {

		Table table = new Table();

		TableCell tableCell = new TableCell();
		tableCell.setRow(1);
		tableCell.setCol(1);
		table.getTableCells().add(tableCell);

		return table;

	}

	public static TextBox createDefaultTextBox() {

		TextBox textBox = new TextBox();
		textBox.setWidth(150);
		textBox.setHeight(50);
		textBox.setPadding(Padding.from(3));
		textBox.setText("Text");

		return textBox;

	}

	public static Border createBorder(BorderType type, float width, Color color) {

		Border border = new Border();
		border.type = type.stringValue;
		border.width = width;
		border.color = ColorUtils.toHex(color);

		return border;

	}

	public static Table createDefaultTable() {

		Table table = new Table();
		table.setWidth(470);
		table.setHeight(100);
		table.setCellPadding(Padding.from(3));
		table.getBorders().add(createBorder(BorderType.DEFAULT, 1, new Color(200, 200, 200)));
		table.getBorders().add(createBorder(BorderType.VERTICAL, 1, new Color(200, 200, 200)));
		table.getBorders().add(createBorder(BorderType.HORIZONTAL, 1, new Color(200, 200, 200)));
		table.setHeightPolicy(HeightPolicy.AUTO.stringValue);

		TableCell tableCell;
		for (int row = 1; row <= 4; row++) {
			for (int col = 1; col <= 4; col++) {

				tableCell = new TableCell();
				tableCell.setRow(row);
				tableCell.setCol(col);
				table.getTableCells().add(tableCell);

			}
		}

		return table;

	}

	public static Image createDefaultImage() {

		Image image = new Image();
		image.setWidth(150);
		image.setHeight(150);

		return image;

	}

	public static QrCode createDefaultQrCode() {

		QrCode qrCode = new QrCode();
		qrCode.setWidth(150);
		qrCode.setHeight(150);
		qrCode.setFit(FitPolicy.WIDTH.stringValue);
		qrCode.setText("QR-Code");

		return qrCode;

	}

	public static TextStyle createTextStyle(int start) {
		return createTextStyle(start, start + 1);
	}

	public static TextStyle createTextStyle(int start, float spaceAbove) {
		return createTextStyle(start, start + 1, null, spaceAbove);
	}

	public static TextStyle createTextStyle(int start, int end) {
		return createTextStyle(start, end, null);
	}

	public static TextStyle createTextStyle(int start, int end, String source) {
		return createTextStyle(start, end, source, 0.0F);
	}

	public static TextStyle createTextStyle(int start, int end, String source, float spaceAbove) {

		TextStyle textStyle = new TextStyle();
		textStyle.start = start;
		textStyle.end = end;
		textStyle.spaceAbove = spaceAbove;
		if (source != null) {
			textStyle.source = source;
		}

		return textStyle;

	}

	public static TableCell createTableCell(String text, int row, int col, String alignment, int fontSize, boolean bold, String source) {

		TableCell tableCell = new TableCell();
		tableCell.setText(text);
		tableCell.setRow(row);
		tableCell.setCol(col);
		tableCell.setAlignment(alignment);

		TextStyle textStyle = new TextStyle();
		textStyle.start = 0;
		textStyle.end = text.length();
		textStyle.fontSize = fontSize;
		textStyle.bold = bold;
		textStyle.source = source;
		tableCell.getStyles().add(textStyle);

		return tableCell;

	}

	public static ColStyle createColStyle(int col, int width) {
		return createColStyle(col, width, null);
	}

	public static ColStyle createColStyle(int col, int width, Border border) {

		ColStyle colStyle = new ColStyle();
		colStyle.col = col;
		colStyle.width = width;
		if (border != null) {
			colStyle.borders.add(border);
		}

		return colStyle;

	}

	public static RowStyle createRowStyle(int row, int repeat, String background) {

		RowStyle rowStyle = new RowStyle();
		rowStyle.row = row;
		rowStyle.repeat = repeat;
		rowStyle.background = background;

		return rowStyle;

	}

	public static DynamicFieldContext getDynamicFieldContext(EditorPanel<?> editorPanel) {

		DynamicFieldContext fieldContext = null;

		if (editorPanel.getEditorContext() != null) {
			fieldContext = editorPanel.getEditorContext().getDynamicFieldContext();
		}

		if (fieldContext == null) {
			fieldContext = new DynamicFieldContext();
		}

		return fieldContext;

	}

}
