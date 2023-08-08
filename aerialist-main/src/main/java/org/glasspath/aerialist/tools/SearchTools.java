/*
 * This file is part of Glasspath Revenue.
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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.glasspath.aerialist.Aerialist;
import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.swing.view.PageContainer;
import org.glasspath.common.os.OsUtils;
import org.glasspath.common.swing.search.SearchField;
import org.glasspath.common.swing.tools.AbstractTools;

public class SearchTools extends AbstractTools<Aerialist> {

	public SearchTools(Aerialist context) {
		super(context, "Search");

		SearchField searchField = context.getGlassPane().getSearchField();
		DocumentEditorPanel editor = context.getMainPanel().getDocumentEditor();

		JMenuItem searchMenuItem = new JMenuItem("Search");
		searchMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, OsUtils.CTRL_OR_CMD_MASK));
		searchMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {

				// TODO: Restore page mode
				if (editor.getPageMode() == PageContainer.PAGE_MODE_SINGLE) {
					context.getViewTools().setPageMode(PageContainer.PAGE_MODE_MULTIPLE);
				}

				context.getGlassPane().showSearchField();

			}
		});
		menu.add(searchMenuItem);

		JMenuItem clearSearchMenuItem = new JMenuItem("Clear search");
		clearSearchMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, OsUtils.CTRL_OR_CMD_MASK | OsUtils.SHIFT_MASK));
		clearSearchMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				context.getGlassPane().hideSearchField();
			}
		});
		menu.add(clearSearchMenuItem);

		menu.addSeparator();

		JMenuItem searchNextMenuItem = new JMenuItem("Search next");
		searchNextMenuItem.setEnabled(false);
		searchNextMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		searchNextMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				searchField.searchNext();
			}
		});
		menu.add(searchNextMenuItem);

		JMenuItem searchPreviousMenuItem = new JMenuItem("Search previous");
		searchPreviousMenuItem.setEnabled(false);
		searchPreviousMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.SHIFT_DOWN_MASK));
		searchPreviousMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				searchField.searchPrevious();
			}
		});
		menu.add(searchPreviousMenuItem);

		searchField.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentHidden(ComponentEvent e) {
				editor.cancelSearch();
				searchNextMenuItem.setEnabled(false);
				searchPreviousMenuItem.setEnabled(false);
			}

			@Override
			public void componentShown(ComponentEvent e) {
				searchNextMenuItem.setEnabled(true);
				searchPreviousMenuItem.setEnabled(true);
			}
		});

		searchField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				editor.cancelSearch();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				editor.cancelSearch();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				editor.cancelSearch();
			}
		});

	}

}
