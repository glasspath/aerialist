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

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.glasspath.aerialist.editor.AbstractEditorPanel;
import org.glasspath.aerialist.resources.Resources;
import org.glasspath.common.swing.color.ColorChooserPanel.ColorEvent;

public class TextColorAction extends TextStyleAction {

	private Color color = null;

	public TextColorAction(AbstractEditorPanel context) {
		super(context);

		putValue(Action.NAME, Resources.getString("TextColor")); //$NON-NLS-1$
		putValue(Action.SHORT_DESCRIPTION, Resources.getString("TextColor")); //$NON-NLS-1$

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e instanceof ColorEvent) {
			color = ((ColorEvent) e).color;
			super.actionPerformed(e);
		}
	}

	@Override
	protected void updateAttributeSet(MutableAttributeSet inputAttributes, SimpleAttributeSet attributeSet) {
		StyleConstants.setForeground(attributeSet, color);
	}

}
