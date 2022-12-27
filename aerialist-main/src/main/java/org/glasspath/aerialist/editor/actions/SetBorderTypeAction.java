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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;

import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.Border;
import org.glasspath.aerialist.BorderType;
import org.glasspath.aerialist.editor.EditorPanel;
import org.glasspath.aerialist.swing.view.ISwingElementView;
import org.glasspath.common.swing.border.BorderMenu.BorderMenuType;
import org.glasspath.common.swing.border.BorderMenu.BorderMenuTypeEvent;
import org.glasspath.common.swing.color.ColorUtils;

public class SetBorderTypeAction extends AbstractAction {

	private final EditorPanel<? extends EditorPanel<?>> context;

	public SetBorderTypeAction(EditorPanel<? extends EditorPanel<?>> context) {

		this.context = context;

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e instanceof BorderMenuTypeEvent) {

			BorderMenuType borderMenuType = ((BorderMenuTypeEvent) e).borderMenuType;

			if (context.getSelection().size() == 1) {

				ISwingElementView<?> elementView = AerialistUtils.getElementView(context.getSelection().get(0));
				if (elementView != null) {

					List<Border> oldBorders = new ArrayList<>();

					for (Border border : elementView.getBorders()) {
						oldBorders.add(new Border(border));
					}

					applyBorderMenuType(elementView, borderMenuType);
					((Component) elementView).repaint();

					context.undoableEditHappened(new SetBorderTypeUndoable(elementView, borderMenuType, oldBorders));

				}

			}

		}

	}

	public static void applyBorderMenuType(ISwingElementView<?> elementView, BorderMenuType borderMenuType) {

		Color color = getBorderColor(elementView.getBorders());

		if (borderMenuType == BorderMenuType.NONE || borderMenuType == null) {

			elementView.getBorders().clear();

		} else if (borderMenuType == BorderMenuType.OUTSIDE) {

			elementView.getBorders().clear();
			elementView.getBorders().add(AerialistUtils.createBorder(BorderType.DEFAULT, 1, color));

		} else if (borderMenuType == BorderMenuType.ALL) {

			elementView.getBorders().clear();
			elementView.getBorders().add(AerialistUtils.createBorder(BorderType.DEFAULT, 1, color));
			elementView.getBorders().add(AerialistUtils.createBorder(BorderType.VERTICAL, 1, color));
			elementView.getBorders().add(AerialistUtils.createBorder(BorderType.HORIZONTAL, 1, color));

		} else {

			BorderType borderType = getBorderType(borderMenuType);
			boolean add = true;

			for (int i = 0; i < elementView.getBorders().size(); i++) {
				if (BorderType.get(elementView.getBorders().get(i).type) == borderType) {
					add = false;
					elementView.getBorders().remove(i);
					break;
				}
			}

			if (add) {
				elementView.getBorders().add(AerialistUtils.createBorder(borderType, 1, color));
			}

		}

	}

	public static Color getBorderColor(List<Border> borders) {

		for (Border border : borders) {

			Color color = ColorUtils.fromHex(border.color);
			if (color != null) {
				return color;
			}
		}

		return Color.black;

	}

	public static BorderType getBorderType(BorderMenuType borderMenuType) {
		switch (borderMenuType) {
		case TOP:
			return BorderType.TOP;
		case RIGHT:
			return BorderType.RIGHT;
		case BOTTOM:
			return BorderType.BOTTOM;
		case LEFT:
			return BorderType.LEFT;
		case VERTICAL:
			return BorderType.VERTICAL;
		case HORIZONTAL:
			return BorderType.HORIZONTAL;
		case OUTSIDE:
			return BorderType.DEFAULT;
		case ALL:
			return null;
		case NONE:
			return null;
		default:
			return null;
		}
	}

}
