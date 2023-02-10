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
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.Utilities;
import javax.swing.text.AbstractDocument.LeafElement;

import org.glasspath.aerialist.editor.EditorPanel;
import org.glasspath.aerialist.swing.view.TextView;
import org.glasspath.aerialist.swing.view.TextView.AttributeProcessor;

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

	protected void updateTextView(TextView textView) {

		EditorKit editorKit = textView.getEditorKit();
		if (editorKit instanceof StyledEditorKit) {

			textView.setUpdatingComponent(true);

			StyledEditorKit kit = (StyledEditorKit) editorKit;

			MutableAttributeSet inputAttributes = kit.getInputAttributes();
			SimpleAttributeSet newAttributes = new SimpleAttributeSet();
			updateAttributeSet(inputAttributes, newAttributes);

			processAttributes(textView, new AttributeProcessor() {

				@Override
				public boolean processAttributes(JTextPane textPane, LeafElement leafElement, int start, int end) {
					setCharacterAttributes(textView, newAttributes, start, end, false);
					return false;
				}
			});

			textView.createUndoableEdit();
			textView.setUpdatingComponent(false);

			if (reload) {
				textView.reload();
			}

		}

	}

	protected void processAttributes(TextView textView, AttributeProcessor attributeProcessor) {

		StyledDocument document = textView.getStyledDocument();

		int start = textView.getSelectionStart();
		int end = textView.getSelectionEnd();

		if (applyToParagraph) {

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

		} else {

			// Extend selection to start/end of fields to make sure the style is applied
			// to the whole field (otherwise field will be split into two fields)

			int fieldStartOffset = textView.getFieldOffset(start, false);
			if (fieldStartOffset >= 0 && fieldStartOffset < start) {
				start = fieldStartOffset;
			}

			int fieldEndOffset = textView.getFieldOffset(end, true);
			if (fieldEndOffset >= 0 && fieldEndOffset > end) {
				end = fieldEndOffset;
			}

		}

		textView.processAttributes(start, end, attributeProcessor);

	}

	protected abstract void updateAttributeSet(MutableAttributeSet inputAttributes, SimpleAttributeSet attributeSet);

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

}
