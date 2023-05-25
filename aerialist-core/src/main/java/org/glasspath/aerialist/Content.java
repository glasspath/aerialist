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
package org.glasspath.aerialist;

import java.util.ArrayList;
import java.util.List;

import org.glasspath.aerialist.Field.FieldType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonInclude(Include.NON_DEFAULT)
public class Content {

	@JacksonXmlProperty(isAttribute = false)
	private ContentRoot root = null;

	public Content() {

	}

	public ContentRoot getRoot() {
		return root;
	}

	public void setRoot(ContentRoot root) {
		this.root = root;
	}

	public List<String> getFieldKeys(FieldType fieldType) {

		List<String> fieldKeys = new ArrayList<>();

		if (root != null) {

			ContentParser contentParser = new ContentParser() {

				@Override
				public void parseIText(IText iText) {

					for (TextStyle textStyle : iText.getStyles()) {

						if (textStyle.source != null) {

							Field field = new Field(textStyle.source);
							if (fieldType == null || field.type == fieldType) {
								addFieldKey(field.key);
							}

						}

					}

				}

				private void addFieldKey(String key) {
					if (!fieldKeys.contains(key)) {
						fieldKeys.add(key);
					}
				}
			};

			contentParser.parseRoot(root);

		}

		return fieldKeys;

	}

	public List<String> getImageKeys() {

		List<String> imageKeys = new ArrayList<>();

		if (root != null) {

			ContentParser contentParser = new ContentParser() {

				@Override
				public void parseIText(IText iText) {
					for (TextStyle textStyle : iText.getStyles()) {
						if (textStyle.image != null) {
							addImageKey(textStyle.image);
						}
					}
				}

				@Override
				public void parseImage(Image image) {
					addImageKey(image.getSrc());
				}

				private void addImageKey(String key) {
					if (!imageKeys.contains(key)) {
						imageKeys.add(key);
					}
				}
			};

			contentParser.parseRoot(root);

		}

		return imageKeys;

	}

}
