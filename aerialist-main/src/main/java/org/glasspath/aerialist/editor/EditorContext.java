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
package org.glasspath.aerialist.editor;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.glasspath.aerialist.Field.FieldType;
import org.glasspath.aerialist.IVisible;
import org.glasspath.aerialist.editor.actions.InsertFieldAction;
import org.glasspath.aerialist.editor.actions.SetVisibilityAction;
import org.glasspath.aerialist.icons.Icons;
import org.glasspath.aerialist.swing.view.TextView;
import org.glasspath.aerialist.template.TemplateMetadata;
import org.glasspath.aerialist.template.TemplateMetadata.AbstractMetadata;
import org.glasspath.aerialist.template.TemplateMetadata.FieldMetadata;
import org.glasspath.aerialist.template.TemplateMetadata.MetadataList;
import org.glasspath.aerialist.template.TemplateMetadata.TableMetadata;
import org.glasspath.common.swing.dialog.AboutDialog.IAbout;

public abstract class EditorContext<T extends EditorPanel<T>> {

	private String suggestedFileName = null;
	private TemplateMetadata templateMetadata = null;
	private JComponent headerComponent = null;
	private boolean editable = true;
	private IAbout about = null;

	public EditorContext() {

	}

	public String getSuggestedFileName() {
		return suggestedFileName;
	}

	public void setSuggestedFileName(String suggestedFileName) {
		this.suggestedFileName = suggestedFileName;
	}

	public TemplateMetadata getTemplateMetadata() {
		return templateMetadata;
	}

	public void setTemplateMetadata(TemplateMetadata templateMetadata) {
		this.templateMetadata = templateMetadata;
	}

	public JComponent getHeaderComponent() {
		return headerComponent;
	}

	public void setHeaderComponent(JComponent headerComponent) {
		this.headerComponent = headerComponent;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public IAbout getAbout() {
		return about;
	}

	public void setAbout(IAbout about) {
		this.about = about;
	}

	public abstract void populateInsertElementMenu(T context, JMenu menu);

	public void populateTemplateFieldsMenu(EditorPanel<? extends EditorPanel<?>> context, TextView textView, JMenu menu) {

		if (templateMetadata != null) {

			AbstractMetadata root = templateMetadata.getTemplateFields();
			if (root != null) {

				if ((root.getName() == null || root.getName().length() == 0) && root instanceof MetadataList) {

					if (((MetadataList) root).getChildren().size() == 1) {

						root = ((MetadataList) root).getChildren().get(0);

						JMenuItem menuItem = createTemplateFieldMenuItem(context, textView, root);
						if (menuItem != null) {
							menu.add(menuItem);
						}

					} else {

						JMenuItem menuItem;
						for (AbstractMetadata abstractMetadata : ((MetadataList) root).getChildren()) {

							menuItem = createTemplateFieldMenuItem(context, textView, abstractMetadata);
							if (menuItem != null) {
								menu.add(menuItem);
							}

						}

					}

				} else {

					JMenuItem menuItem = createTemplateFieldMenuItem(context, textView, root);
					if (menuItem != null) {
						menu.add(menuItem);
					}

				}

			}

		}

	}

	public static JMenu createTemplateFieldListMenu(EditorPanel<? extends EditorPanel<?>> context, TextView textView, MetadataList metadataList) {

		JMenu menu = new JMenu(metadataList.getName());

		if (metadataList instanceof TableMetadata) {
			menu.setIcon(Icons.table);
		}

		JMenuItem menuItem;
		for (AbstractMetadata abstractMetadata : metadataList.getChildren()) {

			menuItem = createTemplateFieldMenuItem(context, textView, abstractMetadata);
			if (menuItem != null) {
				menu.add(menuItem);
			}

		}

		return menu;

	}

	public static JMenuItem createTemplateFieldMenuItem(EditorPanel<? extends EditorPanel<?>> context, TextView textView, AbstractMetadata abstractMetadata) {

		JMenuItem menuItem = null;

		if (abstractMetadata instanceof MetadataList) {
			menuItem = createTemplateFieldListMenu(context, textView, (MetadataList) abstractMetadata);
		} else if (abstractMetadata instanceof FieldMetadata) {
			menuItem = new JMenuItem(new InsertFieldAction(context, textView, FieldType.TEMPLATE.getIdentifier() + ((FieldMetadata) abstractMetadata).getKey(), abstractMetadata.getName()));
		}

		return menuItem;

	}

	public boolean isVisibilityMenuEnabled() {
		return templateMetadata != null && templateMetadata.getVisibilityFields() != null;
	}

	public void populateVisibilityFieldsMenu(EditorPanel<? extends EditorPanel<?>> context, IVisible view, JMenu menu) {

		JMenuItem alwaysMenuItem = new JCheckBoxMenuItem(new SetVisibilityAction(context, view, null, "Always"));
		menu.add(alwaysMenuItem);

		if (templateMetadata != null) {

			AbstractMetadata root = templateMetadata.getVisibilityFields();
			if (root != null) {

				menu.addSeparator();

				if ((root.getName() == null || root.getName().length() == 0) && root instanceof MetadataList) {

					for (AbstractMetadata abstractMetadata : ((MetadataList) root).getChildren()) {

						JMenuItem menuItem = createVisibilityFieldMenuItem(context, view, abstractMetadata);
						if (menuItem != null) {
							menu.add(menuItem);
						}

					}

				} else {

					JMenuItem menuItem = createVisibilityFieldMenuItem(context, view, root);
					if (menuItem != null) {
						menu.add(menuItem);
					}

				}

			}

		}

	}

	public static JMenu createVisibilityFieldListMenu(EditorPanel<? extends EditorPanel<?>> context, IVisible view, MetadataList metadataList) {

		JMenu menu = new JMenu(metadataList.getName());

		JMenuItem menuItem;
		for (AbstractMetadata abstractMetadata : metadataList.getChildren()) {

			menuItem = createVisibilityFieldMenuItem(context, view, abstractMetadata);
			if (menuItem != null) {
				menu.add(menuItem);
			}

		}

		return menu;

	}

	public static JMenuItem createVisibilityFieldMenuItem(EditorPanel<? extends EditorPanel<?>> context, IVisible view, AbstractMetadata abstractMetadata) {

		JMenuItem menuItem = null;

		if (abstractMetadata instanceof MetadataList) {
			menuItem = createVisibilityFieldListMenu(context, view, (MetadataList) abstractMetadata);
		} else if (abstractMetadata instanceof FieldMetadata) {
			menuItem = new JCheckBoxMenuItem(new SetVisibilityAction(context, view, FieldType.TEMPLATE.getIdentifier() + ((FieldMetadata) abstractMetadata).getKey(), abstractMetadata.getName()));
		}

		return menuItem;

	}

}
