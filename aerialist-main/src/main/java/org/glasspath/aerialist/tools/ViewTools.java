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
import java.util.prefs.Preferences;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.glasspath.aerialist.Aerialist;
import org.glasspath.aerialist.MainPanel;
import org.glasspath.aerialist.swing.view.PageContainer;
import org.glasspath.common.os.preferences.BoolPref;
import org.glasspath.common.swing.color.ColorUtils;
import org.glasspath.common.swing.tools.AbstractTools;

public class ViewTools extends AbstractTools<Aerialist> {

	public static final BoolPref FILE_TOOL_BAR_VISIBLE = new BoolPref("fileToolBarVisible", true); //$NON-NLS-1$
	public static final BoolPref EDIT_TOOL_BAR_VISIBLE = new BoolPref("editToolBarVisible", true); //$NON-NLS-1$
	public static final BoolPref TEXT_FORMAT_TOOL_BAR_VISIBLE = new BoolPref("textFormatToolBarVisible", true); //$NON-NLS-1$
	public static final BoolPref OBJECT_FORMAT_TOOL_BAR_VISIBLE = new BoolPref("objectFormatToolBarVisible", true); //$NON-NLS-1$
	public static final BoolPref STATUS_BAR_VISIBLE = new BoolPref("statusBarVisible", true); //$NON-NLS-1$

	private final JCheckBoxMenuItem designMenuItem;
	private final JCheckBoxMenuItem sourceMenuItem;
	private final JCheckBoxMenuItem singlePageLayoutMenuItem;
	private final JCheckBoxMenuItem multiplePagesLayoutMenuItem;
	private final JCheckBoxMenuItem objectFormatToolsMenuItem;
	private final JToolBar viewModeToolBar;
	private final JToggleButton designButton;
	private final JToggleButton sourceButton;

	private boolean updatingViewModeComponents = false;
	private boolean updatingPageModeComponents = false;

	public ViewTools(Aerialist context) {
		super(context, "View");

		Preferences preferences = context.getPreferences();

		designMenuItem = new JCheckBoxMenuItem("Design");
		menu.add(designMenuItem);
		designMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setViewMode(MainPanel.VIEW_MODE_DESIGN);
			}
		});

		sourceMenuItem = new JCheckBoxMenuItem("Source");
		menu.add(sourceMenuItem);
		sourceMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setViewMode(MainPanel.VIEW_MODE_SOURCE);
			}
		});

		menu.addSeparator();

		JMenu layoutMenu = new JMenu("Layout");
		menu.add(layoutMenu);

		singlePageLayoutMenuItem = new JCheckBoxMenuItem("Single page");
		layoutMenu.add(singlePageLayoutMenuItem);
		singlePageLayoutMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				setPageMode(PageContainer.PAGE_MODE_SINGLE);
			}
		});

		multiplePagesLayoutMenuItem = new JCheckBoxMenuItem("Multiple pages");
		layoutMenu.add(multiplePagesLayoutMenuItem);
		multiplePagesLayoutMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				setPageMode(PageContainer.PAGE_MODE_MULTIPLE);
			}
		});

		menu.addSeparator();

		JMenu toolBarsMenu = new JMenu("Tools");
		menu.add(toolBarsMenu);

		JCheckBoxMenuItem fileToolsMenuItem = new JCheckBoxMenuItem("File tools");
		toolBarsMenu.add(fileToolsMenuItem);
		fileToolsMenuItem.setSelected(FILE_TOOL_BAR_VISIBLE.get(preferences));
		fileToolsMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				context.getFileTools().setToolBarVisible(fileToolsMenuItem.isSelected());
				FILE_TOOL_BAR_VISIBLE.put(preferences, fileToolsMenuItem.isSelected());
			}
		});

		JCheckBoxMenuItem editToolsMenuItem = new JCheckBoxMenuItem("Edit tools");
		toolBarsMenu.add(editToolsMenuItem);
		editToolsMenuItem.setSelected(EDIT_TOOL_BAR_VISIBLE.get(preferences));
		editToolsMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				context.getEditTools().setToolBarVisible(editToolsMenuItem.isSelected());
				EDIT_TOOL_BAR_VISIBLE.put(preferences, editToolsMenuItem.isSelected());
			}
		});

		JCheckBoxMenuItem textFormatToolsMenuItem = new JCheckBoxMenuItem("Text format tools");
		toolBarsMenu.add(textFormatToolsMenuItem);
		textFormatToolsMenuItem.setSelected(TEXT_FORMAT_TOOL_BAR_VISIBLE.get(preferences));
		textFormatToolsMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				context.getTextFormatTools().setToolBarVisible(textFormatToolsMenuItem.isSelected());
				TEXT_FORMAT_TOOL_BAR_VISIBLE.put(preferences, textFormatToolsMenuItem.isSelected());
			}
		});

		objectFormatToolsMenuItem = new JCheckBoxMenuItem("Object format tools"); // TODO: Use better name?
		toolBarsMenu.add(objectFormatToolsMenuItem);
		objectFormatToolsMenuItem.setSelected(OBJECT_FORMAT_TOOL_BAR_VISIBLE.get(preferences));
		objectFormatToolsMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				context.getObjectFormatTools().setToolBarVisible(objectFormatToolsMenuItem.isSelected());
				OBJECT_FORMAT_TOOL_BAR_VISIBLE.put(preferences, objectFormatToolsMenuItem.isSelected());
			}
		});

		JCheckBoxMenuItem statusBarMenuItem = new JCheckBoxMenuItem("Status bar");
		menu.add(statusBarMenuItem);
		statusBarMenuItem.setSelected(STATUS_BAR_VISIBLE.get(preferences));
		statusBarMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				context.setStatusBarVisible(statusBarMenuItem.isSelected());
				STATUS_BAR_VISIBLE.put(preferences, statusBarMenuItem.isSelected());
			}
		});

		viewModeToolBar = new JToolBar("View");
		viewModeToolBar.setRollover(true);
		viewModeToolBar.setBackground(ColorUtils.TITLE_BAR_COLOR);

		designButton = new JToggleButton("Design");
		viewModeToolBar.add(designButton);
		designButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setViewMode(MainPanel.VIEW_MODE_DESIGN);
			}
		});

		sourceButton = new JToggleButton("Source");
		viewModeToolBar.add(sourceButton);
		sourceButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setViewMode(MainPanel.VIEW_MODE_SOURCE);
			}
		});

		updateViewModeComponents();
		updatePageModeComponents();

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				if (!fileToolsMenuItem.isSelected()) {
					context.getFileTools().setToolBarVisible(false);
				}
				if (!editToolsMenuItem.isSelected()) {
					context.getEditTools().setToolBarVisible(false);
				}
				if (!textFormatToolsMenuItem.isSelected()) {
					context.getTextFormatTools().setToolBarVisible(false);
				}
				if (!objectFormatToolsMenuItem.isSelected()) {
					context.getObjectFormatTools().setToolBarVisible(false);
				}
				if (!statusBarMenuItem.isSelected()) {
					context.setStatusBarVisible(false);
				}

			}
		});

	}

	public JToolBar getViewModeToolBar() {
		return viewModeToolBar;
	}

	private void setViewMode(int viewMode) {
		if (!updatingViewModeComponents) {
			context.getMainPanel().setViewMode(viewMode);
			updateViewModeComponents();
		}
	}

	private void updateViewModeComponents() {

		updatingViewModeComponents = true;

		boolean viewModeSource = context.getMainPanel().getViewMode() == MainPanel.VIEW_MODE_SOURCE;

		designMenuItem.setSelected(!viewModeSource);
		sourceMenuItem.setSelected(viewModeSource);

		objectFormatToolsMenuItem.setVisible(!viewModeSource);

		designButton.setSelected(!viewModeSource);
		sourceButton.setSelected(viewModeSource);

		updatingViewModeComponents = false;

	}

	public void setPageMode(int pageMode) {

		if (!updatingPageModeComponents) {

			// TODO: Support searching in single page mode..
			if (pageMode == PageContainer.PAGE_MODE_SINGLE) {
				context.getGlassPane().hideSearchField();
			}

			context.getMainPanel().getDocumentEditor().setPageMode(pageMode);

			updatePageModeComponents();

		}

	}

	private void updatePageModeComponents() {

		updatingPageModeComponents = true;

		singlePageLayoutMenuItem.setSelected(context.getMainPanel().getDocumentEditor().getPageMode() == PageContainer.PAGE_MODE_SINGLE);
		multiplePagesLayoutMenuItem.setSelected(context.getMainPanel().getDocumentEditor().getPageMode() == PageContainer.PAGE_MODE_MULTIPLE);

		updatingPageModeComponents = false;

	}

}
