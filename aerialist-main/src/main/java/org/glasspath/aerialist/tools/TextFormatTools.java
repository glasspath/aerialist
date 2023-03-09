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
package org.glasspath.aerialist.tools;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.glasspath.aerialist.TextStyle;
import org.glasspath.aerialist.editor.EditorPanel;
import org.glasspath.aerialist.editor.actions.BoldAction;
import org.glasspath.aerialist.editor.actions.FontFamilyAction;
import org.glasspath.aerialist.editor.actions.FontSizeAction;
import org.glasspath.aerialist.editor.actions.ItalicAction;
import org.glasspath.aerialist.editor.actions.ListBulletedAction;
import org.glasspath.aerialist.editor.actions.SetTextAlignmentAction;
import org.glasspath.aerialist.editor.actions.SpaceAboveAction;
import org.glasspath.aerialist.editor.actions.StrikeThroughAction;
import org.glasspath.aerialist.editor.actions.TextColorAction;
import org.glasspath.aerialist.editor.actions.UnderlineAction;
import org.glasspath.aerialist.icons.Icons;
import org.glasspath.aerialist.swing.view.TextView;
import org.glasspath.common.swing.FrameContext;
import org.glasspath.common.swing.button.SplitButton;
import org.glasspath.common.swing.color.ColorButton;
import org.glasspath.common.swing.tools.AbstractTools;

public class TextFormatTools extends AbstractTools<FrameContext> {

	private final EditorPanel<? extends EditorPanel<?>> editor;
	private final JComboBox<String> fontComboBox;
	private final JComboBox<String> fontSizeComboBox;

	private boolean defaultFontAvailable = false;
	private boolean updating = false;

	public TextFormatTools(FrameContext context, EditorPanel<? extends EditorPanel<?>> editor) {
		super(context, "Format");
		
		this.editor = editor;

		fontComboBox = new JComboBox<>();
		fontComboBox.setFocusable(false);
		fontComboBox.addItem("Loading fonts..");
		toolBar.add(fontComboBox);
		fontComboBox.setAction(new FontFamilyAction(editor, fontComboBox, this::isUpdating));
		fontComboBox.setFont(fontComboBox.getFont().deriveFont(10.0F));
		fontComboBox.setMaximumSize(new Dimension(85, 50));

		/*
		String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		for (int i = 0; i < fonts.length; i++) {
			fontComboBox.addItem(fonts[i]);
		}
		*/

		fontSizeComboBox = new JComboBox<>();
		fontSizeComboBox.setFocusable(false);
		fontSizeComboBox.setEditable(true);
		fontSizeComboBox.addItem("8");
		fontSizeComboBox.addItem("9");
		fontSizeComboBox.addItem("10");
		fontSizeComboBox.addItem("11");
		fontSizeComboBox.addItem("12");
		fontSizeComboBox.addItem("14");
		fontSizeComboBox.addItem("16");
		fontSizeComboBox.addItem("18");
		fontSizeComboBox.addItem("20");
		fontSizeComboBox.addItem("22");
		fontSizeComboBox.addItem("24");
		fontSizeComboBox.addItem("26");
		fontSizeComboBox.addItem("28");
		fontSizeComboBox.addItem("36");
		fontSizeComboBox.addItem("48");
		fontSizeComboBox.addItem("72");
		fontSizeComboBox.setSelectedIndex(4);
		toolBar.add(fontSizeComboBox);
		fontSizeComboBox.setAction(new FontSizeAction(editor, fontSizeComboBox, this::isUpdating));
		fontSizeComboBox.setFont(fontSizeComboBox.getFont().deriveFont(10.0F));
		fontSizeComboBox.setMaximumSize(new Dimension(55, 50));

		ColorButton textColorButton = new ColorButton(new TextColorAction(editor)) {

			@Override
			protected Frame getFrame() {
				return editor.getFrame();
			}
		};
		textColorButton.setToolTipText("Text color");
		textColorButton.setIcon(Icons.formatColorText);
		textColorButton.setPaintMode(ColorButton.PAINT_MODE_BOTTOM);
		toolBar.add(textColorButton);

		toolBar.add(new BoldAction(editor));
		toolBar.add(new ItalicAction(editor));
		toolBar.add(new UnderlineAction(editor));
		toolBar.add(new StrikeThroughAction(editor));

		toolBar.add(new SetTextAlignmentAction(editor, StyleConstants.ALIGN_LEFT));
		toolBar.add(new SetTextAlignmentAction(editor, StyleConstants.ALIGN_CENTER));
		toolBar.add(new SetTextAlignmentAction(editor, StyleConstants.ALIGN_RIGHT));

		SplitButton bulletsButton = new SplitButton();
		bulletsButton.setArrowOffset(-3);
		bulletsButton.setToolTipText("Bulleted list");
		bulletsButton.setIcon(Icons.formatListBulletedSquare);
		toolBar.add(bulletsButton);

		JMenu bulletsMenu = new JMenu("Bullets");
		bulletsMenu.add(new ListBulletedAction(editor, ListBulletedAction.CIRCLE_BULLET, "Circle"));
		bulletsMenu.add(new ListBulletedAction(editor, ListBulletedAction.HYPHEN_BULLET, "Hyphen"));
		bulletsButton.setPopupMenu(bulletsMenu.getPopupMenu());

		// TODO
		JButton indentIncreaseButton = new JButton();
		indentIncreaseButton.setIcon(Icons.formatIndentIncrease);
		// toolBar.add(indentIncreaseButton);

		// TODO
		JButton indentDecreaseButton = new JButton();
		indentDecreaseButton.setIcon(Icons.formatIndentDecrease);
		// toolBar.add(indentDecreaseButton);

		SplitButton spaceAboveButton = new SplitButton();
		spaceAboveButton.setToolTipText("Line spacing");
		spaceAboveButton.setIcon(Icons.formatLineSpacing);
		toolBar.add(spaceAboveButton);

		JMenu spaceAboveMenu = new JMenu("Space above");
		spaceAboveMenu.add(new SpaceAboveAction(editor, 0.0F));
		spaceAboveMenu.add(new SpaceAboveAction(editor, 0.5F));
		spaceAboveMenu.add(new SpaceAboveAction(editor, 1.0F));
		spaceAboveMenu.add(new SpaceAboveAction(editor, 2.0F));
		spaceAboveMenu.add(new SpaceAboveAction(editor, 3.0F));
		spaceAboveMenu.add(new SpaceAboveAction(editor, 4.0F));
		spaceAboveMenu.add(new SpaceAboveAction(editor, 5.0F));
		spaceAboveMenu.add(new SpaceAboveAction(editor, 6.0F));
		spaceAboveMenu.add(new SpaceAboveAction(editor, 7.0F));
		spaceAboveMenu.add(new SpaceAboveAction(editor, 8.0F));
		spaceAboveMenu.add(new SpaceAboveAction(editor, 9.0F));
		spaceAboveMenu.add(new SpaceAboveAction(editor, 10.0F));
		spaceAboveButton.setPopupMenu(spaceAboveMenu.getPopupMenu());

	}

	public void setFontFamilyNames(List<String> fontFamilyNames) {

		fontComboBox.removeAllItems();

		defaultFontAvailable = false;
		
		fontComboBox.addItem(FontFamilyAction.DEFAULT_FONT_DISPLAY_NAME);

		for (String fontFamilyName : fontFamilyNames) {
			fontComboBox.addItem(fontFamilyName);
			if (TextStyle.DEFAULT_FONT.equals(fontFamilyName)) {
				defaultFontAvailable = true;
			}
		}

		fontComboBox.setSelectedItem(FontFamilyAction.DEFAULT_FONT_DISPLAY_NAME);

		fontComboBox.setMinimumSize(new Dimension(85, 1));
		fontComboBox.setMaximumSize(new Dimension(85, 50));

	}

	public boolean isUpdating() {
		return updating;
	}

	public void textSelectionChanged() {
		updateComponents(); // TODO: Add short delay to allow multiple events before updating?
	}

	protected void updateComponents() {

		updating = true;

		if (editor.getSelection().size() == 1) {

			Component component = editor.getSelection().get(0);
			if (component instanceof TextView) {

				TextView textView = (TextView) component;

				int start = textView.getSelectionStart();
				if (start < 0) {
					start = 0;
				}

				StyledDocument document = textView.getStyledDocument();
				Element element = document.getCharacterElement(start);
				AttributeSet style = element.getAttributes();

				String fontFamily = StyleConstants.getFontFamily(style);
				if (fontFamily == null) {
					fontComboBox.setSelectedItem(FontFamilyAction.DEFAULT_FONT_DISPLAY_NAME);
				} else if (!defaultFontAvailable && TextStyle.DEFAULT_FONT.equals(fontFamily)) {
					fontComboBox.setSelectedItem(FontFamilyAction.DEFAULT_FONT_DISPLAY_NAME);
				} else {
					fontComboBox.setSelectedItem(fontFamily);
				}

				fontSizeComboBox.setSelectedItem("" + StyleConstants.getFontSize(style)); //$NON-NLS-1$

			}

		}

		updating = false;

	}

}
