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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;

import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.RowStyle;
import org.glasspath.aerialist.editor.EditorPanel;
import org.glasspath.aerialist.swing.view.ISwingElementView;
import org.glasspath.aerialist.swing.view.TableView;
import org.glasspath.common.swing.color.ColorChooserPanel.ColorEvent;
import org.glasspath.common.swing.color.ColorUtils;

public class SetRowColorAction extends AbstractAction {

	private final EditorPanel<? extends EditorPanel<?>> context;
	private final int row;
	private final int repeat;

	public SetRowColorAction(EditorPanel<? extends EditorPanel<?>> context, int row, int repeat) {
		this.context = context;
		this.row = row;
		this.repeat = repeat;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e instanceof ColorEvent) {

			Color color = ((ColorEvent) e).color;

			if (context.getSelection().size() == 1) {

				ISwingElementView<?> elementView = AerialistUtils.getElementView(context.getSelection().get(0));
				if (elementView instanceof TableView) {

					TableView tableView = (TableView) elementView;

					List<RowStyle> oldRowStyles = tableView.getRowStylesCopy();

					applyRowColor(tableView, row, repeat, color);

					context.undoableEditHappened(new SetRowColorUndoable(tableView, row, repeat, color, oldRowStyles));

				}

			}

		}

	}

	public static void applyRowColor(TableView tableView, int row, int repeat, Color color) {

		boolean create = true;

		for (RowStyle rowStyle : tableView.getRowStyles()) {
			if (rowStyle.row == row && rowStyle.repeat == repeat) {
				rowStyle.background = ColorUtils.toHex(color);
				create = false;
				break;
			}
		}

		if (create) {

			RowStyle rowStyle = new RowStyle();
			rowStyle.row = row;
			rowStyle.repeat = repeat;
			rowStyle.background = ColorUtils.toHex(color);

			tableView.getRowStyles().add(rowStyle);

		}

		tableView.repaint();

	}

}
