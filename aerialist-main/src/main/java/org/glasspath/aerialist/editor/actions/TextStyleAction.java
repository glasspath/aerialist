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
import javax.swing.text.AbstractDocument.LeafElement;
import javax.swing.text.AttributeSet;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.Utilities;

import org.glasspath.aerialist.editor.EditorPanel;
import org.glasspath.aerialist.swing.view.TextView;

public abstract class TextStyleAction extends AbstractAction {

	protected final EditorPanel<? extends EditorPanel<?>> context;
	protected final TextView textView;
	protected final boolean applyToParagraph;
	protected final boolean reload;

	public TextStyleAction(EditorPanel<? extends EditorPanel<?>> context) {
		this(context, null);
	}

	public TextStyleAction(EditorPanel<? extends EditorPanel<?>> context, TextView textView) {
		this(context, textView, false);
	}

	public TextStyleAction(EditorPanel<? extends EditorPanel<?>> context, TextView textView, boolean applyToParagraph) {
		this(context, textView, applyToParagraph, false);
	}

	public TextStyleAction(EditorPanel<? extends EditorPanel<?>> context, TextView textView, boolean applyToParagraph, boolean reload) {
		this.context = context;
		this.textView = textView;
		this.applyToParagraph = applyToParagraph;
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

			MutableAttributeSet inputAttributes = kit.getInputAttributes();
			SimpleAttributeSet attributeSet = new SimpleAttributeSet();
			updateAttributeSet(inputAttributes, attributeSet);

			int start = textView.getSelectionStart();
			int end = textView.getSelectionEnd();

			if (applyToParagraph) {

				StyledDocument document = textView.getStyledDocument();

				Element paragraph = Utilities.getParagraphElement(textView, start);
				if (paragraph != null) {
					start = paragraph.getStartOffset();
				}

				paragraph = Utilities.getParagraphElement(textView, end);
				if (paragraph != null) {
					end = paragraph.getEndOffset();
					if (end > document.getLength()) {
						end = document.getLength();
					}
				}

				if (end > start) {

					int i = start;
					while (i < end) {

						Element element = document.getCharacterElement(i);
						AttributeSet style = element.getAttributes();

						if (style instanceof LeafElement) {

							LeafElement leafElement = (LeafElement) style;

							setCharacterAttributes(textView, attributeSet, leafElement.getStartOffset(), leafElement.getEndOffset(), false);

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

			} else {

				// Extend selection to start/end of fields to make sure the style is applied
				// to the whole field (otherwise field will be split into two fields)

				int fieldStartOffset = getFieldOffset(textView, start, false);
				if (fieldStartOffset >= 0 && fieldStartOffset < start) {
					start = fieldStartOffset;
				}

				int fieldEndOffset = getFieldOffset(textView, end, true);
				if (fieldEndOffset >= 0 && fieldEndOffset > end) {
					end = fieldEndOffset;
				}

				setCharacterAttributes(textView, attributeSet, start, end, false);

			}

			if (reload) {
				textView.reload();
			}

		}

	}

	protected void updateAttributeSet(MutableAttributeSet inputAttributes, SimpleAttributeSet attributeSet) {

	}

	protected final void setCharacterAttributes(JTextPane textPane, AttributeSet attributeSet, boolean replace) {
		setCharacterAttributes(textPane, attributeSet, textPane.getSelectionStart(), textPane.getSelectionEnd(), replace);
	}

	protected final void setCharacterAttributes(JTextPane textPane, AttributeSet attributeSet, int start, int end, boolean replace) {

		if (end > start) {
			textPane.getStyledDocument().setCharacterAttributes(start, end - start, attributeSet, replace);
		}

		if (replace) {

			EditorKit editorKit = textPane.getEditorKit();
			if (editorKit instanceof StyledEditorKit) {

				MutableAttributeSet inputAttributes = ((StyledEditorKit) editorKit).getInputAttributes();
				inputAttributes.removeAttributes(inputAttributes);
				inputAttributes.addAttributes(attributeSet);

			}

		}

	}

	public static int getFieldOffset(TextView textView, int pos, boolean endOffset) {

		StyledDocument document = textView.getStyledDocument();
		Element element = document.getCharacterElement(pos);
		AttributeSet style = element.getAttributes();

		String source = (String) style.getAttribute(TextView.SOURCE_ATTRIBUTE);
		if (source != null && source.length() > 0 && style instanceof LeafElement) {

			LeafElement leafElement = (LeafElement) style;
			if (endOffset) {
				return leafElement.getEndOffset();
			} else {
				return leafElement.getStartOffset();
			}

		}

		return -1;

	}

	/*
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
	*/

}
