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

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;

import javax.swing.SwingUtilities;

import org.glasspath.aerialist.Aerialist;
import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.icons.Icons;
import org.glasspath.aerialist.swing.view.ImageView;
import org.glasspath.aerialist.swing.view.PageView;
import org.glasspath.common.swing.file.chooser.FileChooser;

public class InsertElementOperation extends Operation {

	private final DocumentEditorPanel context;
	private final Component component;

	private PageView pageView = null;
	private boolean componentAdded = false;

	private boolean done = false;

	public InsertElementOperation(DocumentEditorPanel context, Component component) {

		this.context = context;
		this.component = component;

		// TODO: For now layout Y-policy is always disabled because of following issues:
		// - Inserting element causes other elements to shift position
		// - Moving element above other element causes anchor to be created after which element also moves
		// - Undo/redo causes elements to shift due to newly created anchors
		context.getPageContainer().setYPolicyEnabled(false);

		if (context.getSelection().size() > 0) {

			pageView = AerialistUtils.getPageView(context.getSelection().get(0));
			if (pageView != null) {

				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {

						// TODO: For now we clear the selection, otherwise the new
						// component will be painted as part of a multiple selection
						context.deselectAll();

						// Generate a mouse moved event to update the bounds of the new component
						context.getMouseOperationHandler().generateMouseMovedEvent(context.getPageContainer());

					}
				});

			}

		}

	}

	@Override
	public void mouseMoved(MouseEvent e, Point p) {

		if (pageView != null) {

			Rectangle bounds = component.getBounds();

			Point point = context.convertPointToPage(p, pageView, true);

			bounds.x = point.x;
			bounds.y = point.y;

			component.setBounds(bounds);

			if (!componentAdded) {

				pageView.add(component);

				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						context.getSelection().select(component);
					}
				});

				componentAdded = true;

			}

			pageView.updateVerticalAnchors();

			context.getSelection().fireSelectionChanged();
			context.refresh(null, false);

		}

	}

	@Override
	public void mousePressed(MouseEvent e, Point p) {

		if (pageView != null) {

			if (component instanceof ImageView) {

				ImageView imageView = (ImageView) component;
				if (imageView.getSrc() == null || imageView.getSrc().length() == 0) {

					String filePath = FileChooser.browseForImageFile(Icons.image, false, context.getFrame(), Aerialist.PREFERENCES, "lastImageFilePath"); //$NON-NLS-1$
					if (filePath != null) {

						try {

							File file = new File(filePath);
							String key = file.getName();

							BufferedImage image = context.getMediaCache().putImage(key, Files.readAllBytes(file.toPath()));
							if (image != null) {
								((ImageView) component).setImage(key, image);
							}

						} catch (Exception ex) {
							ex.printStackTrace();
						}

					}

				}

			}

			context.undoableEditHappened(new InsertElementUndoable(context, component, pageView));

			context.getSelection().fireSelectionChanged();
			context.refresh(pageView);

			e.consume();

		}

		done = true;

	}

	@Override
	public boolean isDone() {
		return done;
	}

}
