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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glasspath.aerialist.Element;
import org.glasspath.aerialist.Image;
import org.glasspath.aerialist.Page;
import org.glasspath.aerialist.Table;
import org.glasspath.aerialist.TextBox;
import org.glasspath.aerialist.text.TextLayout;

public class DocumentLayoutInfo {

	public PageLayoutInfo header = null;
	public PageLayoutInfo footer = null;
	public List<PageLayoutInfo> pages = new ArrayList<>();

	public DocumentLayoutInfo() {

	}

	public static class PageLayoutInfo {

		public final Page page;
		public LayoutInfo layoutInfo = null;

		public PageLayoutInfo(Page page) {
			this.page = page;
		}

	}

	public static class LayoutInfo {

		public Map<TextBox, TextBoxLayoutInfo> textBoxes = new HashMap<>();
		public Map<Table, TableLayoutInfo> tables = new HashMap<>();
		public Map<Image, ImageLayoutInfo> images = new HashMap<>();

		public LayoutInfo() {

		}

		public void clear() {
			textBoxes.clear();
			tables.clear();
			images.clear();
		}

		public void move(Element element, LayoutInfo toLayoutInfo) {

			if (element instanceof TextBox) {
				TextBoxLayoutInfo layoutInfo = textBoxes.remove(element);
				if (layoutInfo != null) {
					toLayoutInfo.textBoxes.put((TextBox) element, layoutInfo);
				}
			} else if (element instanceof Table) {
				TableLayoutInfo layoutInfo = tables.remove(element);
				if (layoutInfo != null) {
					toLayoutInfo.tables.put((Table) element, layoutInfo);
				}
			} else if (element instanceof Image) {
				ImageLayoutInfo layoutInfo = images.remove(element);
				if (layoutInfo != null) {
					toLayoutInfo.images.put((Image) element, layoutInfo);
				}
			}

		}

	}

	public static class ElementLayoutInfo {

		public int preferredHeight = 0;

		public ElementLayoutInfo() {

		}

		public ElementLayoutInfo(int preferredHeight) {
			this.preferredHeight = preferredHeight;
		}

	}

	public static class TextBoxLayoutInfo extends ElementLayoutInfo {

		public TextLayout textLayout = null;

		public TextBoxLayoutInfo() {

		}

	}

	public static class TableLayoutInfo extends ElementLayoutInfo {

		public int rowCount = 0;
		public Bounds[] rowBounds = null;
		public Bounds[] columnBounds = null;
		public TextLayout[] textLayouts = null;

		public TableLayoutInfo() {

		}

	}

	public static class ImageLayoutInfo extends ElementLayoutInfo {

		public int imageWidth = 0;
		public int imageHeight = 0;

		public ImageLayoutInfo() {

		}

	}

	public static class Bounds {

		public int x = 0;
		public int y = 0;
		public int width = 0;
		public int height = 0;

		public Bounds() {

		}

		public Bounds(Bounds b) {
			this.x = b.x;
			this.y = b.y;
			this.width = b.width;
			this.height = b.height;
		}

		public Bounds(int x, int y, int w, int h) {
			this.x = x;
			this.y = y;
			this.width = w;
			this.height = h;
		}

	}

	public static class Rect {

		public float x = 0;
		public float y = 0;
		public float width = 0;
		public float height = 0;

		public Rect() {

		}

		public Rect(Rect r) {
			this.x = r.x;
			this.y = r.y;
			this.width = r.width;
			this.height = r.height;
		}

		public Rect(Bounds b) {
			this.x = b.x;
			this.y = b.y;
			this.width = b.width;
			this.height = b.height;
		}

		public Rect(float x, float y, float width, float height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

	}

}
