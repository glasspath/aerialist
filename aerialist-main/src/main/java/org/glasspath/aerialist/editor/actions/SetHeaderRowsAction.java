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

import org.glasspath.aerialist.editor.AbstractEditorPanel;
import org.glasspath.aerialist.swing.view.TableView;

public class SetHeaderRowsAction extends AbstractAction {

	private final AbstractEditorPanel context;
	private final TableView tableView;
	private final int headerRowCount;

	public SetHeaderRowsAction(AbstractEditorPanel context, TableView tableView, int headerRowCount) {

		this.context = context;
		this.tableView = tableView;
		this.headerRowCount = headerRowCount;

		String description;
		if (headerRowCount == 0) {
			description = "No header";
		} else if (headerRowCount == 1) {
			description = "1 row";
		} else {
			description = headerRowCount + " rows";
		}

		putValue(Action.NAME, description);
		putValue(Action.SHORT_DESCRIPTION, description);
		putValue(Action.SELECTED_KEY, tableView.getHeaderRows() == headerRowCount);

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		int oldHeaderRowCount = tableView.getHeaderRows();

		tableView.setHeaderRows(headerRowCount);
		context.refresh(tableView);

		context.undoableEditHappened(new SetHeaderRowsUndoable(context, tableView, headerRowCount, oldHeaderRowCount));

	}

}
