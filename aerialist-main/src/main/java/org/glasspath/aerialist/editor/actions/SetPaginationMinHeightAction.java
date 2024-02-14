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

import org.glasspath.aerialist.IPagination;
import org.glasspath.aerialist.Pagination;
import org.glasspath.aerialist.editor.AbstractEditorPanel;

public class SetPaginationMinHeightAction extends AbstractAction {

	private final AbstractEditorPanel context;
	private final IPagination view;
	private final int minHeight;

	public SetPaginationMinHeightAction(AbstractEditorPanel context, IPagination view, int minHeight) {

		this.context = context;
		this.view = view;
		this.minHeight = minHeight;

		int currentMinHeight = 0;
		if (view.getPagination() != null) {
			currentMinHeight = view.getPagination().getMinHeight();
		}

		if (minHeight == 0) {
			putValue(Action.NAME, "Off");
		} else {
			putValue(Action.NAME, "" + minHeight); //$NON-NLS-1$
		}
		putValue(Action.SHORT_DESCRIPTION, "" + minHeight); //$NON-NLS-1$
		putValue(Action.SELECTED_KEY, minHeight == currentMinHeight);

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		Pagination oldPagination = view.getPagination();

		Pagination pagination = oldPagination != null ? new Pagination(oldPagination) : new Pagination();
		pagination.setMinHeight(minHeight);
		view.setPagination(pagination);

		context.undoableEditHappened(new SetPaginationUndoable(context, view, pagination, oldPagination));

	}

}
