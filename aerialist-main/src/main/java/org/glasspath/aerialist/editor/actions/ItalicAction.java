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

import javax.swing.Action;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.glasspath.aerialist.editor.AbstractEditorPanel;
import org.glasspath.aerialist.icons.Icons;

public class ItalicAction extends ToggleTextStyleAction {

	public ItalicAction(AbstractEditorPanel context) {
		super(context);

		putValue(Action.NAME, "Italic");
		putValue(Action.SHORT_DESCRIPTION, "Italic");
		putValue(Action.SMALL_ICON, Icons.formatItalic);

	}

	@Override
	protected boolean getStyle(AttributeSet attributeSet) {
		return StyleConstants.isItalic(attributeSet);
	}

	@Override
	protected void setStyle(SimpleAttributeSet attributeSet, boolean value) {
		StyleConstants.setItalic(attributeSet, value);
	}

}
