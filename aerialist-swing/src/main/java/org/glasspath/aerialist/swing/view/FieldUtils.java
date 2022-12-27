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

import org.glasspath.aerialist.DynamicFieldContext;
import org.glasspath.aerialist.Field;
import org.glasspath.aerialist.IText;
import org.glasspath.aerialist.TextStyle;
import org.glasspath.aerialist.swing.view.TextView.TextData;

public class FieldUtils {

	private FieldUtils() {

	}

	public static void updateDynamicFields(PageContainer pageContainer) {

		DynamicFieldContext fieldContext = new DynamicFieldContext();
		fieldContext.setMillis(System.currentTimeMillis());
		fieldContext.setPages(pageContainer.getPageViews().size());
		fieldContext.setPage(1);

		PageViewParser viewParser = new PageViewParser() {

			@Override
			protected void parseLayeredPageView(LayeredPageView layeredPageView) {
				super.parseLayeredPageView(layeredPageView);
				fieldContext.setPage(fieldContext.getPage() + 1);
			}

			@Override
			protected void parseTextView(TextView textView) {
				updateTextView(textView, fieldContext);
			}
		};

		viewParser.parsePageContainer(pageContainer);

	}

	public static void updateTextView(TextView textView, DynamicFieldContext fieldContext) {

		TextData iText = new TextData();
		textView.toText(iText);

		boolean updated = false;

		for (int i = 0; i < iText.getStyles().size(); i++) {

			TextStyle textStyle = iText.getStyles().get(i);
			if (textStyle.source != null) {

				Field field = new Field(textStyle.source);
				if (field.isDynamicField()) {

					String replacement = fieldContext.getString(field.key);
					if (replacement != null) {
						IText.replaceText(iText, i, replacement);
						updated = true;
					}

				}

			}

		}

		if (updated) {
			textView.init(iText);
		}

	}

}
