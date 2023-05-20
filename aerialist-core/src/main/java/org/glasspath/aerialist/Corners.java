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
public abstract class Corners {

	private Corners() {

	}

	public static class Float extends Corners {

		public static final String DEFAULT = null;
		public static final float DEFAULT_VALUE = 0.0F;

		public float topLeft = DEFAULT_VALUE;
		public float topRight = DEFAULT_VALUE;
		public float bottomRight = DEFAULT_VALUE;
		public float bottomLeft = DEFAULT_VALUE;

		public Float() {

		}

		public Float(Corners.Float corners) {
			from(corners);
		}

		public Float(String corners) {
			parse(corners);
		}

		public Float(float value) {
			this(value, value, value, value);
		}

		public Float(float topLeft, float topRight, float bottomRight, float bottomLeft) {
			this.topLeft = topLeft;
			this.topRight = topRight;
			this.bottomRight = bottomRight;
			this.bottomLeft = bottomLeft;
		}

		public void from(Corners.Float corners) {
			topLeft = corners.topLeft;
			topRight = corners.topRight;
			bottomRight = corners.bottomRight;
			bottomLeft = corners.bottomLeft;
		}

		public void parse(String corners) {

			if (corners != null) {

				List<java.lang.Float> floats = new ArrayList<>();

				String[] split = corners.split(" ");
				for (String s : split) {
					try {
						floats.add(java.lang.Float.parseFloat(s));
					} catch (Exception e) {
						// Not an integer
					}
				}

				if (floats.size() >= 4) {
					topLeft = floats.get(0);
					topRight = floats.get(1);
					bottomRight = floats.get(2);
					bottomLeft = floats.get(3);
				} else if (floats.size() == 3) {
					topLeft = floats.get(0);
					topRight = floats.get(1);
					bottomRight = floats.get(2);
					bottomLeft = topRight;
				} else if (floats.size() == 2) {
					topLeft = floats.get(0);
					topRight = topLeft;
					bottomRight = floats.get(1);
					bottomLeft = bottomRight;
				} else if (floats.size() == 1) {
					topLeft = floats.get(0);
					topRight = topLeft;
					bottomRight = topLeft;
					bottomLeft = topLeft;
				}

			} else {
				topLeft = DEFAULT_VALUE;
				topRight = topLeft;
				bottomRight = topLeft;
				bottomLeft = topLeft;
			}

		}

		@Override
		public String toString() {
			if (topRight == topLeft && bottomRight == topLeft && bottomLeft == topLeft) {
				if (topLeft == DEFAULT_VALUE) {
					return DEFAULT;
				} else {
					return "" + topLeft;
				}
			} else {
				return "" + topLeft + " " + topRight + " " + bottomRight + " " + bottomLeft;
			}
		}

		public boolean isComplex() {
			if (topLeft == topRight && topLeft == bottomRight && topLeft == bottomLeft) {
				return false;
			} else {
				return true;
			}
		}

		public static String from(int corners) {
			if (corners == DEFAULT_VALUE) {
				return DEFAULT;
			} else {
				return "" + corners;
			}
		}

		public static String from(int topLeft, int topRight, int bottomRight, int bottomLeft) {
			return new Corners.Float(topLeft, topRight, bottomRight, bottomLeft).toString();
		}

		@Override
		public boolean equals(Object object) {
			if (object instanceof Corners.Float) {
				Corners.Float corners = (Corners.Float) object;
				return corners.topLeft == topLeft && corners.topRight == topRight && corners.bottomRight == bottomRight && corners.bottomLeft == bottomLeft;
			} else {
				return super.equals(object);
			}
		}

	}

}
