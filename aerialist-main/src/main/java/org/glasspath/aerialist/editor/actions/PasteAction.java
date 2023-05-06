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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.glasspath.aerialist.Element;
import org.glasspath.aerialist.Page;
import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.editor.actions.PasteUndoable.ComponentInfo;
import org.glasspath.aerialist.swing.view.ISwingElementView;
import org.glasspath.aerialist.swing.view.LayeredPageView;
import org.glasspath.aerialist.swing.view.PageContainer;
import org.glasspath.aerialist.swing.view.PageView;
import org.glasspath.aerialist.swing.view.TextView;
import org.glasspath.common.os.OsUtils;

public class PasteAction extends AbstractAction {

	protected final DocumentEditorPanel context;
	protected final CopyAction copyAction;

	public PasteAction(DocumentEditorPanel context, CopyAction copyAction) {

		this.context = context;
		this.copyAction = copyAction;

		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, OsUtils.CTRL_OR_CMD_MASK));
		putValue(Action.NAME, "Paste");
		putValue(Action.SHORT_DESCRIPTION, "Paste");

	}

	@Override
	public boolean isEnabled() {

		boolean enabled = false;

		// First check if a TextView is selected (invoke copy/paste actions of TextView)
		if (context.getSelection().size() == 1 && context.getSelection().get(0) instanceof TextView) {

			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			if (clipboard != null) {

				DataFlavor[] dataFlavors = clipboard.getAvailableDataFlavors();
				if (dataFlavors != null && dataFlavors.length > 0) {

					// TODO: Check if pasting of contents from clip-board is possible
					enabled = true;

				}

			}

		} else if (copyAction.selection.size() == 0) {

			enabled = false;

		} else if (context.getSelection().size() == 0) {

			// Nothing selected, we can only paste pages
			enabled = copyAction.isPageSelection();

		} else if (context.getSelection().size() == 1 && context.getSelection().get(0) instanceof PageView) {

			// Page selected, we can paste if the selection only contains elements
			enabled = copyAction.isElementSelection();

			// We can also paste if the selection only contains pages
			if (!enabled) {
				enabled = copyAction.isPageSelection();
			}

		} else {
			enabled = false;
		}

		return enabled;

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (context.getSelection().size() == 1 && context.getSelection().get(0) instanceof TextView) {

			((TextView) context.getSelection().get(0)).paste();

		} else {

			List<ComponentInfo> pastedComponents = new ArrayList<>();

			if (context.getSelection().size() == 0) {

				if (copyAction.isPageSelection()) {

					for (Object object : copyAction.selection) {

						if (object instanceof Page) {

							LayeredPageView newPageView = PageContainer.createLayeredPageView((Page) object, context.getPageContainer());
							int index = context.getPageContainer().getPageViews().size();
							context.getPageContainer().insertPageView(newPageView, index);

							pastedComponents.add(new ComponentInfo(newPageView, context.getPageContainer(), index));

						}

					}

				}

			} else if (context.getSelection().size() == 1 && context.getSelection().get(0) instanceof PageView) {

				PageView pageView = (PageView) context.getSelection().get(0);

				if (copyAction.isElementSelection()) {

					for (Object object : copyAction.selection) {

						if (object instanceof Element) {

							Element element = (Element) object;

							// Shift the element a little bit when pasting to the same page, we perform the
							// shifting on the source object so that multiple paste actions are all shifted
							if (pageView == copyAction.sourcePageView) {
								element.setX(element.getX() + 10);
								element.setY(element.getY() + 10);
							}

							JComponent newComponent = ISwingElementView.createElementView(element, context.getPageContainer());
							newComponent.setBounds(element.getX(), element.getY(), element.getWidth(), element.getHeight());

							int index = pageView.getComponentCount();
							pageView.add(newComponent, index);

							pastedComponents.add(new ComponentInfo(newComponent, pageView, index));

						}

					}

					pageView.updateVerticalAnchors();

				} else if (copyAction.isPageSelection()) {

					int index = context.getPageContainer().getPageViews().indexOf(pageView) + 1;

					for (Object object : copyAction.selection) {

						if (object instanceof Page) {

							LayeredPageView newPageView = PageContainer.createLayeredPageView((Page) object, context.getPageContainer());
							context.getPageContainer().insertPageView(newPageView, index);

							pastedComponents.add(new ComponentInfo(newPageView, context.getPageContainer(), index));

							index++;

						}

					}

				}

			}

			if (pastedComponents.size() > 0) {

				context.undoableEditHappened(new PasteUndoable(context, pastedComponents));

				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						selectPastedComponents(context, pastedComponents);
						context.refresh(null, true, true);
					}
				});

			}

		}

	}

	public static void selectPastedComponents(DocumentEditorPanel context, List<ComponentInfo> pastedComponents) {

		context.getSelection().clear();

		for (ComponentInfo pastedComponent : pastedComponents) {
			context.getSelection().add(pastedComponent.component);
		}

		context.getSelection().fireSelectionChanged();

	}

}
