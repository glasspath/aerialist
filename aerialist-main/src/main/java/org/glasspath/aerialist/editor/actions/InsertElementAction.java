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
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.glasspath.aerialist.Element;
import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.editor.InsertElementOperation;
import org.glasspath.aerialist.swing.view.ISwingElementView;
import org.glasspath.aerialist.swing.view.PageView;

public class InsertElementAction extends AbstractAction {

	private final DocumentEditorPanel context;
	private final Element element;

	public InsertElementAction(DocumentEditorPanel context, Element element, String name, Icon icon) {

		this.context = context;
		this.element = element;

		putValue(Action.NAME, name);
		putValue(Action.SHORT_DESCRIPTION, name);
		putValue(Action.SMALL_ICON, icon);

	}

	@Override
	public boolean isEnabled() {
		return context.getSelection().size() == 1 && context.getSelection().get(0) instanceof PageView;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		context.setScrollLock(true);

		JComponent component = ISwingElementView.createElementView(element, context.getPageContainer());
		component.setBounds(element.getX(), element.getY(), element.getWidth(), element.getHeight());

		context.getMouseOperationHandler().startOperation(new InsertElementOperation(context, component));

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				context.setScrollLock(false);
			}
		});

	}

}
