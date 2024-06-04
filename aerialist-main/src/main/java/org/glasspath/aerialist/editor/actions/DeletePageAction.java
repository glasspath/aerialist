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

import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.swing.view.PageView;
import org.glasspath.common.icons.Icons;
import org.glasspath.common.swing.resources.CommonResources;

public class DeletePageAction extends AbstractAction {

	private final DocumentEditorPanel context;
	private final PageView pageView;

	public DeletePageAction(DocumentEditorPanel context, PageView pageView) {

		this.context = context;
		this.pageView = pageView;

		putValue(Action.NAME, CommonResources.getString("Delete")); //$NON-NLS-1$
		putValue(Action.SHORT_DESCRIPTION, CommonResources.getString("Delete")); //$NON-NLS-1$
		putValue(Action.SMALL_ICON, Icons.closeRed);

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		int index = context.getPageContainer().getPageViews().indexOf(pageView);
		if (index >= 0) {

			context.getPageContainer().removePageView(pageView);

			context.undoableEditHappened(new DeletePageUndoable(context, pageView, index));

			context.deselectAll();
			context.refresh(null);

		}

	}

}
