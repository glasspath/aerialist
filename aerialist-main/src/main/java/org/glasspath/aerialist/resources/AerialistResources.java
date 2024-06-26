/*
 * This file is part of Glasspath Revenue.
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
package org.glasspath.aerialist.resources;

import java.util.ResourceBundle;

public class AerialistResources {

	@SuppressWarnings("unused")
	private static final AerialistResources INSTANCE = new AerialistResources();
	public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("org.glasspath.aerialist.resources.messages"); //$NON-NLS-1$

	private AerialistResources() {

	}

	public static String getString(String key) {
		try {
			return BUNDLE.getString(key);
		} catch (Exception e) {
			System.err.println("Language key not found: " + key); //$NON-NLS-1$
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

}
