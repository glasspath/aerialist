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
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.glasspath.aerialist.editor.EditorPanel;
import org.glasspath.aerialist.icons.Icons;
import org.glasspath.aerialist.swing.view.ImageView;
import org.glasspath.common.swing.file.chooser.FileChooser;

public class SetImageAction extends AbstractAction {

	private final EditorPanel<? extends EditorPanel<?>> context;
	private final ImageView imageView;

	public SetImageAction(EditorPanel<? extends EditorPanel<?>> context, ImageView imageView) {

		this.context = context;
		this.imageView = imageView;

		putValue(Action.NAME, "Choose image");
		putValue(Action.SHORT_DESCRIPTION, "Choose image");
		putValue(Action.SMALL_ICON, Icons.image);

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String oldSrc = imageView.getSrc();

		String filePath = FileChooser.browseForImageFile(Icons.image, false, context.getFrame(), context.getPreferences(), "lastImageFilePath");
		if (filePath != null) {

			try {

				File file = new File(filePath);
				String key = file.getName();

				BufferedImage image = context.getMediaCache().putImage(key, Files.readAllBytes(file.toPath()));
				if (image != null) {

					imageView.setImage(key, image);
					imageView.repaint();

					context.undoableEditHappened(new SetImageUndoable(context, imageView, oldSrc, key));

				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}

	}

}
