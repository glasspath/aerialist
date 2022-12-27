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
package org.glasspath.aerialist.template;

import java.util.ArrayList;
import java.util.List;

public class TemplateMetadata {

	private AbstractMetadata root = null;

	public TemplateMetadata() {

	}

	public TemplateMetadata(AbstractMetadata root) {
		this.root = root;
	}

	public AbstractMetadata getRoot() {
		return root;
	}

	public void setRoot(AbstractMetadata root) {
		this.root = root;
	}

	public static class CategoryMetadata extends MetadataList {

		public CategoryMetadata(String name) {
			super(name);
		}

	}

	public static class TableMetadata extends MetadataList {

		public TableMetadata(String name) {
			super(name);
		}

	}

	public static class FieldMetadata extends AbstractMetadata {

		private final String key;

		public FieldMetadata(String name, String key) {
			super(name);
			this.key = key;
		}

		public String getKey() {
			return key;
		}

	}

	public static abstract class MetadataList extends AbstractMetadata {

		private List<AbstractMetadata> children = new ArrayList<>();

		public MetadataList(String name) {
			super(name);
		}

		public List<AbstractMetadata> getChildren() {
			return children;
		}

	}

	public static abstract class AbstractMetadata {

		private final String name;

		public AbstractMetadata(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

	}

}
