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
package org.glasspath.aerialist.media;

import java.util.HashMap;
import java.util.Map;

public abstract class MediaCache<T> {

	private final Map<String, ImageResource> imageResources = new HashMap<>();
	private final Map<String, T> images = new HashMap<>();

	public MediaCache() {

	}

	public Map<String, ImageResource> getImageResources() {
		return imageResources;
	}

	public T putImage(String key, byte[] bytes) {

		try {

			imageResources.put(key, new ImageResource(bytes));

			T image = readImage(key, bytes);
			if (image != null) {
				images.put(key, image);
			}

			return image;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	public ImageResource getImageResource(String key) {
		return imageResources.get(key);
	}

	protected abstract T readImage(String key, byte[] bytes);

	public T getImage(String key) {
		return images.get(key);
	}
	
	public abstract ImageSize getImageSize(String key);

	public static class ImageResource {

		private final byte[] bytes;

		public ImageResource(byte[] bytes) {
			this.bytes = bytes;
		}

		public byte[] getBytes() {
			return bytes;
		}

	}

	public static class ImageSize {

		public int width = 0;
		public int height = 0;

		public ImageSize(int width, int height) {
			this.width = width;
			this.height = height;
		}

	}

}
