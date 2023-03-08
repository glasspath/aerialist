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
package org.glasspath.aerialist.source;

@SuppressWarnings("nls")
public class SourceUtils {

	private SourceUtils() {

	}

	public static TextNodeInfo getTextNodeInfo(String source, int start, int end) {

		TextNodeInfo nodeInfo = new TextNodeInfo();

		String nodeStart = "<text>";
		String nodeEnd = "</text>";

		nodeInfo.end = source.indexOf(nodeEnd, end);
		if (nodeInfo.end >= end) {

			// Node-start was found before node-end, we are not inside node
			int nextNodeStartIndex = source.indexOf(nodeStart, end);
			if (nextNodeStartIndex >= 0 && nextNodeStartIndex < nodeInfo.end) {
				return null;
			} else {

				nodeInfo.start = source.lastIndexOf(nodeStart, start);
				if (nodeInfo.start >= 0 && nodeInfo.start <= start - nodeStart.length()) {

					// Node-end was found after node-start, we are not inside node
					int previousNodeEndIndex = source.lastIndexOf(nodeEnd, start);
					if (previousNodeEndIndex >= 0 && previousNodeEndIndex > nodeInfo.start) {
						return null;
					} else {

						try {

							nodeInfo.text = source.substring(nodeInfo.start + nodeStart.length(), nodeInfo.end);

							return nodeInfo;

						} catch (Exception e) {
							// TODO? (Should not be possible)
						}

					}

				}

			}

		}

		return null;
	}

	public static class TextNodeInfo {

		public int start = -1;
		public int end = -1;
		public String text = null;

	}

}
