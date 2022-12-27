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
package org.glasspath.aerialist.tools;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import org.glasspath.common.swing.color.ColorUtils;

public class EditTools {

	private final JMenu menu;
	private final JToolBar toolBar;

	private final JMenuItem undoMenuItem;
	private final JMenuItem redoMenuItem;

	private final JButton undoButton;
	private final JButton redoButton;

	public EditTools(UndoActions undoActions) {

		this.menu = new JMenu("Edit");
		this.toolBar = new JToolBar("Edit");
		toolBar.setRollover(true);
		toolBar.setBackground(ColorUtils.TITLE_BAR_COLOR);

		this.undoMenuItem = new JMenuItem(undoActions.getUndoAction());
		menu.add(undoMenuItem);

		this.redoMenuItem = new JMenuItem(undoActions.getRedoAction());
		menu.add(redoMenuItem);

		this.undoButton = new JButton(undoActions.getUndoAction());
		undoButton.setHideActionText(true);
		toolBar.add(undoButton);

		this.redoButton = new JButton(undoActions.getRedoAction());
		redoButton.setHideActionText(true);
		toolBar.add(redoButton);

	}

	public JMenu getMenu() {
		return menu;
	}

	public JMenu prepareMenu() {

		menu.removeAll();

		menu.add(undoMenuItem);
		menu.add(redoMenuItem);

		menu.addSeparator();

		return menu;

	}

	public void finishMenu() {
		if (menu.getPopupMenu().getComponentCount() > 0 && menu.getPopupMenu().getComponent(menu.getPopupMenu().getComponentCount() - 1) instanceof JPopupMenu.Separator) {
			menu.getPopupMenu().remove(menu.getPopupMenu().getComponentCount() - 1);
		}
	}

	public JToolBar getToolBar() {
		return toolBar;
	}

}
