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

	public static final String LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
	public static final String SED_UT_PERSPICIATIS = "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?";

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
		table.setHeaderRows(1);
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

		table.getRowStyles().add(AerialistUtils.createRowStyle(0, 0, ColorUtils.toHex(TABLE_HEADER_ROW_COLOR)));
		table.getRowStyles().add(AerialistUtils.createRowStyle(1, 2, ColorUtils.toHex(TABLE_ALTERNATING_ROW_COLOR)));

		return table;

	}

}
