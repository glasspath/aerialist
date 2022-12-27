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
package org.glasspath.aerialist.demo;

import java.awt.Color;
import java.net.URL;
import java.nio.file.Paths;

import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.Alignment;
import org.glasspath.aerialist.BorderType;
import org.glasspath.aerialist.HeightPolicy;
import org.glasspath.aerialist.Padding;
import org.glasspath.aerialist.Table;
import org.glasspath.aerialist.swing.view.ColorUtils;
import org.glasspath.common.Common;

public class DemoUtils {

	public static final Color TABLE_HEADER_ROW_COLOR = new Color(217, 217, 217);
	public static final Color TABLE_ALTERNATING_ROW_COLOR = new Color(242, 242, 242);
	public static final Color TABLE_VERTICAL_LINE_COLOR = new Color(165, 165, 165);

	private DemoUtils() {

	}

	public static String getDemoResourcePath(String resourceName) {

		try {

			URL demoResourceURL = PaginationDemo.class.getResource("/org/glasspath/aerialist/demo/" + resourceName);
			if (demoResourceURL != null) {
				return Paths.get(demoResourceURL.toURI()).toFile().getAbsolutePath();
			}

		} catch (Exception e) {
			Common.LOGGER.error("Exception while getting demo resource: ", e);
		}

		return null;

	}

	public static Table createTable(String tableName, String[] columnNames) {

		Table table = new Table();
		table.setWidth(470);
		table.setHeightPolicy(HeightPolicy.AUTO.stringValue);
		table.setCellPadding(Padding.from(3));
		table.getBorders().add(AerialistUtils.createBorder(BorderType.VERTICAL, 1, TABLE_VERTICAL_LINE_COLOR));

		if (columnNames != null && columnNames.length > 0) {

			int columnWidth = 470 / columnNames.length;

			for (int col = 0; col < columnNames.length; col++) {

				table.getColStyles().add(AerialistUtils.createColStyle(col + 1, columnWidth));

				table.getTableCells().add(AerialistUtils.createTableCell(columnNames[col], 1, col + 1, Alignment.DEFAULT.stringValue, 10, true, null));

				table.getTableCells().add(AerialistUtils.createTableCell(columnNames[col], 2, col + 1, Alignment.DEFAULT.stringValue, 10, false, "t:" + tableName + "." + columnNames[col]));

			}

		}

		table.getRowStyles().add(AerialistUtils.createRowStyle(1, 0, ColorUtils.toHex(TABLE_HEADER_ROW_COLOR)));
		table.getRowStyles().add(AerialistUtils.createRowStyle(1, 2, ColorUtils.toHex(TABLE_ALTERNATING_ROW_COLOR)));

		return table;

	}

}
