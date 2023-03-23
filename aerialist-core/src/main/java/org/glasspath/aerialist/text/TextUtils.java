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

import java.util.ArrayList;
import java.util.List;

import org.glasspath.aerialist.Alignment;
import org.glasspath.aerialist.IText;
import org.glasspath.aerialist.TextStyle;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.Rect;
import org.glasspath.aerialist.text.TextLayout.Line;
import org.glasspath.aerialist.text.TextLayout.Span;
import org.glasspath.aerialist.text.font.FontCache;

public class TextUtils {

	public static boolean MATCH_AWT = true;
	
	private TextUtils() {

	}

	public static TextLayout createTextLayout(IText iText, FontCache<?> fontCache, float width) {

		if (iText.getStyles().size() > 0) {

			List<Line> lines = new ArrayList<>();

			float y = 0.0F;
			float w = 0.0F;
			Alignment alignment = Alignment.get(iText.getAlignment());

			LineLayout lineLayout = new LineLayout(iText, fontCache);

			for (int i = 0; i < iText.getStyles().size(); i++) {

				if (lineLayout.addSpan(i) || i == iText.getStyles().size() - 1) {

					LineLayout wrappedLineLayout = lineLayout.limitWidth(width, alignment);

					y = lineLayout.layout(y, width, alignment);
					lines.add(new Line(lineLayout.spans.get(0).start, lineLayout.spans.toArray(new Span[0]), lineLayout.baseline, lineLayout.bounds));
					if (lineLayout.x > w) {
						w = lineLayout.x;
					}

					while (wrappedLineLayout != null) {

						lineLayout = wrappedLineLayout;

						wrappedLineLayout = lineLayout.limitWidth(width, alignment);

						y = lineLayout.layout(y, width, alignment);
						lines.add(new Line(lineLayout.spans.get(0).start, lineLayout.spans.toArray(new Span[0]), lineLayout.baseline, lineLayout.bounds));
						if (lineLayout.x > w) {
							w = lineLayout.x;
						}

					}

					if (i < iText.getStyles().size() - 1) {
						lineLayout = new LineLayout(iText, fontCache);
					}

				}

			}

			return new TextLayout(lines.toArray(new Line[0]), w, y); // TODO

		} else {
			return new TextLayout(new Line[0], 0.0F, 10.0F); // TODO: Calculate height of empty string with default font
		}

	}

	protected static class LineLayout {

		private final IText iText;
		private final FontCache<?> fontCache;

		private List<Span> spans = new ArrayList<>();
		private List<SpanInfo> infos = new ArrayList<>();
		private float x = 0.0F;
		private int newLineIndex = -1;

		private float baseline = 0.0F;
		private Rect bounds = new Rect();

		public LineLayout(IText iText, FontCache<?> fontCache) {
			this.iText = iText;
			this.fontCache = fontCache;
		}

		protected boolean addSpan(int styleIndex) {

			TextStyle style = iText.getStyles().get(styleIndex);
			String text = iText.getText().substring(style.start, style.end);
			SpanInfo info = fontCache.getSpanInfo(text, style.font != null ? style.font : TextStyle.DEFAULT_FONT, style.fontSize, style.bold, style.italic);

			Span span = new Span(style);
			span.fontIndex = info.fontIndex;
			span.start = style.start;
			span.end = style.end;
			span.x = x;
			spans.add(span);
			infos.add(info);

			x += info.width;

			newLineIndex = text.indexOf("\n"); //$NON-NLS-1$

			// TODO: Is this possible?
			if (newLineIndex >= 0 && newLineIndex != text.length() - 1) {
				System.err.println("TextLayout: newLineIndex != text.length() - 1");
			}

			return newLineIndex >= 0;

		}

		protected LineLayout limitWidth(float width, Alignment alignment) {

			LineLayout newLine = null;

			float x = this.x;
			if (x >= width) {

				outerLoop: for (int i = spans.size() - 1; i >= 0; i--) {

					Span span = spans.get(i);

					String text = iText.getText().substring(span.start, span.end);

					x = span.x;

					int spaceIndex = text.lastIndexOf(" "); //$NON-NLS-1$
					while (spaceIndex >= 0) {

						int offset = alignment == Alignment.RIGHT ? 0 : 1;
						
						String s = text.substring(0, spaceIndex + offset);
						TextStyle style = span.style;
						SpanInfo info = fontCache.getSpanInfo(s, style.font != null ? style.font : TextStyle.DEFAULT_FONT, style.fontSize, style.bold, style.italic);

						if (x + info.width <= width) {

							newLine = new LineLayout(iText, fontCache);

							Span newSpan2 = new Span(style);
							newSpan2.fontIndex = info.fontIndex;
							newSpan2.start = span.start + spaceIndex + offset;
							newSpan2.end = span.end;
							newSpan2.x = newLine.x;

							// Update the remaining span after creating the new span
							span.end = span.start + spaceIndex + offset;
							this.x = x + info.width;

							// Update lists of current line
							spans = spans.subList(0, i + 1);
							infos = infos.subList(0, i);
							infos.add(info);

							text = iText.getText();

							s = text.substring(newSpan2.start, newSpan2.end);
							info = fontCache.getSpanInfo(s, style.font != null ? style.font : TextStyle.DEFAULT_FONT, style.fontSize, style.bold, style.italic);
							newLine.x += info.width;

							newLine.spans.add(newSpan2);
							newLine.infos.add(info);

							// Move the right-side spans to the new line
							for (int j = i + 1; j < spans.size(); j++) {

								span = spans.get(j);
								span.x = newLine.x;

								s = text.substring(span.start, span.end);
								info = fontCache.getSpanInfo(s, style.font != null ? style.font : TextStyle.DEFAULT_FONT, style.fontSize, style.bold, style.italic);
								newLine.x += info.width;

								newLine.spans.add(span);
								newLine.infos.add(info);

							}

							break outerLoop;

						}

						spaceIndex = text.lastIndexOf(" ", spaceIndex - 1); //$NON-NLS-1$

					}

				}

			}

			return newLine;

		}

		protected float layout(float y, float width, Alignment alignment) {

			float maxTop = 0.0F;
			float maxDescent = 0.0F;

			for (int i = 0; i < spans.size(); i++) {

				Span span = spans.get(i);
				SpanInfo info = infos.get(i);

				// float top = span.style.fontSize + span.style.spaceAbove;
				float top = info.ascent + span.style.spaceAbove;
				if (MATCH_AWT) {
					top = (float) Math.ceil(top);
				}
				if (top > maxTop) {
					maxTop = top;
				}

				float descent = info.descent;
				if (MATCH_AWT) {
					descent = (float) Math.ceil(descent);
				}
				if (descent > maxDescent) {
					maxDescent = descent;
				}

			}

			float xOffset = 0;
			if (alignment == Alignment.RIGHT) {
				xOffset = width - x;
			} else if (alignment == Alignment.CENTER) {
				xOffset = (width - x) / 2;
			}

			for (Span span : spans) {
				span.x += xOffset;
			}

			// baseline = y + maxTop + maxLeading;
			baseline = y + maxTop;

			bounds.x = spans.get(0).x;
			bounds.y = baseline - maxTop;
			bounds.width = (spans.get(spans.size() - 1).x + infos.get(spans.size() - 1).width) - bounds.x;
			bounds.height = maxTop + maxDescent;

			return baseline + maxDescent;

		}

	}

	public static class SpanInfo {

		public int fontIndex = 0;
		public float width = 0.0f;
		public float ascent = 0.0f;
		public float descent = 0.0f;
		public float height = 0.0f;

		public SpanInfo() {

		}

	}

}
