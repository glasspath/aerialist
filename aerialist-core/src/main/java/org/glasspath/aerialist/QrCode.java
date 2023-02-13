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
public class QrCode extends Element implements IText, IScalable {

	@JacksonXmlProperty(isAttribute = false)
	private String text = "";

	@JacksonXmlProperty(isAttribute = true)
	private float scale = 1.0F;

	@JacksonXmlProperty(isAttribute = true)
	private String alignment = Alignment.DEFAULT.stringValue;

	@JacksonXmlProperty(isAttribute = true)
	private String fit = FitPolicy.DEFAULT.stringValue;

	@JacksonXmlElementWrapper(localName = "styles")
	@JacksonXmlProperty(localName = "style")
	private List<TextStyle> styles = new ArrayList<>();

	public QrCode() {

	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public float getScale() {
		return scale;
	}

	@Override
	public void setScale(float scale) {
		this.scale = scale;
	}

	@Override
	public String getAlignment() {
		return alignment;
	}

	@Override
	public void setAlignment(String alignment) {
		this.alignment = alignment;
	}

	@Override
	public String getFit() {
		return fit;
	}

	@Override
	public void setFit(String fit) {
		this.fit = fit;
	}

	@Override
	public List<TextStyle> getStyles() {
		return styles;
	}

	@Override
	public void setStyles(List<TextStyle> styles) {
		this.styles = styles;
	}

}
