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

import java.awt.Container;
import java.text.BreakIterator;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.GlyphView;
import javax.swing.text.LabelView;
import javax.swing.text.Segment;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;

public abstract class DefaultLabelView extends LabelView {

	// TODO: Default implementation has some issues with word wrapping, after inserting
	// and removing line break sometimes the returned minimum span is wrong causing
	// weird layout. For now we always update break spots
	private static final boolean ALWAYS_UPDATE_BREAKSPOTS = true;
	
	private int alignment = -1;
	private int[] breakSpots = null;

	public DefaultLabelView(Element elem) {
		super(elem);
	}

	public abstract int getTextAlignment();

	@Override
	public float getMinimumSpan(int axis) {
		// TODO: Default implementation has some issues with word wrapping, after inserting
		// and removing line break sometimes the returned minimum span is wrong causing
		// weird layout. This seems to fix it (value 0 is no problem for now because we don't
		// yet have layouts with dynamic text-box widths.
		return 0;
	}
	
	@Override
	public View breakView(int axis, int p0, float pos, float len) {

		if (axis == View.X_AXIS) {

			checkPainter();

			int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, len);
			int breakSpot = getBreakSpot(p0, p1);

			if (breakSpot != -1) {
				p1 = breakSpot;
			}

			// else, no break in the region, return a fragment of the
			// bounded region.
			if (p0 == getStartOffset() && p1 == getEndOffset()) {
				return this;
			}

			GlyphView v = (GlyphView) createFragment(p0, p1);
			// v.x = (int) pos;

			return v;

		}

		return this;

	}

	/**
	 * Returns a location to break at in the passed in region, or BreakIterator.DONE if there isn't a good location to break at in the specified region.
	 */
	private int getBreakSpot(int p0, int p1) {

		int alignment = getTextAlignment();
		if (breakSpots == null || this.alignment != alignment || ALWAYS_UPDATE_BREAKSPOTS) {

			// Re-calculate breakpoints for the whole view
			int start = getStartOffset();
			int end = getEndOffset();
			int[] bs = new int[end + 1 - start];
			int ix = 0;

			// Breaker should work on the parent element because there may be
			// a valid breakpoint at the end edge of the view (space, etc.)
			Element parent = getElement().getParentElement();
			int pstart = (parent == null ? start : parent.getStartOffset());
			int pend = (parent == null ? end : parent.getEndOffset());

			// Segment s = getText(pstart, pend);
			Segment s = getTextSegment(pstart, pend);
			s.first();
			BreakIterator breaker = getBreaker();
			breaker.setText(s);

			// Backward search should start from end+1 unless there's NO end+1
			int startFrom = end + (pend > end ? 1 : 0);
			for (;;) {
				startFrom = breaker.preceding(s.offset + (startFrom - pstart))
						+ (pstart - s.offset);
				if (startFrom > start) {
					// The break spot is within the view
					bs[ix++] = startFrom;
				} else {
					break;
				}
			}

			// SegmentCache.releaseSharedSegment(s);
			this.alignment = alignment;
			breakSpots = new int[ix];
			System.arraycopy(bs, 0, breakSpots, 0, ix);

		}

		int breakSpot = BreakIterator.DONE;
		for (int i = 0; i < breakSpots.length; i++) {
			int bsp = breakSpots[i];
			
			// Added to improve breaking behavior, this way we always return the first
			// available break-spot even if it is not inside the bounded position.
			// This looks better when the available space is very narrow.
			// Before sometimes the last letter of a word would be moved to the next line.
			breakSpot = bsp;
			
			if (bsp <= p1) {
				if (bsp > p0) {
					breakSpot = bsp;
				}
				break;
			}
		}

		return breakSpot;

	}

	public Segment getTextSegment(int p0, int p1) {
		Segment text = new Segment();
		try {
			Document doc = getDocument();
			doc.getText(p0, p1 - p0, text);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return text;
	}

	private BreakIterator getBreaker() {
		Document doc = getDocument();
		if ((doc != null) && Boolean.TRUE.equals(doc.getProperty("multiByte"))) { //$NON-NLS-1$
			Container c = getContainer();
			Locale locale = (c == null ? Locale.getDefault() : c.getLocale());
			return BreakIterator.getLineInstance(locale);
		} else {
			return new WhitespaceBasedBreakIterator(getTextAlignment() == StyleConstants.ALIGN_RIGHT);
		}
	}

	private static class WhitespaceBasedBreakIterator extends BreakIterator {

		private char[] text = new char[0];
		private int[] breaks = new int[] { 0 };
		private int pos = 0;
		private boolean breakBeforeSpace = false;

		public WhitespaceBasedBreakIterator(boolean breakBeforeSpace) {
			this.breakBeforeSpace = breakBeforeSpace;
		}

		/**
		 * Calculate break positions eagerly parallel to reading text.
		 */
		public void setText(CharacterIterator ci) {

			int begin = ci.getBeginIndex();
			text = new char[ci.getEndIndex() - begin];
			int[] breaks0 = new int[text.length + 1];
			int brIx = 0;
			breaks0[brIx++] = begin;

			int charIx = 0;
			boolean inWs = false;
			for (char c = ci.first(); c != CharacterIterator.DONE; c = ci.next()) {
				text[charIx] = c;
				boolean ws = Character.isWhitespace(c);
				if (inWs && !ws) {
					breaks0[brIx++] = charIx + begin - (breakBeforeSpace ? 1 : 0);
				}
				inWs = ws;
				charIx++;
			}
			if (text.length > 0) {
				breaks0[brIx++] = text.length + begin;
			}
			System.arraycopy(breaks0, 0, breaks = new int[brIx], 0, brIx);

		}

		public CharacterIterator getText() {
			return new StringCharacterIterator(new String(text));
		}

		public int first() {
			return breaks[pos = 0];
		}

		public int last() {
			return breaks[pos = breaks.length - 1];
		}

		public int current() {
			return breaks[pos];
		}

		public int next() {
			return (pos == breaks.length - 1 ? DONE : breaks[++pos]);
		}

		public int previous() {
			return (pos == 0 ? DONE : breaks[--pos]);
		}

		public int next(int n) {
			return checkhit(pos + n);
		}

		public int following(int n) {
			return adjacent(n, 1);
		}

		public int preceding(int n) {
			return adjacent(n, -1);
		}

		private int checkhit(int hit) {
			if ((hit < 0) || (hit >= breaks.length)) {
				return DONE;
			} else {
				return breaks[pos = hit];
			}
		}

		private int adjacent(int n, int bias) {
			int hit = Arrays.binarySearch(breaks, n);
			int offset = (hit < 0 ? (bias < 0 ? -1 : -2) : 0);
			return checkhit(Math.abs(hit) + bias + offset);
		}

	}

}
