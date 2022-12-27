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
public enum Alignment {

	// Default and left both have value 0 because left is also default (and we want to use same values as StyleConstants)
	DEFAULT("", 0),
	LEFT("left", 0),
	CENTER("center", 1),
	RIGHT("right", 2);

	public final String stringValue;
	public final int intValue;

	Alignment(String stringValue, int intValue) {
		this.stringValue = stringValue;
		this.intValue = intValue;
	}

	public static Alignment get(String value) {

		if (value != null) {
			value = value.trim().toLowerCase();
		} else {
			return DEFAULT;
		}

		for (Alignment alignment : values()) {
			if (alignment.stringValue.equals(value)) {
				return alignment;
			}
		}

		return DEFAULT;

	}

	public static String get(int value) {

		for (Alignment alignment : values()) {
			if (alignment.intValue == value) {
				return alignment.stringValue;
			}
		}

		return DEFAULT.stringValue;

	}

}
