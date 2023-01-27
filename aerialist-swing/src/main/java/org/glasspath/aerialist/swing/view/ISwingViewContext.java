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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.undo.UndoableEdit;

import org.glasspath.aerialist.layout.ILayoutContext;

public interface ISwingViewContext extends ILayoutContext<BufferedImage> {

	public boolean isRightMouseSelectionAllowed();

	public void focusGained(JComponent component);

	public void undoableEditHappened(UndoableEdit edit);

	public void refresh(Component component);

	public Color getDefaultForeground();

	public static void installSelectionHandler(JComponent component, ISwingViewContext viewContext) {

		component.setFocusable(true);

		component.addFocusListener(new FocusAdapter() {

			@Override
			public void focusGained(FocusEvent e) {
				viewContext.focusGained(component);
			}
		});

		component.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) || (SwingUtilities.isRightMouseButton(e) && viewContext.isRightMouseSelectionAllowed())) {
					component.requestFocusInWindow();
				}
			}
		});

	}

}
