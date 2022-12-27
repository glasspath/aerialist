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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonInclude(Include.NON_DEFAULT)
@SuppressWarnings("nls")
public class TextStyle {

	public static String DEFAULT_FONT = "Roboto";
	// public static String DEFAULT_FONT = "Raleway";
	public static int DEFAULT_FONT_SIZE = 12;

	@JacksonXmlProperty(isAttribute = true)
	public int start = -1;

	@JacksonXmlProperty(isAttribute = true)
	public int end = -1;

	@JacksonXmlProperty(isAttribute = true)
	public float spaceAbove = 0.0F;

	@JacksonXmlProperty(isAttribute = true)
	public String font = null;

	@JacksonXmlProperty(isAttribute = true)
	public int fontSize = DEFAULT_FONT_SIZE;

	@JacksonXmlProperty(isAttribute = true)
	public boolean bold = false;

	@JacksonXmlProperty(isAttribute = true)
	public boolean italic = false;

	@JacksonXmlProperty(isAttribute = true)
	public boolean underline = false;

	@JacksonXmlProperty(isAttribute = true)
	public boolean strikeThrough = false;

	@JacksonXmlProperty(isAttribute = true)
	public String foreground = null;

	@JacksonXmlProperty(isAttribute = true)
	public String background = null;

	@JacksonXmlProperty(isAttribute = true)
	public String image = null;

	@JacksonXmlProperty(isAttribute = true)
	public String source = null;

	public TextStyle() {

	}

	public TextStyle(TextStyle textStyle) {

		this.start = textStyle.start;
		this.end = textStyle.end;
		this.spaceAbove = textStyle.spaceAbove;
		this.font = textStyle.font;
		this.fontSize = textStyle.fontSize;
		this.bold = textStyle.bold;
		this.italic = textStyle.italic;
		this.underline = textStyle.underline;
		this.strikeThrough = textStyle.strikeThrough;
		this.foreground = textStyle.foreground;
		this.background = textStyle.background;
		this.source = textStyle.source;

	}

}
