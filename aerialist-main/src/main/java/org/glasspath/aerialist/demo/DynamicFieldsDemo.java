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
package org.glasspath.aerialist.demo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.Timer;

import org.glasspath.aerialist.Aerialist;
import org.glasspath.aerialist.Document;
import org.glasspath.aerialist.DynamicFieldContext;
import org.glasspath.aerialist.editor.DocumentEditorContext;
import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.swing.view.DynamicFieldCache;
import org.glasspath.aerialist.swing.view.LayeredPageView;
import org.glasspath.aerialist.swing.view.PageView;

public class DynamicFieldsDemo {

	private boolean demoStarted = false;

	public DynamicFieldsDemo() {

		DocumentEditorContext editorContext = new DocumentEditorContext() {

			@Override
			public void populateInsertElementMenu(DocumentEditorPanel context, JMenu menu) {

			}

			@Override
			public void documentShown(DocumentEditorPanel context, Document document, String path) {
				if (!demoStarted) {
					startDemo(context);
					demoStarted = true;
				}
			}
		};

		String filePath = DemoUtils.getDemoResourcePath("dynamic-fields-demo.gpdx"); //$NON-NLS-1$
		if (filePath != null) {
			Aerialist.launch(editorContext, null, new String[] { filePath });
		}

	}

	private void startDemo(DocumentEditorPanel context) {

		if (context.getPageContainer().getPageViews().size() == 1) {

			PageView pageView = context.getPageContainer().getPageViews().get(0);
			if (pageView instanceof LayeredPageView) {

				DynamicFieldCache fieldCache = new DynamicFieldCache((LayeredPageView) pageView);

				DynamicFieldContext fieldContext = new DynamicFieldContext() {

					int i = 0;

					@Override
					public String getString(String key) {
						return key + "(" + ++i * 10 + ")"; //$NON-NLS-1$ //$NON-NLS-2$
					}
				};

				// Repaint at 30 frames per second (for testing video overlay performance)
				Timer timer = new Timer(1000 / 30, new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						// System.out.println("Updating dynamic fields");
						fieldCache.updateDynamicFields(fieldContext);
					}
				});
				timer.start();

			}

		}

	}

	public static void main(String[] args) {
		new DynamicFieldsDemo();
	}

}
