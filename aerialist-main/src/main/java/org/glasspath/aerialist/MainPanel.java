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
package org.glasspath.aerialist;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

import org.glasspath.aerialist.editor.DocumentEditorContext;
import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.editor.DocumentSourceEditorPanel;
import org.glasspath.aerialist.editor.actions.EditSourceUndoable;
import org.glasspath.aerialist.swing.view.PageView;
import org.glasspath.common.xml.XmlUtils;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class MainPanel extends AbstractMainPanel<Aerialist> {

	private final DocumentEditorPanel documentEditor;
	private DocumentSourceEditorPanel sourceEditor = null; // Takes some time to create, so for now only create it when it is actually used
	private final XmlMapper xmlMapper;

	public MainPanel(Aerialist context, DocumentEditorContext editorContext) {

		super(context);

		setLayout(new BorderLayout());

		documentEditor = new DocumentEditorPanel(context, editorContext) {

			@Override
			public Frame getFrame() {
				return context.getFrame();
			}
		};

		xmlMapper = XmlUtils.createXmlMapper();

		add(documentEditor, BorderLayout.CENTER);

	}

	public DocumentEditorPanel getDocumentEditor() {
		return documentEditor;
	}

	public void setViewMode(int viewMode) {

		if (this.viewMode != viewMode) {

			removeAll();

			if (sourceEditor == null) {
				sourceEditor = new DocumentSourceEditorPanel();
			}

			if (viewMode == VIEW_MODE_DESIGN) {

				if (sourceEditor.isSourceChanged()) {

					List<PageView> oldPageViews = new ArrayList<>();
					List<PageView> newPageViews = new ArrayList<>();

					oldPageViews.addAll(documentEditor.getPageContainer().getPageViews());

					updateDocumentEditor();

					newPageViews.addAll(documentEditor.getPageContainer().getPageViews());

					documentEditor.getUndoManager().addEdit(new EditSourceUndoable(documentEditor, oldPageViews, newPageViews));

				}

				add(documentEditor, BorderLayout.CENTER);
				context.getUndoActions().setUndoManager(documentEditor.getUndoManager());

			} else if (viewMode == VIEW_MODE_SOURCE) {

				// TODO: Check if something changed
				updateSourceEditor();

				add(sourceEditor, BorderLayout.CENTER);
				context.getUndoActions().setUndoManager(sourceEditor.getUndoManager());

			}

			invalidate();
			revalidate();
			repaint();

		}

		super.setViewMode(viewMode);

		context.showTools(null); // TODO

	}

	private void updateDocumentEditor() {

		try {
			Document document = xmlMapper.readValue(sourceEditor.getSource(), Document.class);
			documentEditor.getPageContainer().init(document);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void updateSourceEditor() {

		Document document = documentEditor.getPageContainer().toDocument();

		try {
			sourceEditor.setSource(xmlMapper.writeValueAsString(document));
			sourceEditor.setSourceChanged(false);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void updateEditMenu() {
		documentEditor.populateEditMenu(context.getEditTools().prepareMenu());
		context.getEditTools().finishMenu();
	}

}
