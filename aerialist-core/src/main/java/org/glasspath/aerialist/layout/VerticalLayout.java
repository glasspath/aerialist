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
package org.glasspath.aerialist.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.glasspath.aerialist.HeightPolicy;
import org.glasspath.aerialist.YPolicy;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.Bounds;

public abstract class VerticalLayout<C, E> extends Layout<C, E> {

	private final Map<E, AnchorList> anchorLists = new HashMap<>();

	public VerticalLayout(ILayoutContext<?> layoutContext, ILayoutMetrics<E> metrics) {
		super(layoutContext, metrics);
	}

	public VerticalLayout(ILayoutContext<?> layoutContext, ILayoutMetrics<E> metrics, C container) {
		super(layoutContext, metrics, container);
	}

	@Override
	public void setContainer(C container) {
		setContainer(container, true);
	}

	public void setContainer(C container, boolean clearAnchorLists) {
		super.setContainer(container);
		if (clearAnchorLists) {
			anchorLists.clear();
		}
	}

	public abstract YPolicy getYPolicy(E element);

	public abstract HeightPolicy getHeightPolicy(E element);

	public AnchorList getAnchorList(E element) {

		AnchorList anchorList = anchorLists.get(element);
		if (anchorList == null) {
			anchorList = new AnchorList();
			anchorLists.put(element, anchorList);
		}

		return anchorList;

	}

	@Override
	public void validateLayout() {

		if (layoutContext.isHeightPolicyEnabled()) {

			for (int i = 0; i < getElementCount(); i++) {

				E element = getElement(i);
				if (getHeightPolicy(element) == HeightPolicy.AUTO) {

					Bounds oldBounds = getBounds(element);

					int height = layoutMetrics.getPreferredHeight(element);

					// TODO?
					if (height < 10) {
						height = 10;
					}

					if (height != oldBounds.height) {

						Bounds bounds = new Bounds(oldBounds);
						bounds.height = height;

						setBounds(element, bounds);

						updateLayout(element);
						updateVerticalAnchors();

						/* TODO?
						if (getParent() instanceof ElementContainer) {
							getParent().invalidate();
							getParent().validate();
						}
						*/

					}

				}

			}

		}

	}

	public void updateVerticalAnchors() {

		anchorLists.clear();

		for (int i = 0; i < getElementCount(); i++) {
			updateAnchors(getElement(i));
		}

	}

	/**
	 * Replaces anchor element (key) by new element (value), all other anchors are removed.
	 * 
	 * @param elementMap map of elements to be replaced
	 */
	public void replaceAnchors(Map<E, E> elementMap) {

		for (Entry<E, AnchorList> entry : anchorLists.entrySet()) {

			AnchorList anchorList = entry.getValue();

			if (anchorList.anchors != null) {

				List<E> anchors = new ArrayList<>();

				for (E element : anchorList.anchors) {

					E newElement = elementMap.get(element);
					if (newElement != null) {
						anchors.add(newElement);
					}

				}

				anchorList.anchors = anchors;

			}

		}

	}

	private void updateAnchors(E elementView) {

		AnchorList anchorList = getAnchorList(elementView);

		if (getYPolicy(elementView) == YPolicy.DEFAULT) {

			List<E> anchors = new ArrayList<>();
			int margin = Integer.MAX_VALUE;

			E ev;
			int evMargin;
			for (int i = 0; i < getElementCount(); i++) {

				ev = getElement(i);
				if (ev != elementView) {

					evMargin = getY(elementView) - (getY(ev) + getHeight(ev));

					if (getHeightPolicy(ev) == HeightPolicy.AUTO && evMargin >= 0) {

						anchors.add(ev);

						if (evMargin < margin) {
							margin = evMargin;
						}

					}

				}

			}

			anchorList.anchors = anchors;
			anchorList.margin = margin;

		} else {
			anchorList.anchors = null;
		}

	}

	public List<E> getAnchoredElements(E fromElement) {

		List<E> elements = new ArrayList<>();

		AnchorList anchorList;
		for (int i = 0; i < getElementCount(); i++) {

			E element = getElement(i);
			anchorList = getAnchorList(element);

			if (getYPolicy(element) == YPolicy.DEFAULT && anchorList.isAnchor(fromElement)) {
				elements.add(element);
			}

		}

		return elements;

	}

	public void updateLayout(E fromElement) {

		if (layoutContext.isYPolicyEnabled() && !isUpdatingLayout()) {

			setUpdatingLayout(true);

			if (getHeightPolicy(fromElement) == HeightPolicy.AUTO) {

				List<E> elements = getAnchoredElements(fromElement);

				if (elements.size() > 0) {

					Collections.sort(elements, new Comparator<E>() {

						@Override
						public int compare(E e1, E e2) {
							return Integer.compare(getY(e1), getY(e2));
						}
					});

					for (E element : elements) {

						AnchorList anchorList = getAnchorList(element);

						int margin = anchorList.getCurrentMargin(getY(element));
						if (margin != anchorList.margin) {

							Bounds bounds = getBounds(element);
							bounds.y += (anchorList.margin - margin);

							setBounds(element, bounds);

						}

					}

				}

			}

		}

		setUpdatingLayout(false);

	}

	public class AnchorList {

		public List<E> anchors = null;
		public int margin = 0;

		public AnchorList() {

		}

		public boolean isAnchor(E element) {
			return anchors != null && anchors.contains(element);
		}

		public int getCurrentMargin(int y) {

			int margin = Integer.MAX_VALUE;

			if (anchors != null) {

				for (E anchor : anchors) {

					int anchorMargin = y - (getY(anchor) + getHeight(anchor));
					if (anchorMargin < margin) {
						margin = anchorMargin;
					}

				}

			}

			return margin;

		}

	}

}
