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
package org.glasspath.aerialist.swing.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import org.glasspath.aerialist.Alignment;
import org.glasspath.aerialist.Border;
import org.glasspath.aerialist.BorderType;
import org.glasspath.aerialist.FitPolicy;

public class PaintUtils {

	private PaintUtils() {

	}

	public static void paintImage(Graphics2D g2d, int width, int height, BufferedImage image, double scale, Alignment alignment, FitPolicy fitPolicy) {

		if (image != null && image.getWidth() > 0 && image.getHeight() > 0) {

			if (fitPolicy == FitPolicy.WIDTH) {
				scale = (double) width / (double) image.getWidth();
			} else if (fitPolicy == FitPolicy.HEIGHT) {
				scale = (double) height / (double) image.getHeight();
			}

			if (scale > 0.0) {

				int x = 0;
				int w = (int) (width / scale);

				if (alignment == Alignment.CENTER) {
					x = (w / 2) - (image.getWidth() / 2);
				} else if (alignment == Alignment.RIGHT) {
					x = w - image.getWidth();
				}

				g2d.scale(scale, scale);
				g2d.drawImage(image, x, 0, null);
				g2d.scale(1.0 / scale, 1.0 / scale);

			}

		}

	}

	public static void paintBorders(Graphics2D g2d, List<Border> borders, Rectangle rect) {
		for (Border border : borders) {
			paintBorder(g2d, border, rect);
		}
	}

	public static void paintBorder(Graphics2D g2d, Border border, Rectangle rect) {

		Color color = ColorUtils.fromHex(border.color);
		if (color != null && border.width > 0.0) {

			g2d.setColor(color);

			Rectangle2D r = new Rectangle2D.Float();

			switch (BorderType.get(border.type)) {

			case DEFAULT:

				r.setFrame(rect.x, rect.y, rect.x + rect.width, border.width);
				Area area = new Area(r);

				r.setFrame(rect.x + rect.width - border.width, rect.y, border.width, rect.y + rect.height);
				area.add(new Area(r));

				r.setFrame(rect.x, rect.y + rect.height - border.width, rect.x + rect.width, border.width);
				area.add(new Area(r));

				r.setFrame(rect.x, rect.y, border.width, rect.y + rect.height);
				area.add(new Area(r));

				g2d.fill(area);

				break;

			case TOP:
				r.setFrame(rect.x, rect.y, rect.x + rect.width, border.width);
				g2d.fill(r);
				break;

			case RIGHT:
				r.setFrame(rect.x + rect.width - border.width, rect.y, border.width, rect.y + rect.height);
				g2d.fill(r);
				break;

			case BOTTOM:
				r.setFrame(rect.x, rect.y + rect.height - border.width, rect.x + rect.width, border.width);
				g2d.fill(r);
				break;

			case LEFT:
				r.setFrame(rect.x, rect.y, border.width, rect.y + rect.height);
				g2d.fill(r);
				break;

			case VERTICAL:
				// Not implemented
				break;

			case HORIZONTAL:
				// Not implemented
				break;

			default:
				break;
			}

		}

	}

}
