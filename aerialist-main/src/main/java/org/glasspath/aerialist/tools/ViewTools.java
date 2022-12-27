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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.glasspath.aerialist.AbstractMainPanel;
import org.glasspath.aerialist.MainPanel;
import org.glasspath.common.swing.color.ColorUtils;

public class ViewTools {

	private final JMenu menu;
	private final JToolBar toolBar;
	private final JToolBar viewModeToolBar;

	private boolean updatingViewModeButtons = false;

	public ViewTools(AbstractMainPanel<?> context) {

		this.menu = new JMenu("View");
		this.toolBar = new JToolBar("View");
		this.viewModeToolBar = new JToolBar("View");

		toolBar.setRollover(true);
		toolBar.setBackground(ColorUtils.TITLE_BAR_COLOR);

		viewModeToolBar.setRollover(true);
		viewModeToolBar.setBackground(ColorUtils.TITLE_BAR_COLOR);

		JToggleButton designButton = new JToggleButton("Design");
		designButton.setSelected(true);
		viewModeToolBar.add(designButton);

		JToggleButton sourceButton = new JToggleButton("Source");
		viewModeToolBar.add(sourceButton);

		designButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (!updatingViewModeButtons) {
					updatingViewModeButtons = true;
					sourceButton.setSelected(!designButton.isSelected());
					context.setViewMode(MainPanel.VIEW_MODE_DESIGN);
					updatingViewModeButtons = false;
				}

			}
		});
		sourceButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (!updatingViewModeButtons) {
					updatingViewModeButtons = true;
					designButton.setSelected(!sourceButton.isSelected());
					context.setViewMode(MainPanel.VIEW_MODE_SOURCE);
					updatingViewModeButtons = false;
				}

			}
		});

	}

	public JMenu getMenu() {
		return menu;
	}

	public JToolBar getToolBar() {
		return toolBar;
	}

	public JToolBar getViewModeToolBar() {
		return viewModeToolBar;
	}

}
