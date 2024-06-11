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

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.glasspath.aerialist.Alignment;
import org.glasspath.aerialist.editor.AbstractEditorPanel;
import org.glasspath.aerialist.icons.Icons;
import org.glasspath.aerialist.resources.AerialistResources;
import org.glasspath.aerialist.swing.view.IScalableView;

public class SetImageAlignmentAction extends AbstractAction {

	private final AbstractEditorPanel context;
	private final IScalableView view;
	private final Alignment alignment;

	public SetImageAlignmentAction(AbstractEditorPanel context, IScalableView view, Alignment alignment) {

		this.context = context;
		this.view = view;
		this.alignment = alignment;

		putValue(Action.SELECTED_KEY, false);

		if (alignment == Alignment.LEFT) {
			putValue(Action.NAME, AerialistResources.getString("AlignLeft")); //$NON-NLS-1$
			putValue(Action.SHORT_DESCRIPTION, AerialistResources.getString("AlignLeft")); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, Icons.formatAlignLeft);
		} else if (alignment == Alignment.CENTER) {
			putValue(Action.NAME, AerialistResources.getString("AlignCenter")); //$NON-NLS-1$
			putValue(Action.SHORT_DESCRIPTION, AerialistResources.getString("AlignCenter")); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, Icons.formatAlignCenter);
		} else if (alignment == Alignment.RIGHT) {
			putValue(Action.NAME, AerialistResources.getString("AlignRight")); //$NON-NLS-1$
			putValue(Action.SHORT_DESCRIPTION, AerialistResources.getString("AlignRight")); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, Icons.formatAlignRight);
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		Alignment oldAlignment = view.getAlignment();

		view.setAlignment(alignment);
		context.refresh((Component) view);

		context.undoableEditHappened(new SetImageAlignmentUndoable(context, view, oldAlignment, alignment));

	}

}
