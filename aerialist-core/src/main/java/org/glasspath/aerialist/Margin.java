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

@SuppressWarnings("nls")
public class Margin {

	public static final String DEFAULT = null;
	public static final int DEFAULT_MARGIN = 0;

	public int top = DEFAULT_MARGIN;
	public int right = DEFAULT_MARGIN;
	public int bottom = DEFAULT_MARGIN;
	public int left = DEFAULT_MARGIN;

	public Margin() {

	}

	public Margin(Margin margin) {
		from(margin);
	}

	public Margin(String margin) {
		parse(margin);
	}

	public Margin(int margin) {
		this(margin, margin, margin, margin);
	}

	public Margin(int top, int right, int bottom, int left) {
		this.top = top;
		this.right = right;
		this.bottom = bottom;
		this.left = left;
	}

	public void from(Margin margin) {
		top = margin.top;
		right = margin.right;
		bottom = margin.bottom;
		left = margin.left;
	}

	public void parse(String margin) {

		if (margin != null) {

			List<Integer> integers = new ArrayList<>();

			String[] split = margin.split(" ");
			for (String s : split) {
				try {
					integers.add(Integer.parseInt(s));
				} catch (Exception e) {
					// Not an integer
				}
			}

			if (integers.size() >= 4) {
				top = integers.get(0);
				right = integers.get(1);
				bottom = integers.get(2);
				left = integers.get(3);
			} else if (integers.size() == 3) {
				top = integers.get(0);
				right = integers.get(1);
				bottom = integers.get(2);
				left = right;
			} else if (integers.size() == 2) {
				top = integers.get(0);
				right = integers.get(1);
				bottom = top;
				left = right;
			} else if (integers.size() == 1) {
				top = integers.get(0);
				right = top;
				bottom = top;
				left = top;
			}

		} else {
			top = DEFAULT_MARGIN;
			right = top;
			bottom = top;
			left = top;
		}

	}

	@Override
	public String toString() {
		if (right == top && bottom == top && left == top) {
			if (top == DEFAULT_MARGIN) {
				return DEFAULT;
			} else {
				return "" + top;
			}
		} else {
			return "" + top + " " + right + " " + bottom + " " + left;
		}
	}

	public static String from(int margin) {
		if (margin == DEFAULT_MARGIN) {
			return DEFAULT;
		} else {
			return "" + margin;
		}
	}

	public static String from(int top, int right, int bottom, int left) {
		return new Margin(top, right, bottom, left).toString();
	}

}
