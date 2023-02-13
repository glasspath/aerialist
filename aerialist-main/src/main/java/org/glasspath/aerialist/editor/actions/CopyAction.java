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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.Element;
import org.glasspath.aerialist.Page;
import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.swing.view.ISwingElementView;
import org.glasspath.aerialist.swing.view.LayeredPageView;
import org.glasspath.aerialist.swing.view.PageView;
import org.glasspath.aerialist.swing.view.TableCellView;
import org.glasspath.aerialist.swing.view.TextView;
import org.glasspath.common.os.OsUtils;

public class CopyAction extends AbstractAction {

	protected final DocumentEditorPanel context;
	protected final List<Object> selection = new ArrayList<>();
	protected PageView sourcePageView = null;

	public CopyAction(DocumentEditorPanel context) {

		this.context = context;

		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, OsUtils.CTRL_OR_CMD_MASK));
		putValue(Action.NAME, "Copy");
		putValue(Action.SHORT_DESCRIPTION, "Copy");
		putValue(Action.SMALL_ICON, org.glasspath.common.icons.Icons.contentCopy);

	}

	@Override
	public boolean isEnabled() {
		return context.getSelection().size() > 0;
	}

	public boolean isPageSelection() {

		for (Object object : selection) {
			if (!(object instanceof Page)) {
				return false;
			}
		}

		return selection.size() > 0;

	}

	public boolean isElementSelection() {

		for (Object object : selection) {
			if (!(object instanceof Element)) {
				return false;
			}
		}

		return selection.size() > 0;

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		TextView textView = null;
		if (context.getSelection().size() == 1 && context.getSelection().get(0) instanceof TextView) {
			textView = (TextView) context.getSelection().get(0);
		}

		if (textView != null && textView.getSelectionEnd() > textView.getSelectionStart()) {

			// Text in TextView is selected, so copy this text
			textView.copy();

		} else {

			selection.clear();
			sourcePageView = null;

			for (Component component : context.getSelection()) {

				Object object = null;

				if (component instanceof LayeredPageView) {
					object = ((LayeredPageView) component).toPage();
				} else if (component instanceof TableCellView) {

					// When a table cell is selected we want to copy the table
					object = ISwingElementView.createElement(component.getParent());

					// TODO: Check if all selected elements are on the same page?
					sourcePageView = AerialistUtils.getPageView(component);

				} else if (component instanceof ISwingElementView<?>) {

					object = ISwingElementView.createElement(component);

					// TODO: Check if all selected elements are on the same page?
					sourcePageView = AerialistUtils.getPageView(component);

				}

				if (object != null) {
					selection.add(object);
				}

			}

			// TODO? (Is fired for updating edit menu)
			context.getSelection().fireSelectionChanged();

		}

	}

}
