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
package org.glasspath.aerialist.editor.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.glasspath.aerialist.Page;
import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.icons.Icons;
import org.glasspath.aerialist.swing.view.PageContainer;
import org.glasspath.aerialist.swing.view.PageView;

public class InsertPageAction extends AbstractAction {

	public static int INSERT = 0;
	public static int ABOVE = 1;
	public static int BELOW = 2;

	private final DocumentEditorPanel context;
	private final Page page;
	private final int option;

	public InsertPageAction(DocumentEditorPanel context, Page page, int option) {

		this.context = context;
		this.page = page;
		this.option = option;

		if (option == ABOVE) {
			putValue(Action.NAME, "Insert page above");
			putValue(Action.SHORT_DESCRIPTION, "Insert page above");
		} else if (option == BELOW) {
			putValue(Action.NAME, "Insert page below");
			putValue(Action.SHORT_DESCRIPTION, "Insert page below");
		} else {
			putValue(Action.NAME, "Insert page");
			putValue(Action.SHORT_DESCRIPTION, "Insert page");
		}

		putValue(Action.SMALL_ICON, Icons.fileDocumentPlus);

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		int index = context.getPageContainer().getPageViews().size();

		if (context.getSelection().size() == 1 && context.getSelection().get(0) instanceof PageView) {

			PageView selectedPageView = (PageView) context.getSelection().get(0);

			int selectedIndex = context.getPageContainer().getPageViews().indexOf(selectedPageView);
			if (selectedIndex >= 0) {

				if (option == ABOVE) {
					index = selectedIndex;
				} else if (option == BELOW) {
					index = selectedIndex + 1;
				}

			}

		}

		PageView pageView = PageContainer.createLayeredPageView(page, context.getPageContainer());

		context.getPageContainer().insertPageView(pageView, index);
		context.refresh(null, null, false, true);
		context.undoableEditHappened(new InsertPageUndoable(context, pageView, index));

	}

}
