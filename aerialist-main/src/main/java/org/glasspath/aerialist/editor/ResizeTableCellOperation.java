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
package org.glasspath.aerialist.editor;

import java.awt.Point;
import java.awt.event.MouseEvent;

import org.glasspath.aerialist.editor.ChangeTableLayoutUndoable.TableViewData;
import org.glasspath.aerialist.swing.view.PageView;
import org.glasspath.aerialist.swing.view.TableCellView;
import org.glasspath.aerialist.swing.view.TableView;
import org.glasspath.aerialist.swing.view.TableView.TableViewLayout;

public class ResizeTableCellOperation extends Operation {

	private final DocumentEditorPanel context;
	private final TableCellView tableCellView;

	private boolean started = false;
	private boolean done = false;

	private TableView tableView = null;
	private TableViewLayout tableLayout = null;
	private TableViewData oldTableViewData = null;
	private PageView pageView = null;
	private int column = -1;

	public ResizeTableCellOperation(DocumentEditorPanel context, TableCellView tableCellView) {
		this.context = context;
		this.tableCellView = tableCellView;
	}

	@Override
	public void mousePressed(MouseEvent e, Point p) {

		if (p != null && tableCellView.getParent() instanceof TableView) {

			tableView = (TableView) tableCellView.getParent();
			tableLayout = tableView.getTableLayout();
			oldTableViewData = new TableViewData(tableView.updateColStyles(), null);

			if (tableView.getParent() instanceof PageView && tableLayout != null) {

				pageView = (PageView) tableView.getParent();

				column = (tableCellView.getCol() - 1) + (tableCellView.getColSpan() - 1);
				if (column >= 0 && column < tableLayout.getNumColumn() - 1) {

					e.consume();

					started = true;

				}

			}

		}

	}

	@Override
	public void mouseDragged(MouseEvent e, Point p) {

		if (started && p != null) {

			Point point = context.convertPointToPage(p, pageView, true);
			point.translate(-tableView.getX(), -tableView.getY());
			tableLayout.setColumnOffset(column + 1, point.x);

			tableView.invalidate();
			tableView.validate();

			e.consume();

			context.refresh(pageView);

		}

	}

	@Override
	public void mouseReleased(MouseEvent e, Point p) {

		if (started) {

			TableViewData newTableViewData = new TableViewData(tableView.updateColStyles(), null);

			context.undoableEditHappened(new ChangeTableLayoutUndoable(tableView, oldTableViewData, newTableViewData));

			context.getSelection().fireSelectionChanged();
			context.refresh(pageView);

		}

		e.consume();

		done = true;

	}

	@Override
	public boolean isDone() {
		return done;
	}

}
