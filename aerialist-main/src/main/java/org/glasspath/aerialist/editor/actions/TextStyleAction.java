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

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.EditorKit;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;

import org.glasspath.aerialist.editor.EditorPanel;
import org.glasspath.aerialist.swing.view.TextView;

public abstract class TextStyleAction extends AbstractAction {

	protected final EditorPanel<? extends EditorPanel<?>> context;
	protected final TextView textView;
	protected final boolean reload;

	public TextStyleAction(EditorPanel<? extends EditorPanel<?>> context) {
		this(context, null);
	}

	public TextStyleAction(EditorPanel<? extends EditorPanel<?>> context, TextView textView) {
		this(context, textView, false);
	}

	public TextStyleAction(EditorPanel<? extends EditorPanel<?>> context, TextView textView, boolean reload) {
		this.context = context;
		this.textView = textView;
		this.reload = reload;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (textView != null) {
			updateTextView(textView);
		} else {
			for (Component component : context.getSelection()) {
				if (component instanceof TextView) {
					updateTextView((TextView) component);
				}
			}
		}
	}

	/*
	protected void performAction(String actionName) {
		for (Component component : context.getSelection()) {
			if (component instanceof TextView) {
				for (Action action : ((TextView) component).getActions()) {
					if (actionName.equals(action.getValue(Action.NAME))) {
						action.actionPerformed(null);
						break;
					}
				}
			}
		}
	}
	*/

	protected void updateTextView(TextView textView) {

		EditorKit editorKit = textView.getEditorKit();
		if (editorKit instanceof StyledEditorKit) {

			StyledEditorKit kit = (StyledEditorKit) editorKit;

			MutableAttributeSet attr = kit.getInputAttributes();
			SimpleAttributeSet sas = new SimpleAttributeSet();
			updateAttributeSet(attr, sas);

			setCharacterAttributes(textView, sas, false);

			if (reload) {
				textView.reload();
			}

		}

	}

	protected void updateAttributeSet(MutableAttributeSet inputAttributes, SimpleAttributeSet attributeSet) {

	}

	protected final void setCharacterAttributes(JTextPane textPane, AttributeSet attr, boolean replace) {

		EditorKit editorKit = textPane.getEditorKit();
		if (editorKit instanceof StyledEditorKit) {

			StyledEditorKit kit = (StyledEditorKit) editorKit;

			int p0 = textPane.getSelectionStart();
			int p1 = textPane.getSelectionEnd();
			if (p0 != p1) {
				StyledDocument doc = textPane.getStyledDocument();
				doc.setCharacterAttributes(p0, p1 - p0, attr, replace);
			}

			MutableAttributeSet inputAttributes = kit.getInputAttributes();
			if (replace) {
				inputAttributes.removeAttributes(inputAttributes);
			}
			inputAttributes.addAttributes(attr);

		}

	}

	protected final void setParagraphAttributes(JTextPane textPane, AttributeSet attr, boolean replace) {

		int p0 = textPane.getSelectionStart();
		int p1 = textPane.getSelectionEnd();
		if (p0 != p1) {

			int length = p1 - p0;

			StyledDocument doc = textPane.getStyledDocument();

			try {

				String text = doc.getText(p0, length);

				doc.remove(p0, length);
				doc.insertString(p0, text, attr);

				doc.setParagraphAttributes(p0, length, attr, false);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

}
