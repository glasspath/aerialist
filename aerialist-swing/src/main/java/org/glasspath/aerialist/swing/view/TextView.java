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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AbstractDocument.LeafElement;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.DocumentFilter;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.ParagraphView;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.Utilities;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.glasspath.aerialist.Alignment;
import org.glasspath.aerialist.IText;
import org.glasspath.aerialist.TextStyle;
import org.glasspath.aerialist.layout.ILayoutContext.ExportPhase;

@SuppressWarnings("nls")
public class TextView extends JTextPane {

	public static final String SOURCE_ATTRIBUTE = "source";
	public static final Color SOURCE_RECT_COLOR = new Color(200, 200, 200);

	protected final ISwingViewContext viewContext;
	private boolean singleLine = false;
	private boolean updatingComponent = false;
	private TextData currentTextData = null;

	public TextView(ISwingViewContext viewContext) {

		this.viewContext = viewContext;

		setForeground(viewContext.getDefaultForeground());
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder());
		setFont(new Font(TextStyle.DEFAULT_FONT, Font.PLAIN, TextStyle.DEFAULT_FONT_SIZE));

		// See https://bugs.openjdk.org/browse/JDK-8298017
		setAutoscrolls(false);

		// getDocument().putProperty("i18n", Boolean.TRUE); // Hack to force GlyphPainter2 to be used, looks good in editor but not in PDF (text is painted as glyph vectors)
		setEditorKit(new FractionalStyledEditorKit());

		StyledDocument document = getStyledDocument();

		document.addUndoableEditListener(new UndoableEditListener() {

			@Override
			public void undoableEditHappened(UndoableEditEvent e) {
				if (!updatingComponent) {
					createUndoableEdit();
				}
			}
		});

		if (document instanceof AbstractDocument) {

			((AbstractDocument) document).setDocumentFilter(new DocumentFilter() {

				@Override
				public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
					if (singleLine) {
						super.insertString(fb, offset, string.replaceAll("\n", ""), attr);
					} else {
						super.insertString(fb, offset, string, attr);
					}
				}

				@Override
				public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attr) throws BadLocationException {

					if (!updatingComponent) {

						if (attr instanceof SimpleAttributeSet) {

							SimpleAttributeSet attributeSet = (SimpleAttributeSet) attr;

							// TODO: This doesn't work if the field extends to the end of the line (element seems to include the line feed)
							// Typing at the start or end of a field should not extend the field
							Element element = document.getCharacterElement(offset);
							if (element != null && (element.getStartOffset() == offset || element.getEndOffset() == offset)) {
								attributeSet.addAttribute(SOURCE_ATTRIBUTE, "");
							}

						}

					}

					if (singleLine) {
						super.replace(fb, offset, length, text.replaceAll("\n", ""), attr);
					} else {
						super.replace(fb, offset, length, text, attr);
					}

				}
			});

		}

		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK), "insert-break");

		ISwingViewContext.installSelectionHandler(this, viewContext);

	}

	public boolean isSingleLine() {
		return singleLine;
	}

	public void setSingleLine(boolean singleLine) {
		this.singleLine = singleLine;
	}

	@Override
	public FontMetrics getFontMetrics(Font font) {

		FontMetrics fontMetrics = null;

		Graphics g = getGraphics();

		if (g instanceof Graphics2D) {

			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

			fontMetrics = g2d.getFontMetrics(font);

		} else {

			// fontMetrics = super.getFontMetrics(font);
			// fontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
			// fontMetrics = FontDesignMetrics.getMetrics(font);

			// TODO: This is a bit of a hack to get a FontRenderingContext with fractional metrics set to ON
			Graphics2D g2d = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

			fontMetrics = g2d.getFontMetrics(font);

		}

		if (g != null) {
			g.dispose();
		}

		return fontMetrics;

	}

	public void init(IText iText) {

		updatingComponent = true;

		TextData textData = new TextData();
		if (iText != null && iText.getText() != null) {
			textData.setText(iText.getText());
			textData.setStyles(iText.getStyles());
			textData.setAlignment(iText.getAlignment());
		}

		initTextAndStyles(textData);

		currentTextData = new TextData();
		toText(currentTextData);

		updatingComponent = false;

	}

	private void createUndoableEdit() {

		TextData newTextData = new TextData();
		toText(newTextData);

		viewContext.undoableEditHappened(new TextViewUndoableEdit(viewContext, TextView.this, currentTextData, newTextData));

		currentTextData = new TextData();
		toText(currentTextData);

	}

	private void initTextAndStyles(TextData textData) {

		StyledDocument document = getStyledDocument();
		int length = document.getLength();

		try {
			document.remove(0, length);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setFontSize(defaultStyle, TextStyle.DEFAULT_FONT_SIZE);
		document.setParagraphAttributes(0, length, defaultStyle, false);

		try {
			document.insertString(0, textData.getText(), defaultStyle);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		addStyles(textData.getStyles());

		setTextAlignment(Alignment.get(textData.getAlignment()).intValue);

	}

	public void addStyles(List<TextStyle> styles) {
		for (TextStyle style : styles) {
			addStyle(style);
		}
	}

	public void addStyle(TextStyle style) {

		try {

			StyledDocument document = getStyledDocument();

			int length = style.end - style.start;
			if (length >= 0) {

				String text = document.getText(style.start, length);

				Element element = document.getCharacterElement(style.start);
				AttributeSet elementAttributeSet = element.getAttributes();

				SimpleAttributeSet attributeSet = new SimpleAttributeSet(elementAttributeSet);

				StyleConstants.setFontFamily(attributeSet, style.font != null ? style.font : TextStyle.DEFAULT_FONT);
				StyleConstants.setFontSize(attributeSet, style.fontSize);
				StyleConstants.setSpaceAbove(attributeSet, style.spaceAbove);
				StyleConstants.setBold(attributeSet, style.bold);
				StyleConstants.setItalic(attributeSet, style.italic);
				StyleConstants.setUnderline(attributeSet, style.underline);
				StyleConstants.setStrikeThrough(attributeSet, style.strikeThrough);

				Color color = ColorUtils.fromHex(style.foreground);
				if (color != null) {
					StyleConstants.setForeground(attributeSet, color);
				} else {
					StyleConstants.setForeground(attributeSet, viewContext.getDefaultForeground());
				}

				color = ColorUtils.fromHex(style.background);
				if (color != null) {
					StyleConstants.setBackground(attributeSet, color);
				} else {
					attributeSet.removeAttribute(StyleConstants.Background);
				}

				if (style.image != null) {
					BufferedImage image = viewContext.getMediaCache().getImage(style.image);
					if (image != null) {
						StyleConstants.setIcon(attributeSet, new ImageIconView(image, style.image));
					}
				}

				if (style.source != null) {
					attributeSet.addAttribute(SOURCE_ATTRIBUTE, style.source);
				} else {
					attributeSet.addAttribute(SOURCE_ATTRIBUTE, "");
				}

				document.remove(style.start, length);
				document.insertString(style.start, text, attributeSet);

				// TODO: SpaceAbove only seems to be applied by setting paragraph attributes, why?
				// if (style.spaceAbove > 0.0F) {
				if (style.image == null) {
					document.setParagraphAttributes(style.start, length, attributeSet, false);
				}

			}

		} catch (BadLocationException e) {
			e.printStackTrace();
		}

	}

	public int getTextAlignment() {
		AttributeSet style = getStyledDocument().getParagraphElement(0).getAttributes();
		return StyleConstants.getAlignment(style);
	}

	public void setTextAlignment(int textAlignment) {

		SimpleAttributeSet style = new SimpleAttributeSet();
		StyleConstants.setAlignment(style, textAlignment);

		StyledDocument document = getStyledDocument();

		// TODO: document.getLength() doesn't seem to include \n's so if the text ends with a empty line the alignment isn't applied to the last line..
		// document.setParagraphAttributes(0, document.getLength(), style, false);
		// System.out.println("getText().length() = " + getText().length() + ", document.getLength() = " + document.getLength());
		document.setParagraphAttributes(0, getText().length(), style, false);

	}

	// TODO: When setting font size using the default action the caret before the first char of the first line still has the old size
	public void setFontSize(int fontSize) {
		MutableAttributeSet attributeSet = getAttributes();
		StyleConstants.setFontSize(attributeSet, fontSize);
		applyAttributeSet(attributeSet, null, false);
	}

	public void setSpaceAbove(float spaceAbove) {
		MutableAttributeSet attributeSet = getAttributes();
		StyleConstants.setSpaceAbove(attributeSet, spaceAbove);
		applyAttributeSet(attributeSet, null, true);
	}

	public void insertField(String key, String text) {

		MutableAttributeSet attributeSet = getAttributes();
		attributeSet.addAttribute(SOURCE_ATTRIBUTE, key);

		if (getSelectionEnd() == getSelectionStart()) {
			applyAttributeSet(attributeSet, text, false);
		} else {
			applyAttributeSet(attributeSet, null, false);
		}

	}

	private MutableAttributeSet getAttributes() {

		MutableAttributeSet attributeSet = new SimpleAttributeSet();

		// TODO: Check everything between start and end, and determine which elements to change
		Element element = getStyledDocument().getCharacterElement(getSelectionStart());
		if (element != null) {

			AttributeSet attributes = element.getAttributes();
			if (attributes instanceof LeafElement) {

				// TODO: Copy all styles

				if (StyleConstants.isBold(attributes)) {
					StyleConstants.setBold(attributeSet, true);
				}

				String source = (String) attributes.getAttribute(SOURCE_ATTRIBUTE);
				if (source != null && source.length() > 0) {
					attributeSet.addAttribute(SOURCE_ATTRIBUTE, source);
				}

			}

		}

		return attributeSet;

	}

	private void applyAttributeSet(MutableAttributeSet attributeSet, String replaceText, boolean applyToParagraph) {

		updatingComponent = true;

		try {

			int selectionStart = getSelectionStart();
			int selectionEnd = getSelectionEnd();

			int start = selectionStart;
			int end = selectionEnd;

			StyledDocument document = getStyledDocument();

			if (applyToParagraph) {

				Element paragraph = Utilities.getParagraphElement(this, start);
				if (paragraph != null) {
					start = paragraph.getStartOffset();
				}

				paragraph = Utilities.getParagraphElement(this, end);
				if (paragraph != null) {
					end = paragraph.getEndOffset();
					if (end > document.getLength()) {
						end = document.getLength();
					}
				}

			}

			if (end >= start) {

				int length = end - start;

				String text = document.getText(start, length);

				document.remove(start, length);

				if (replaceText != null) {
					document.insertString(start, replaceText, attributeSet);
				} else {
					document.insertString(start, text, attributeSet);
				}

				if (applyToParagraph) {
					document.setParagraphAttributes(start, length, attributeSet, false);
				} else {
					document.setCharacterAttributes(start, length, attributeSet, false);
				}

				createUndoableEdit();
				initTextAndStyles(currentTextData); // TODO? This was added because sometimes the JTextPane displays text wrong after a few operations, while applying all the same styles at init looks good
				select(selectionStart, selectionEnd);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		updatingComponent = false;

	}

	public void toggleLinePrefix(String prefix) {

		updatingComponent = true;

		try {

			int selectionStart = getSelectionStart();
			int selectionEnd = getSelectionEnd();

			StyledDocument document = getStyledDocument();

			Element rootElement = document.getDefaultRootElement();
			int firstIndex = rootElement.getElementIndex(selectionStart);
			int lastIndex = rootElement.getElementIndex(selectionEnd);

			for (int i = lastIndex; i >= firstIndex; i--) {

				Element paragraph = rootElement.getElement(i);

				String text = document.getText(paragraph.getStartOffset(), paragraph.getEndOffset() - paragraph.getStartOffset());
				if (text.startsWith(prefix)) {
					document.remove(paragraph.getStartOffset(), prefix.length());
				} else {
					document.insertString(paragraph.getStartOffset(), prefix, paragraph.getAttributes());
				}

			}

			createUndoableEdit();

			// TODO: Calculate new selection start/end
			// select(selectionStart, selectionEnd);

		} catch (Exception e) {
			e.printStackTrace();
		}

		updatingComponent = false;

	}

	public void insertImage(String src, BufferedImage image) {

		updatingComponent = true;

		try {

			ImageIconView imageIconView = new ImageIconView(image, src);

			int start = getSelectionStart();
			int end = getSelectionEnd();
			if (end >= start) {

				int length = end - start;

				StyledDocument document = getStyledDocument();

				MutableAttributeSet attributeSet = new SimpleAttributeSet();
				// StyleConstants.setComponent(attributeSet, new ImageComponentView("test"));
				StyleConstants.setIcon(attributeSet, imageIconView);

				document.remove(start, length);
				document.insertString(start, "#", attributeSet);

				// document.setParagraphAttributes(start, length, attributeSet, false);

				createUndoableEdit();

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		updatingComponent = false;

	}

	public void toText(IText iText) {

		try {

			StyledDocument document = getStyledDocument();
			int length = document.getLength();

			iText.setText(document.getText(0, length));
			iText.setAlignment(Alignment.get(getTextAlignment()));

			iText.setStyles(createStyles());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private List<TextStyle> createStyles() {

		List<TextStyle> styles = new ArrayList<>();

		try {

			StyledDocument document = getStyledDocument();
			int length = document.getLength();

			Element element;
			AttributeSet style;

			if (length == 0) {

				EditorKit editorKit = getEditorKit();
				if (editorKit instanceof StyledEditorKit) {

					StyledEditorKit styledEditorKit = (StyledEditorKit) editorKit;
					MutableAttributeSet attributeSet = styledEditorKit.getInputAttributes();

					TextStyle textStyle = createTextStyle(attributeSet);
					textStyle.start = 0;
					textStyle.end = 0;

					styles.add(textStyle);

				}

			} else {

				int i = 0;
				while (i < length) {

					element = document.getCharacterElement(i);
					style = element.getAttributes();

					if (style instanceof LeafElement) {

						LeafElement leafElement = (LeafElement) style;

						TextStyle textStyle = createTextStyle(leafElement);
						textStyle.start = leafElement.getStartOffset();
						textStyle.end = leafElement.getEndOffset();

						// TODO: When text is added to a empty document we get a style where end is too high, why?
						if (textStyle.end > length) {
							textStyle.end = length;
						}

						styles.add(textStyle);

						if (leafElement.getEndOffset() > i) {
							i = leafElement.getEndOffset();
						} else {
							i++;
						}

					} else {
						// System.out.println(i + ": " + style + ", " + style.getClass());
					}

				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return styles;

	}

	private TextStyle createTextStyle(AttributeSet attributeSet) {

		TextStyle textStyle = new TextStyle();

		textStyle.spaceAbove = StyleConstants.getSpaceAbove(attributeSet);

		String font = StyleConstants.getFontFamily(attributeSet);
		if (!TextStyle.DEFAULT_FONT.equals(font)) {
			textStyle.font = font;
		}

		textStyle.fontSize = StyleConstants.getFontSize(attributeSet);
		textStyle.bold = StyleConstants.isBold(attributeSet);
		textStyle.italic = StyleConstants.isItalic(attributeSet);
		textStyle.underline = StyleConstants.isUnderline(attributeSet);
		textStyle.strikeThrough = StyleConstants.isStrikeThrough(attributeSet);

		Object object = attributeSet.getAttribute(StyleConstants.Foreground);
		if (!viewContext.getDefaultForeground().equals(object)) {
			textStyle.foreground = ColorUtils.toHex(object);
		}

		textStyle.background = ColorUtils.toHex(attributeSet.getAttribute(StyleConstants.Background));

		/*
		Component component = StyleConstants.getComponent(attributeSet);
		if (component instanceof ImageComponentView) {
			textStyle.image = ((ImageComponentView) component).imagePath;
		} else {
			textStyle.image = null;
		}
		*/
		Icon icon = StyleConstants.getIcon(attributeSet);
		if (icon instanceof ImageIconView) {
			textStyle.image = ((ImageIconView) icon).src;
		}

		String source = (String) attributeSet.getAttribute(SOURCE_ATTRIBUTE);
		if (source != null && source.length() > 0) {
			textStyle.source = source;
		}

		return textStyle;

	}

	public class FractionalStyledEditorKit extends StyledEditorKit {

		public FractionalStyledEditorKit() {

		}

		@Override
		public ViewFactory getViewFactory() {
			return new StyledViewFactory();
		}

		public class StyledViewFactory implements ViewFactory {

			public View create(Element elem) {

				String kind = elem.getName();
				if (kind != null) {
					if (kind.equals(AbstractDocument.ContentElementName)) {
						return new DecoratedLabelView(elem);
					} else if (kind.equals(AbstractDocument.ParagraphElementName)) {
						return new ParagraphView(elem);
					} else if (kind.equals(AbstractDocument.SectionElementName)) {
						return new FractionalBoxView(elem, View.Y_AXIS);
					} else if (kind.equals(StyleConstants.ComponentElementName)) {
						return new ComponentView(elem);
					} else if (kind.equals(StyleConstants.IconElementName)) {
						return new IconView(elem);
					}
				}

				return new LabelView(elem);

			}

		}

	}

	public class DecoratedLabelView extends DefaultLabelView {

		public DecoratedLabelView(Element elem) {
			super(elem);
		}

		@Override
		public int getTextAlignment() {
			return TextView.this.getTextAlignment();
		}

		@Override
		public void paint(Graphics g, Shape a) {

			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

			if (a != null) {

				if (viewContext.getExportPhase() == ExportPhase.IDLE) {

					AttributeSet attributeSet = getAttributes();
					if (attributeSet != null) {

						String source = (String) attributeSet.getAttribute(SOURCE_ATTRIBUTE);
						if (source != null && source.length() > 0) {

							g2d.setColor(SOURCE_RECT_COLOR);

							Rectangle rect = new Rectangle(a.getBounds());
							rect.y += 1;
							rect.height -= 3;
							if (rect.height < 3) {
								rect.height = 3;
							}
							g2d.draw(rect);

						}

					}

				}

			}

			super.paint(g, a);

		}

	}

	public class FractionalBoxView extends BoxView {

		public FractionalBoxView(Element elem, int axis) {
			super(elem, axis);
		}

		@Override
		public void paint(Graphics g, Shape allocation) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			super.paint(g2d, allocation);
		}

	}

	public static class ImageIconView extends ImageIcon {

		private final String src;

		public ImageIconView(Image image, String src) {
			super(image);
			this.src = src;
		}

	}

	/*
	public class ImageComponentView extends JComponent {
	
		private final String imagePath;
		private final Dimension size = new Dimension(100, 100);
	
		public ImageComponentView(String imagePath) {
			this.imagePath = imagePath;
		}
	
		@Override
		public Dimension getMinimumSize() {
			return size;
		}
	
		@Override
		public Dimension getPreferredSize() {
			return size;
		}
	
		@Override
		public Dimension getMaximumSize() {
			return size;
		}
	
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			g.setColor(Color.green);
			g.fillRect(0, 0, 100, 100);
		}
	
	}
	 */

	protected static class TextData implements IText {

		private String text = "";
		private String alignment = Alignment.LEFT.stringValue;
		private List<TextStyle> styles = new ArrayList<>();

		protected TextData() {

		}

		@Override
		public String getText() {
			return text;
		}

		@Override
		public void setText(String text) {
			this.text = text;
		}

		@Override
		public String getAlignment() {
			return alignment;
		}

		@Override
		public void setAlignment(String alignment) {
			this.alignment = alignment;
		}

		@Override
		public List<TextStyle> getStyles() {
			return styles;
		}

		@Override
		public void setStyles(List<TextStyle> styles) {
			this.styles = styles;
		}

	}

	public static class TextViewUndoableEdit implements UndoableEdit {

		private final ISwingViewContext viewContext;
		private final TextView textView;
		private final TextData oldTextData;
		private final TextData newTextData;
		private final boolean yPolicyEnabled;

		public TextViewUndoableEdit(ISwingViewContext viewContext, TextView textView, TextData oldTextData, TextData newTextData) {
			this.viewContext = viewContext;
			this.textView = textView;
			this.oldTextData = oldTextData;
			this.newTextData = newTextData;
			this.yPolicyEnabled = viewContext.isYPolicyEnabled();
		}

		@Override
		public String getPresentationName() {
			return "Edit text";
		}

		@Override
		public String getUndoPresentationName() {
			return "Undo edit text";
		}

		@Override
		public String getRedoPresentationName() {
			return "Redo edit text";
		}

		@Override
		public boolean addEdit(UndoableEdit anEdit) {
			return false;
		}

		@Override
		public boolean canRedo() {
			return true;
		}

		@Override
		public boolean canUndo() {
			return true;
		}

		@Override
		public void die() {

		}

		@Override
		public boolean isSignificant() {
			return true;
		}

		@Override
		public void redo() throws CannotRedoException {
			viewContext.setYPolicyEnabled(yPolicyEnabled);
			textView.init(newTextData);
			viewContext.refresh(null); // TODO
		}

		@Override
		public boolean replaceEdit(UndoableEdit anEdit) {
			return false;
		}

		@Override
		public void undo() throws CannotUndoException {
			viewContext.setYPolicyEnabled(yPolicyEnabled);
			textView.init(oldTextData);
			viewContext.refresh(null); // TODO
		}

	}

}
