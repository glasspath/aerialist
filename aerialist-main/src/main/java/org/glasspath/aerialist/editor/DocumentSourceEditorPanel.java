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

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.RUndoManager;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.glasspath.common.swing.FrameContext;
import org.glasspath.common.swing.color.ColorUtils;
import org.glasspath.common.swing.theme.Theme;
import org.glasspath.common.swing.undo.DefaultUndoManager.UndoManagerListener;
import org.glasspath.common.swing.undo.IUndoManager;

public class DocumentSourceEditorPanel extends JPanel {

	private final RSyntaxTextArea textArea;

	private UndoManager undoManager = null;
	private boolean sourceChanged = false;
	private boolean updatingSource = false;

	private boolean inited = false;

	public DocumentSourceEditorPanel(FrameContext context) {

		setLayout(new BorderLayout());

		textArea = new RSyntaxTextArea(20, 60) {

			@Override
			protected JPopupMenu createPopupMenu() {

				JPopupMenu menu = super.createPopupMenu();
				
				return menu;

			}

			@Override
			protected RUndoManager createUndoManager() {
				undoManager = new UndoManager(this);
				return undoManager;
			}
		};
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
		textArea.setCodeFoldingEnabled(true);

		try {

			final org.fife.ui.rsyntaxtextarea.Theme theme;
			if (Theme.isDark()) {
				theme = org.fife.ui.rsyntaxtextarea.Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml")); //$NON-NLS-1$
			} else {
				theme = org.fife.ui.rsyntaxtextarea.Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/idea.xml")); //$NON-NLS-1$
			}

			theme.apply(textArea);

		} catch (IOException e) {
			e.printStackTrace();
		}

		textArea.setCurrentLineHighlightColor(ColorUtils.createTransparentColor(ColorUtils.SELECTION_COLOR_FOCUSSED, Theme.isDark() ? 75 : 175));

		final RTextScrollPane textAreaScrollPane = new RTextScrollPane(textArea);
		textAreaScrollPane.setBorder(BorderFactory.createEmptyBorder());
		add(textAreaScrollPane, BorderLayout.CENTER);

		textArea.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				setSourceChanged();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				setSourceChanged();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				setSourceChanged();
			}

			private void setSourceChanged() {
				if (!updatingSource) {
					sourceChanged = true;
					context.setContentChanged(true);
				}
			}
		});

	}

	public RSyntaxTextArea getTextArea() {
		return textArea;
	}

	public UndoManager getUndoManager() {
		return undoManager;
	}

	public void setSource(String source) {

		updatingSource = true;

		if (source != null && !source.equals(textArea.getText())) {

			if (undoManager != null) {
				undoManager.beginInternalAtomicEdit();
			}

			textArea.setText(source);

			if (undoManager != null) {

				undoManager.endInternalAtomicEdit();

				if (!inited) {
					undoManager.discardAllEdits();
				}

			}

		}

		updatingSource = false;

	}

	public void sourceEditorShown() {

		if (!inited) {

			textArea.setCaretPosition(0);

			inited = true;

		}

		textArea.requestFocusInWindow();

	}

	public String getSource() {
		return textArea.getText();
	}

	public boolean isSourceChanged() {
		return sourceChanged;
	}

	public void setSourceChanged(boolean sourceChanged) {
		this.sourceChanged = sourceChanged;
	}

	public void clearSearch() {
		search("", false); //$NON-NLS-1$
	}

	public void searchNext(String text) {
		search(text, false);
	}

	public void searchPrevious(String text) {
		search(text, true);
	}

	private void search(String text, boolean reverse) {

		if (text != null) {

			SearchContext context = new SearchContext();
			context.setSearchFor(text);
			context.setMatchCase(false);
			context.setSearchForward(!reverse);
			context.setRegularExpression(false);
			context.setWholeWord(false);
			context.setMarkAll(true);

			SearchEngine.find(textArea, context);

		}

	}

	public static class UndoManager extends RUndoManager implements IUndoManager {

		private final List<UndoManagerListener> listeners = new ArrayList<>();

		public UndoManager(RSyntaxTextArea textArea) {
			super(textArea);
		}

		@Override
		public synchronized boolean addEdit(UndoableEdit edit) {
			boolean result = super.addEdit(edit);

			for (UndoManagerListener listener : listeners) {
				listener.editAdded(edit);
			}

			return result;

		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();

			for (UndoManagerListener listener : listeners) {
				listener.undoPerformed();
			}

		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();

			for (UndoManagerListener listener : listeners) {
				listener.redoPerformed();
			}

		}

		@Override
		public void addListener(UndoManagerListener listener) {
			listeners.add(listener);
		}

		@Override
		public void removeListener(UndoManagerListener listener) {
			listeners.remove(listener);
		}

	}

}
