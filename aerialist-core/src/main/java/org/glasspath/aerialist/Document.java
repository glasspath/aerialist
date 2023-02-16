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
public class Document extends ContentRoot implements IPagination {

	@JacksonXmlProperty(isAttribute = true)
	private int headerHeight = 90;

	@JacksonXmlProperty(isAttribute = true)
	private int footerHeight = 90;

	@JacksonXmlProperty(isAttribute = false)
	private Page header = null;

	@JacksonXmlProperty(isAttribute = false)
	private Page footer = null;

	@JacksonXmlProperty(isAttribute = false)
	private Pagination pagination = null;

	@JacksonXmlElementWrapper(localName = "pages")
	@JacksonXmlProperty(localName = "page")
	private List<Page> pages = new ArrayList<>();

	public Document() {

	}

	public int getHeaderHeight() {
		return headerHeight;
	}

	public void setHeaderHeight(int headerHeight) {
		this.headerHeight = headerHeight;
	}

	public int getFooterHeight() {
		return footerHeight;
	}

	public void setFooterHeight(int footerHeight) {
		this.footerHeight = footerHeight;
	}

	public Page getHeader() {
		return header;
	}

	public void setHeader(Page header) {
		this.header = header;
	}

	public Page getFooter() {
		return footer;
	}

	public void setFooter(Page footer) {
		this.footer = footer;
	}

	@Override
	public Pagination getPagination() {
		return pagination;
	}

	@Override
	public void setPagination(Pagination pagination) {
		this.pagination = pagination;
	}

	public List<Page> getPages() {
		return pages;
	}

	public void setPages(List<Page> pages) {
		this.pages = pages;
	}

}
