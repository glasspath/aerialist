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

import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.glasspath.aerialist.HeightPolicy;
import org.glasspath.aerialist.YPolicy;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.Bounds;
import org.glasspath.aerialist.layout.VerticalLayout;

public class ElementContainer extends JPanel {

	protected final ISwingViewContext viewContext;
	private final VerticalLayout<ElementContainer, ISwingElementView<?>> layout;
	private boolean layoutInited = false;

	public ElementContainer(ISwingViewContext viewContext) {

		this.viewContext = viewContext;

		this.layout = new VerticalLayout<ElementContainer, ISwingElementView<?>>(viewContext, new LayoutMetrics()) {

			@Override
			public YPolicy getYPolicy(ISwingElementView<?> element) {
				return element.getYPolicy();
			}

			@Override
			public HeightPolicy getHeightPolicy(ISwingElementView<?> element) {
				return element.getHeightPolicy();
			}

			@Override
			public int getElementCount() {
				return getElementViewCount();
			}

			@Override
			public ISwingElementView<?> getElement(int i) {
				return getElementView(i);
			}

			@Override
			public Bounds getBounds(ISwingElementView<?> element) {
				return new Bounds(element.getX(), element.getY(), element.getWidth(), element.getHeight());
			}

			@Override
			public void setBounds(ISwingElementView<?> element, Bounds bounds) {
				((Component) element).setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
			}

			@Override
			public int getX(ISwingElementView<?> element) {
				return element.getX();
			}

			@Override
			public int getY(ISwingElementView<?> element) {
				return element.getY();
			}

			@Override
			public int getWidth(ISwingElementView<?> element) {
				return element.getWidth();
			}

			@Override
			public int getHeight(ISwingElementView<?> element) {
				return element.getHeight();
			}
		};

		setOpaque(false);

		setLayout(null);

		ISwingViewContext.installSelectionHandler(this, viewContext);

	}

	public int getElementViewCount() {
		return getComponentCount();
	}

	public ISwingElementView<?> getElementView(int i) {
		return (ISwingElementView<?>) getComponent(i);
	}

	public VerticalLayout<ElementContainer, ISwingElementView<?>>.AnchorList getAnchorList(ISwingElementView<?> element) {
		return layout.getAnchorList(element);
	}

	@Override
	public void doLayout() {

		super.doLayout();

		if (layoutInited) {
			layout.validateLayout();
		}

		// TODO: Initialize vertical anchors from outside after loading document?
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				if (!layoutInited && isValid()) {

					updateVerticalAnchors();
					layout.validateLayout();

					layoutInited = true;

				}

				repaint();

			}
		});

	}

	public void invalidate(HeightPolicy heightPolicy) {

		ISwingElementView<?> elementView;
		for (int i = 0; i < getElementViewCount(); i++) {

			elementView = getElementView(i);
			if (elementView instanceof Component) {

				Component component = (Component) elementView;

				if (elementView.getHeightPolicy() == heightPolicy) {
					component.invalidate();
				}

				if (elementView instanceof ElementContainer) {
					((ElementContainer) elementView).invalidate(heightPolicy);
				}

			}

		}

	}

	public void elementResized(Component component, Rectangle previousBounds) {

		if (layoutInited && component instanceof ISwingElementView<?>) {
			layout.updateLayout((ISwingElementView<?>) component);
		}

		updateVerticalAnchors();

	}

	public void elementMoved(Component component, Rectangle previousBounds) {
		elementMoved(component, previousBounds, true);
	}

	public void elementMoved(Component component, Rectangle previousBounds, boolean updateVerticalAnchors) {

		if (layoutInited && component instanceof ISwingElementView<?>) {
			layout.updateLayout((ISwingElementView<?>) component);
		}

		if (updateVerticalAnchors) {
			updateVerticalAnchors();
		}

	}

	public boolean isUpdatingVerticalLayout() {
		return layout.isUpdatingLayout();
	}

	public void updateVerticalAnchors() {
		layout.updateVerticalAnchors();
	}

}
