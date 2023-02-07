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

import org.glasspath.aerialist.editor.EditorPanel;
import org.glasspath.aerialist.swing.view.TextView;

public class InsertFieldAction extends TextStyleAction {

	private final String key;
	private final String description;

	public InsertFieldAction(EditorPanel<? extends EditorPanel<?>> context, String key, String description) {
		this(context, null, key, description);
	}

	public InsertFieldAction(EditorPanel<? extends EditorPanel<?>> context, TextView textView, String key, String description) {
		super(context, textView);

		this.key = key;
		this.description = description;

		putValue(Action.NAME, description);
		putValue(Action.SHORT_DESCRIPTION, description);

	}

	@Override
	protected void updateTextView(TextView textView) {
		textView.insertField(key, description);
	}

	@Override
	protected void updateAttributeSet(MutableAttributeSet inputAttributes, SimpleAttributeSet attributeSet) {

	}

}
