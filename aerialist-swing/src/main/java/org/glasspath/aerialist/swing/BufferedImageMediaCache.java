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
package org.glasspath.aerialist.swing;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;

import org.glasspath.aerialist.media.MediaCache;

public class BufferedImageMediaCache extends MediaCache<BufferedImage> {

	public BufferedImageMediaCache() {

	}

	@Override
	protected BufferedImage readImage(String key, byte[] bytes) {

		try {

			ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
			BufferedImage image = ImageIO.read(inputStream);
			inputStream.close();

			return image;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	@Override
	public ImageSize getImageSize(String key) {
		BufferedImage image = getImage(key);
		if (image != null) {
			return new ImageSize(image.getWidth(), image.getHeight());
		} else {
			return null;
		}
	}

}
