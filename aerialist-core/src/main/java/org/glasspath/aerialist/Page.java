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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonInclude(Include.NON_DEFAULT)
@SuppressWarnings("nls")
public class Page implements IElementContainer, IVisible {

	public enum PageSize {

		A4("A4", 595, 842);

		private final String name;
		private final int width;
		private final int height;

		PageSize(String name, int width, int height) {
			this.name = name;
			this.width = width;
			this.height = height;
		}

		public String getName() {
			return name;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

	}

	@JacksonXmlProperty(isAttribute = true)
	private int width = 0;

	@JacksonXmlProperty(isAttribute = true)
	private int height = 0;

	@JacksonXmlProperty(isAttribute = true)
	private String margin = null;

	@JacksonXmlProperty(isAttribute = true)
	private String visible = null;

	@JacksonXmlElementWrapper(localName = "elements")
	@JacksonXmlProperty(localName = "element")
	private List<Element> elements = new ArrayList<>();

	public Page() {

	}

	public Page(PageSize pageSize) {
		this.width = pageSize.width;
		this.height = pageSize.height;
	}

	public Page(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getMargin() {
		return margin;
	}

	public void setMargin(String margin) {
		this.margin = margin;
	}

	@Override
	public String getVisible() {
		return visible;
	}

	@Override
	public void setVisible(String visible) {
		this.visible = visible;
	}

	@Override
	public List<Element> getElements() {
		return elements;
	}

	@Override
	public void setElements(List<Element> elements) {
		this.elements = elements;
	}

}
