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
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;

import org.glasspath.aerialist.editor.AbstractEditorPanel;
import org.glasspath.aerialist.swing.view.TextView;

public class ListBulletedAction extends TextStyleAction {

	public static final String CIRCLE_BULLET = " • ";
	public static final String HYPHEN_BULLET = " - ";
	/* TODO: Not supported by OpenPDF?
	public static final String WHITE_BULLET = " ◦ ";
	public static final String TRIANGLE_BULLET = " ‣ ";
	public static final String WHITE_POINT_BULLET = " ⦾ ";
	public static final String BLACK_POINT_BULLET = " ⦿ ";
	*/

	private final String prefix;

	public ListBulletedAction(AbstractEditorPanel context, String prefix, String name) {
		super(context);

		this.prefix = prefix;

		putValue(Action.NAME, prefix + name);
		putValue(Action.SHORT_DESCRIPTION, "Bulleted List");

	}

	@Override
	protected void updateTextView(TextView textView) {
		textView.toggleLinePrefix(prefix);
	}

	@Override
	protected void updateAttributeSet(MutableAttributeSet inputAttributes, SimpleAttributeSet attributeSet) {

	}

}
