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

@SuppressWarnings("nls")
public class Field {

	public static enum FieldType {

		DYNAMIC("d:", "Dynamic field"),
		TEMPLATE("t:", "Template field");

		private final String identifier;
		private final String description; // TODO: Change to descriptionKey for i18n

		FieldType(String identifier, String description) {
			this.identifier = identifier;
			this.description = description;
		}

		public String getIdentifier() {
			return identifier;
		}

		public String getDescription() {
			return description;
		}

		public static FieldType get(String source) {

			if (source != null) {

				for (FieldType fieldType : values()) {
					if (source.startsWith(fieldType.identifier)) {
						return fieldType;
					}
				}
			}

			return null;

		}

	}

	public static enum DynamicFieldKey {

		MILLIS("Millis", "Milliseconds"),
		PAGE("Page", "Page"),
		PAGES("Pages", "Pages");

		private final String key;
		private final String description; // TODO: Change to descriptionKey for i18n

		DynamicFieldKey(String key, String description) {
			this.key = key;
			this.description = description;
		}

		public String getKey() {
			return key;
		}

		public String getDescription() {
			return description;
		}

		public static DynamicFieldKey get(String key) {

			if (key != null) {

				for (DynamicFieldKey documentFieldKey : values()) {
					if (documentFieldKey.key.equals(key)) {
						return documentFieldKey;
					}
				}

			}

			return null;

		}

	}

	public final FieldType type;
	public final String key;

	public Field(String source) {
		type = FieldType.get(source);
		if (type != null) {
			key = source.substring(2);
		} else {
			key = null;
		}
	}

	public boolean isDynamicField() {
		return type == FieldType.DYNAMIC;
	}

	public boolean isTemplateField() {
		return type == FieldType.TEMPLATE;
	}

}
