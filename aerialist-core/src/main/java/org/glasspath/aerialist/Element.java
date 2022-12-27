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
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = Group.class, name = "Group"),
		@JsonSubTypes.Type(value = TextBox.class, name = "TextBox"),
		@JsonSubTypes.Type(value = QrCode.class, name = "QrCode"),
		@JsonSubTypes.Type(value = Image.class, name = "Image"),
		@JsonSubTypes.Type(value = Table.class, name = "Table")
})
@JsonInclude(Include.NON_DEFAULT)
public abstract class Element {

	@JacksonXmlProperty(isAttribute = true)
	private int x = 0;

	@JacksonXmlProperty(isAttribute = true)
	private int y = 0;

	@JacksonXmlProperty(isAttribute = true)
	private int width = 0;

	@JacksonXmlProperty(isAttribute = true)
	private int height = 0;

	@JacksonXmlProperty(isAttribute = true)
	private String yPolicy = YPolicy.DEFAULT.stringValue;

	@JacksonXmlProperty(isAttribute = true)
	private String heightPolicy = HeightPolicy.DEFAULT.stringValue;

	@JacksonXmlProperty(isAttribute = true)
	private String background = null;

	@JacksonXmlElementWrapper(localName = "borders")
	@JacksonXmlProperty(localName = "border")
	private List<Border> borders = new ArrayList<>();

	public Element() {

	}

	public Element(Element element) {
		fromElement(element);
	}

	public void fromElement(Element element) {

		x = element.x;
		y = element.y;
		width = element.width;
		height = element.height;

		yPolicy = element.yPolicy;
		heightPolicy = element.heightPolicy;

		background = element.background;

		borders.clear();
		for (Border border : element.borders) {
			borders.add(new Border(border));
		}

	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
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

	public String getYPolicy() {
		return yPolicy;
	}

	public void setYPolicy(String yPolicy) {
		this.yPolicy = yPolicy;
	}

	public String getHeightPolicy() {
		return heightPolicy;
	}

	public void setHeightPolicy(String heightPolicy) {
		this.heightPolicy = heightPolicy;
	}

	public String getBackground() {
		return background;
	}

	public void setBackground(String background) {
		this.background = background;
	}

	public List<Border> getBorders() {
		return borders;
	}

	public void setBorders(List<Border> borders) {
		this.borders = borders;
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

}
