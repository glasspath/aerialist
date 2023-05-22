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

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyledDocument;

import org.glasspath.aerialist.DynamicFieldContext;
import org.glasspath.aerialist.Field;
import org.glasspath.aerialist.TextStyle;
import org.glasspath.aerialist.swing.view.TextView.TextData;

public class DynamicFieldCache {

	private final List<CachedField> cachedFields = new ArrayList<>();

	public DynamicFieldCache(LayeredPageView layeredPageView) {

		// Find all the TextView's which contain a dynamic field
		// For now we only do this for one page because this class is only
		// used for refreshing a single page (video overlay)
		PageViewParser viewParser = new PageViewParser() {

			@Override
			protected void parseTextView(TextView textView) {

				TextData iText = new TextData();
				textView.toText(iText);

				for (int i = 0; i < iText.getStyles().size(); i++) {

					TextStyle textStyle = iText.getStyles().get(i);
					if (textStyle.source != null) {

						Field field = new Field(textStyle.source);
						if (field.isDynamicField()) {

							cachedFields.add(new CachedField(field, textView, textView.getAttributes(textStyle.start), textStyle.start, textStyle.end - textStyle.start));

						}

					}

				}

			}
		};
		viewParser.parseLayeredPageView(layeredPageView);

		// Find fields that follow behind a field in the same TextView, the start
		// position of these fields has to be updated when a field is replaced
		for (int i = 0; i < cachedFields.size(); i++) {

			CachedField cachedField = cachedFields.get(i);

			for (int j = 0; j < cachedFields.size(); j++) {

				CachedField nextField = cachedFields.get(j);

				if (i != j && nextField.textView == cachedField.textView && nextField.start > cachedField.start) {

					if (cachedField.nextFields == null) {
						cachedField.nextFields = new ArrayList<>();
					}

					cachedField.nextFields.add(nextField);

				}

			}

		}

	}

	public void updateDynamicFields(DynamicFieldContext fieldContext) {

		for (CachedField cachedField : cachedFields) {

			String replacement = fieldContext.getString(cachedField.field.key);
			if (replacement != null) {

				int oldLength = cachedField.length;

				StyledDocument document = cachedField.textView.getStyledDocument();

				cachedField.textView.setUpdatingComponent(true);

				try {

					document.remove(cachedField.start, cachedField.length);
					cachedField.length = 0;

					document.insertString(cachedField.start, replacement, cachedField.attributeSet);
					cachedField.length = replacement.length();

				} catch (Exception e) {
					// TODO?
				}

				cachedField.textView.setUpdatingComponent(false);

				if (cachedField.nextFields != null) {

					int delta = cachedField.length - oldLength;
					if (delta != 0) {

						for (CachedField nextField : cachedField.nextFields) {
							nextField.start += delta;
						}

					}

				}

			}

		}

	}

	public static class CachedField {

		private final Field field;
		private final TextView textView;
		private final MutableAttributeSet attributeSet;
		private int start = 0;
		private int length = 0;
		private List<CachedField> nextFields = null;

		public CachedField(Field field, TextView textView, MutableAttributeSet attributeSet, int start, int length) {
			this.field = field;
			this.textView = textView;
			this.attributeSet = attributeSet;
			this.start = start;
			this.length = length;
		}

	}

}
