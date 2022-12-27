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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glasspath.aerialist.IFieldContext;

@SuppressWarnings("nls")
public class TemplateFieldContext implements IFieldContext {

	private final Map<String, String> stringMap = new HashMap<>();
	private final Map<String, List<String>> listMap = new HashMap<>();

	private String defaultValue = "";

	public TemplateFieldContext() {

	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void put(String key, String value) {
		stringMap.put(key, value);
	}

	@Override
	public String getString(String key) {
		String value = stringMap.get(key);
		if (value != null) {
			return value;
		} else {
			return defaultValue;
		}
	}

	public void put(String key, List<String> value) {
		listMap.put(key, value);
	}

	@Override
	public List<String> getList(String key) {
		return listMap.get(key);
	}

}
