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
package org.glasspath.aerialist.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.glasspath.aerialist.Aerialist;
import org.glasspath.common.swing.dialog.AboutDialog;
import org.glasspath.common.swing.dialog.AboutDialog.IAbout;
import org.glasspath.common.swing.help.HelpUtils;
import org.glasspath.common.swing.resources.CommonResources;
import org.glasspath.common.swing.tools.AbstractTools;

public class HelpTools extends AbstractTools<Aerialist> {

	public HelpTools(Aerialist context) {
		super(context, CommonResources.getString("Help")); //$NON-NLS-1$

		JMenuItem helpMenuItem = new JMenuItem(CommonResources.getString("Help")); //$NON-NLS-1$
		helpMenuItem.setAccelerator(KeyStroke.getKeyStroke("F1")); //$NON-NLS-1$
		helpMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				HelpUtils.showHelp("https://glasspath.org"); //$NON-NLS-1$
			}
		});
		menu.add(helpMenuItem);

		JMenuItem aboutMenuItem = new JMenuItem(CommonResources.getString("About")); //$NON-NLS-1$
		aboutMenuItem.setEnabled(context.getEditorContext() != null && context.getEditorContext().getAbout() != null);
		aboutMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {

				IAbout about = null;

				if (context.getEditorContext() != null) {
					about = context.getEditorContext().getAbout();
				}

				if (about != null) {
					new AboutDialog(context, about);
				}

			}
		});
		menu.add(aboutMenuItem);

	}

}
