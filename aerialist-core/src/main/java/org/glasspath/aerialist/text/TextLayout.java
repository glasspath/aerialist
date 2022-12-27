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
package org.glasspath.aerialist.text;

import org.glasspath.aerialist.TextStyle;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.Rect;

public class TextLayout {

	public final Line[] lines;
	public final float preferredWidth;
	public final float preferredHeight;

	public TextLayout(Line[] lines, float preferredWidth, float preferredHeight) {
		this.lines = lines;
		this.preferredWidth = preferredWidth;
		this.preferredHeight = preferredHeight;
	}

	public static class Line {

		public final int start;
		public final Span[] spans;
		public final float baseline;
		public final Rect lineBounds;

		public Line(int start, Span[] spans, float baseline, Rect lineBounds) {
			this.start = start;
			this.spans = spans;
			this.baseline = baseline;
			this.lineBounds = lineBounds;
		}

	}

	public static class Span {

		public final TextStyle style;
		public float x = 0;
		public int fontIndex = 0;
		public int start = 0;
		public int end = 0;

		public Span(TextStyle style) {
			this.style = style;
		}

	}

}
