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
package org.glasspath.aerialist.text.font;

@SuppressWarnings("nls")
public enum FontWeight {

	THIN(100, "thin"),
	EXTRA_LIGHT(200, "extralight", "ultralight"),
	LIGHT(300, "light"),
	REGULAR(400, "regular", "normal"),
	MEDIUM(500, "medium"),
	SEMI_BOLD(600, "semibold", "demibold"),
	BOLD(700, "bold"),
	EXTRA_BOLD(800, "extrabold", "ultrabold"),
	BLACK(900, "black", "heavy"),
	EXTRA_BLACK(950, "extrablack", "ultrablack", "extraheavy", "ultraheavy");

	public final int weight;
	public final String[] names;

	FontWeight(int weight, String... names) {
		this.weight = weight;
		this.names = names;
	}

	public boolean isBoldWeight() {
		return weight >= SEMI_BOLD.weight; // TODO: Is it OK to include SEMI_BOLD?
	}

	public static FontWeight getFontWeight(String name) {

		if (name != null) {

			// TODO: For efficiency toLowerCase().replaceAll("[^A-Za-z0-9]", "") will have to be done outside of this method
			//       Should we add an extra argument which defines this behavior?
			// name = name.toLowerCase().replaceAll("[^A-Za-z0-9]", "");

			if (containsWeightName(name, REGULAR)) {
				return REGULAR;
			} else if (containsWeightName(name, SEMI_BOLD)) {
				return SEMI_BOLD;
			} else if (containsWeightName(name, EXTRA_BOLD)) {
				return EXTRA_BOLD;
			} else if (containsWeightName(name, BOLD)) {
				return BOLD;
			} else if (containsWeightName(name, MEDIUM)) {
				return MEDIUM;
			} else if (containsWeightName(name, EXTRA_LIGHT)) {
				return EXTRA_LIGHT;
			} else if (containsWeightName(name, LIGHT)) {
				return LIGHT;
			} else if (containsWeightName(name, THIN)) {
				return THIN;
			} else if (containsWeightName(name, EXTRA_BLACK)) {
				return EXTRA_BLACK;
			} else if (containsWeightName(name, BLACK)) {
				return BLACK;
			}

		}

		return REGULAR;

	}

	private static boolean containsWeightName(String name, FontWeight weight) {

		for (String weightName : weight.names) {
			if (name.contains(weightName)) {
				return true;
			}
		}

		return false;

	}

}
