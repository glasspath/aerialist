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
import org.glasspath.aerialist.swing.view.PageView;

public class SetPageSizeAction extends AbstractAction {

	private final AbstractEditorPanel context;
	private final PageView pageView;
	private final int width;
	private final int height;

	public SetPageSizeAction(AbstractEditorPanel context, PageView pageView, int width, int height, String description) {

		this.context = context;
		this.pageView = pageView;
		this.width = width;
		this.height = height;

		putValue(Action.NAME, description);
		putValue(Action.SHORT_DESCRIPTION, description);
		putValue(Action.SELECTED_KEY, pageView.getWidth() == width && pageView.getHeight() == height);

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (pageView != null) {

			int oldWidth = pageView.getWidth();
			int oldHeight = pageView.getHeight();

			pageView.setPageSize(width, height);
			context.refresh(null);

			context.undoableEditHappened(new SetPageSizeUndoable(context, pageView, width, height, oldWidth, oldHeight));

		}

	}

}
