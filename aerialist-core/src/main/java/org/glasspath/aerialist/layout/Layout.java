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

import org.glasspath.aerialist.layout.DocumentLayoutInfo.Bounds;

public abstract class Layout<C, E> {

	protected final ILayoutContext<?> layoutContext;
	protected final ILayoutMetrics<E> layoutMetrics;
	protected C container = null;
	private boolean updatingLayout = false;

	public Layout(ILayoutContext<?> layoutContext, ILayoutMetrics<E> layoutMetrics) {
		this(layoutContext, layoutMetrics, null);
	}

	public Layout(ILayoutContext<?> layoutContext, ILayoutMetrics<E> layoutMetrics, C container) {
		this.layoutContext = layoutContext;
		this.layoutMetrics = layoutMetrics;
		this.container = container;
	}

	public ILayoutContext<?> getLayoutContext() {
		return layoutContext;
	}

	public ILayoutMetrics<E> getLayoutMetrics() {
		return layoutMetrics;
	}

	public C getContainer() {
		return container;
	}

	public void setContainer(C container) {
		this.container = container;
	}

	public boolean isUpdatingLayout() {
		return updatingLayout;
	}

	protected void setUpdatingLayout(boolean updatingLayout) {
		this.updatingLayout = updatingLayout;
	}

	public abstract void validateLayout();

	public abstract void updateLayout(E fromElement);

	public abstract int getElementCount();

	public abstract E getElement(int i);

	public abstract Bounds getBounds(E element);

	public abstract void setBounds(E element, Bounds bounds);

	public abstract int getX(E element);

	public abstract int getY(E element);

	public abstract int getWidth(E element);

	public abstract int getHeight(E element);

}
