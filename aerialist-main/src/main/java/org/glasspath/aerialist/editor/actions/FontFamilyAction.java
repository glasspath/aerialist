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
package org.glasspath.aerialist.editor.actions;

import java.awt.event.ActionEvent;
import java.util.function.Supplier;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.glasspath.aerialist.TextStyle;
import org.glasspath.aerialist.editor.EditorPanel;

public class FontFamilyAction extends TextStyleAction {

	public static final String DEFAULT_FONT_DISPLAY_NAME = "Default";

	private final JComboBox<String> fontComboBox;
	private final Supplier<Boolean> updatingSupplier;

	public FontFamilyAction(EditorPanel<? extends EditorPanel<?>> context, JComboBox<String> fontComboBox, Supplier<Boolean> updatingSupplier) {
		super(context, null, false, true);

		this.fontComboBox = fontComboBox;
		this.updatingSupplier = updatingSupplier;

		putValue(Action.NAME, "Font");
		putValue(Action.SHORT_DESCRIPTION, "Font");

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!updatingSupplier.get()) {
			super.actionPerformed(e);
		}
	}

	/*
	@Override
	protected void updateTextView(TextView textView) {
	
		String fontFamily = (String) fontComboBox.getSelectedItem();
	
		MutableAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setFontFamily(attr, fontFamily);
		setCharacterAttributes(textView, attr, false);
	
	}
	*/

	@Override
	protected void updateAttributeSet(MutableAttributeSet inputAttributes, SimpleAttributeSet attributeSet) {

		String fontFamily = (String) fontComboBox.getSelectedItem();

		try {
			if (DEFAULT_FONT_DISPLAY_NAME.equals(fontFamily)) {
				StyleConstants.setFontFamily(attributeSet, TextStyle.DEFAULT_FONT);
			} else {
				StyleConstants.setFontFamily(attributeSet, fontFamily);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
