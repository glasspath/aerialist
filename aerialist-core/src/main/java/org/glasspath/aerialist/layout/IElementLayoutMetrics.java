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
package org.glasspath.aerialist.layout;

import org.glasspath.aerialist.Element;
import org.glasspath.aerialist.FitPolicy;
import org.glasspath.aerialist.HeightPolicy;
import org.glasspath.aerialist.Image;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.ElementLayoutInfo;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.LayoutInfo;

public interface IElementLayoutMetrics extends ILayoutMetrics<Element> {

	public LayoutInfo getLayoutInfo();

	public void setLayoutInfo(LayoutInfo layoutInfo);

	public ElementLayoutInfo getElementLayoutInfo(Element element);

	public static int getPreferredImageHeight(Image image, int width, int height) {

		if (HeightPolicy.get(image.getHeightPolicy()) == HeightPolicy.AUTO) {
			if (FitPolicy.get(image.getFit()) == FitPolicy.WIDTH) {
				return (int) (height * ((double) image.getWidth() / (double) width));
			} else {
				return height;
			}
		}

		return image.getHeight();

	}

}
