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
package org.glasspath.aerialist.swing.view;

import java.awt.Color;

@SuppressWarnings("nls")
public class ColorUtils {

	private ColorUtils() {

	}

	public static String toHex(Object object) {
		if (object instanceof Color) {
			return toHex((Color) object);
		} else {
			return null;
		}
	}

	public static String toHex(Color color) {
		if (color == null) {
			return null;
		} else {
			return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
		}
	}

	public static Color fromHex(String hex) {
		if (hex != null && hex.length() > 0) {
			try {
				return Color.decode(hex);
			} catch (Exception e) {
				return null;
			}
		} else {
			return null;
		}
	}

}
