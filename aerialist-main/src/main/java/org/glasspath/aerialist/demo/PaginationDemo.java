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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;

import org.glasspath.aerialist.Aerialist;
import org.glasspath.aerialist.editor.DocumentEditorContext;
import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.editor.actions.InsertElementAction;
import org.glasspath.aerialist.icons.Icons;
import org.glasspath.aerialist.template.TemplateFieldContext;
import org.glasspath.aerialist.template.TemplateMetadata;
import org.glasspath.aerialist.template.TemplateMetadata.CategoryMetadata;
import org.glasspath.aerialist.template.TemplateMetadata.FieldMetadata;
import org.glasspath.aerialist.template.TemplateMetadata.TableMetadata;

public class PaginationDemo {

	public PaginationDemo() {

		TemplateMetadata templateMetadata = new TemplateMetadata();

		CategoryMetadata templateFields = new CategoryMetadata("Pagination demo");
		templateMetadata.setTemplateFields(templateFields);

		TableMetadata tableMetadata = new TableMetadata("table");
		tableMetadata.getChildren().add(new FieldMetadata("Field 1", "table.field1"));
		tableMetadata.getChildren().add(new FieldMetadata("Field 2", "table.field2"));
		templateFields.getChildren().add(tableMetadata);

		DocumentEditorContext editorContext = new DocumentEditorContext() {

			@Override
			public void populateInsertElementMenu(DocumentEditorPanel context, JMenu menu) {

				menu.addSeparator();

				JMenu databaseMenu = new JMenu("Pagination demo");
				menu.add(databaseMenu);

				databaseMenu.add(new InsertElementAction(context, DemoUtils.createTable("Table", new String[] { "Column 1", "Column 2" }), "Demo table", Icons.tablePlus));

			}
		};
		editorContext.setTemplateMetadata(templateMetadata);

		TemplateFieldContext templateFieldContext = new TemplateFieldContext();

		List<String> tableColumn1 = new ArrayList<>();
		List<String> tableColumn2 = new ArrayList<>();

		for (int row = 1; row <= 50; row++) {
			tableColumn1.add("col 1, row " + row);
			tableColumn2.add("col 2, row " + row);
		}

		templateFieldContext.put("table.field1", tableColumn1);
		templateFieldContext.put("table.field2", tableColumn2);

		templateFieldContext.put("paragraph1.title", "Paragraph 1 Title");
		templateFieldContext.put("paragraph1.content", DemoUtils.LOREM_IPSUM + "\n" + DemoUtils.LOREM_IPSUM);

		templateFieldContext.put("paragraph2.title", "Paragraph 2 Title");
		templateFieldContext.put("paragraph2.content", DemoUtils.SED_UT_PERSPICIATIS + "\n" + DemoUtils.SED_UT_PERSPICIATIS);

		templateFieldContext.put("paragraph3.content", "Notice how this paragraph has retained it's distance to the bottom of all three elements above.");
		// templateFieldContext.put("paragraph3.content", "At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident, similique sunt in culpa qui officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi optio cumque nihil impedit quo minus id quod maxime placeat facere possimus, omnis voluptas assumenda est, omnis dolor repellendus. Temporibus autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe eveniet ut et voluptates repudiandae sint et molestiae non recusandae. Itaque earum rerum hic tenetur a sapiente delectus, ut aut
		// reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus asperiores repellat.");

		String filePath = DemoUtils.getDemoResourcePath("pagination-demo.gpdx");
		if (filePath != null) {
			Aerialist.launch(editorContext, templateFieldContext, new String[] { filePath });
		}

	}

	public static void main(String[] args) {
		new PaginationDemo();
	}

}
