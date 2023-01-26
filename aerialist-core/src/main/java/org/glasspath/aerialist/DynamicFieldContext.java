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

import java.util.List;

import org.glasspath.aerialist.Field.DynamicFieldKey;

@SuppressWarnings("nls")
public class DynamicFieldContext implements IFieldContext {

	private String defaultValue = null;

	private long millis = 0;
	private int pages = 0;
	private int page = 0;

	public DynamicFieldContext() {

	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public long getMillis() {
		return millis;
	}

	public void setMillis(long millis) {
		this.millis = millis;
	}

	public int getPages() {
		return pages;
	}

	public void setPages(int pages) {
		this.pages = pages;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	@Override
	public String getString(String key) {

		DynamicFieldKey dynamicFieldKey = DynamicFieldKey.get(key);
		if (dynamicFieldKey != null) {

			switch (dynamicFieldKey) {

			case MILLIS:
				return "" + millis;

			case PAGE:
				return "" + page;

			case PAGES:
				return "" + pages;

			default:
				break;
			}

		}

		return defaultValue;

	}

	@Override
	public List<String> getList(String key) {
		return null;
	}

	@Override
	public Object getObject(String key) {
		return getString(key); // TODO?
	}

}
