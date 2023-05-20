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
public abstract class Sides {

	private Sides() {

	}

	public static class Integer extends Sides {

		public static final String DEFAULT = null;
		public static final int DEFAULT_VALUE = 0;

		public int top = DEFAULT_VALUE;
		public int right = DEFAULT_VALUE;
		public int bottom = DEFAULT_VALUE;
		public int left = DEFAULT_VALUE;

		public Integer() {

		}

		public Integer(Sides.Integer sides) {
			from(sides);
		}

		public Integer(String sides) {
			parse(sides);
		}

		public Integer(int value) {
			this(value, value, value, value);
		}

		public Integer(int top, int right, int bottom, int left) {
			this.top = top;
			this.right = right;
			this.bottom = bottom;
			this.left = left;
		}

		public void from(Sides.Integer sides) {
			top = sides.top;
			right = sides.right;
			bottom = sides.bottom;
			left = sides.left;
		}

		public void parse(String sides) {

			if (sides != null) {

				List<java.lang.Integer> integers = new ArrayList<>();

				String[] split = sides.split(" ");
				for (String s : split) {
					try {
						integers.add(java.lang.Integer.parseInt(s));
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
				top = DEFAULT_VALUE;
				right = top;
				bottom = top;
				left = top;
			}

		}

		@Override
		public String toString() {
			if (right == top && bottom == top && left == top) {
				if (top == DEFAULT_VALUE) {
					return DEFAULT;
				} else {
					return "" + top;
				}
			} else {
				return "" + top + " " + right + " " + bottom + " " + left;
			}
		}

		public static String from(int sides) {
			if (sides == DEFAULT_VALUE) {
				return DEFAULT;
			} else {
				return "" + sides;
			}
		}

		public static String from(int top, int right, int bottom, int left) {
			return new Sides.Integer(top, right, bottom, left).toString();
		}

		@Override
		public boolean equals(Object object) {
			if (object instanceof Sides.Integer) {
				Sides.Integer sides = (Sides.Integer) object;
				return sides.top == top && sides.right == right && sides.bottom == bottom && sides.left == left;
			} else {
				return super.equals(object);
			}
		}

	}

}
