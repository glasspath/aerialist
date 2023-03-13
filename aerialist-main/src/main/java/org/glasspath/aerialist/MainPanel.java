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
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.undo.UndoableEdit;

import org.glasspath.aerialist.editor.DocumentEditorContext;
import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.editor.DocumentSourceEditorPanel;
import org.glasspath.aerialist.editor.EditorPanel;
import org.glasspath.aerialist.editor.actions.EditSourceUndoable;
import org.glasspath.aerialist.swing.view.PageView;
import org.glasspath.common.swing.undo.DefaultUndoManager.UndoManagerListener;
import org.glasspath.common.xml.XmlUtils;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class MainPanel extends AbstractMainPanel<Aerialist> {

	private final DocumentEditorPanel documentEditor;
	private DocumentSourceEditorPanel sourceEditor = null; // Takes some time to create, so for now only create it when it is actually used
	private final XmlMapper xmlMapper;

	private boolean awtEventListenerInstalled = false;
	private boolean designChanged = true; // Initialized at true to make sure source is updated first time

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

		documentEditor.getUndoManager().addListener(new UndoManagerListener() {

			@Override
			public void undoPerformed() {
				designChanged = true;
			}

			@Override
			public void redoPerformed() {
				designChanged = true;
			}

			@Override
			public void editAdded(UndoableEdit edit) {
				designChanged = true;
			}
		});

	}

	public DocumentEditorPanel getDocumentEditor() {
		return documentEditor;
	}

	public void windowActivated(WindowEvent e) {
		if (viewMode == VIEW_MODE_DESIGN) {
			installAwtEventListener();
		}
	}

	public void windowDeactivated(WindowEvent e) {
		uninstallAwtEventListener();
	}

	private void installAwtEventListener() {
		if (!awtEventListenerInstalled) {
			Toolkit.getDefaultToolkit().addAWTEventListener(documentEditor, EditorPanel.MOUSE_EVENTS_MASK);
			awtEventListenerInstalled = true;
		}
	}

	private void uninstallAwtEventListener() {
		if (awtEventListenerInstalled) {
			Toolkit.getDefaultToolkit().removeAWTEventListener(documentEditor);
			awtEventListenerInstalled = false;
		}
	}

	public void setViewMode(int viewMode) {

		if (this.viewMode != viewMode) {

			removeAll();

			if (sourceEditor == null) {
				sourceEditor = new DocumentSourceEditorPanel(context);
			}

			if (viewMode == VIEW_MODE_DESIGN) {

				installAwtEventListener();

				updateDocumentEditor();

				add(documentEditor, BorderLayout.CENTER);
				context.getUndoActions().setUndoManager(documentEditor.getUndoManager());

			} else if (viewMode == VIEW_MODE_SOURCE) {

				uninstallAwtEventListener();

				updateSourceEditor();

				add(sourceEditor, BorderLayout.CENTER);
				context.getUndoActions().setUndoManager(sourceEditor.getUndoManager());
				sourceEditor.sourceEditorShown();

			}

			invalidate();
			revalidate();
			repaint();

		}

		super.setViewMode(viewMode);

		context.updateToolBars();

	}

	public void updateDocumentEditor() {

		if (sourceEditor.isSourceChanged()) {

			List<PageView> oldPageViews = new ArrayList<>();
			List<PageView> newPageViews = new ArrayList<>();

			oldPageViews.addAll(documentEditor.getPageContainer().getPageViews());

			try {
				Document document = xmlMapper.readValue(sourceEditor.getSource(), Document.class);
				documentEditor.getPageContainer().init(document);
			} catch (Exception e) {
				e.printStackTrace();
			}

			newPageViews.addAll(documentEditor.getPageContainer().getPageViews());

			documentEditor.getUndoManager().addEdit(new EditSourceUndoable(documentEditor, oldPageViews, newPageViews));

			sourceEditor.setSourceChanged(false);
			designChanged = false;

		}

	}

	private void updateSourceEditor() {

		if (designChanged) {

			Document document = documentEditor.getPageContainer().toDocument();

			try {
				sourceEditor.setSource(xmlMapper.writeValueAsString(document));
			} catch (Exception e) {
				e.printStackTrace();
			}

			sourceEditor.setSourceChanged(false);
			designChanged = false;

		}

	}

	public void updateEditMenu() {
		documentEditor.populateEditMenu(context.getEditTools().prepareMenu());
		context.getEditTools().finishMenu();
	}

	public void clearSearch() {
		if (viewMode == VIEW_MODE_SOURCE) {
			sourceEditor.clearSearch();
		}
	}

	public void searchNext(String text) {
		if (viewMode == VIEW_MODE_DESIGN) {
			documentEditor.searchNext(text);
		} else if (viewMode == VIEW_MODE_SOURCE) {
			sourceEditor.searchNext(text);
		}
	}

	public void searchPrevious(String text) {
		if (viewMode == VIEW_MODE_DESIGN) {
			documentEditor.searchPrevious(text);
		} else if (viewMode == VIEW_MODE_SOURCE) {
			sourceEditor.searchPrevious(text);
		}
	}

}
