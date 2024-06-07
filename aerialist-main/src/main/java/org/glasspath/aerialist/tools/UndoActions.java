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
package org.glasspath.aerialist.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import org.glasspath.aerialist.icons.Icons;
import org.glasspath.common.os.OsUtils;
import org.glasspath.common.swing.undo.IUndoManager;
import org.glasspath.common.swing.undo.DefaultUndoManager.UndoManagerListener;

public class UndoActions {

	public static final int ID_EDIT_ADDED = 0;
	public static final int ID_UNDO_PERFORMED = 1;
	public static final int ID_REDO_PERFORMED = 2;

	private final AbstractAction undoAction;
	private final AbstractAction redoAction;
	private final UndoManagerListener undoManagerListener;

	private UndoManager undoManager = null;

	private List<ActionListener> actionListeners = new ArrayList<>();

	public UndoActions() {

		undoAction = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (undoManager != null) {
					undoManager.undo();
				}
			}
		};
		undoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, OsUtils.CTRL_OR_CMD_MASK));
		undoAction.putValue(Action.SMALL_ICON, Icons.undo);

		redoAction = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (undoManager != null) {
					undoManager.redo();
				}
			}
		};
		redoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, OsUtils.CTRL_OR_CMD_MASK));
		redoAction.putValue(Action.SMALL_ICON, Icons.redo);

		undoManagerListener = new UndoManagerListener() {

			@Override
			public void editAdded(UndoableEdit edit) {
				updateActions();
				fireActionPerformed(ID_EDIT_ADDED, "editAdded"); //$NON-NLS-1$
			}

			@Override
			public void undoPerformed() {
				updateActions();
				fireActionPerformed(ID_UNDO_PERFORMED, "undoPerformed"); //$NON-NLS-1$
			}

			@Override
			public void redoPerformed() {
				updateActions();
				fireActionPerformed(ID_REDO_PERFORMED, "redoPerformed"); //$NON-NLS-1$
			}
		};

		updateActions();

	}

	public AbstractAction getUndoAction() {
		return undoAction;
	}

	public AbstractAction getRedoAction() {
		return redoAction;
	}

	public UndoManager getUndoManager() {
		return undoManager;
	}

	public void setUndoManager(UndoManager undoManager) {

		if (this.undoManager instanceof IUndoManager) {
			((IUndoManager) this.undoManager).removeListener(undoManagerListener);
		}

		this.undoManager = undoManager;

		if (this.undoManager instanceof IUndoManager) {
			((IUndoManager) this.undoManager).addListener(undoManagerListener);
		}

		updateActions();

	}

	public void updateActions() {

		undoAction.setEnabled(undoManager != null && undoManager.canUndo());
		undoAction.putValue(Action.NAME, undoManager != null ? undoManager.getUndoPresentationName() : ""); //$NON-NLS-1$
		undoAction.putValue(Action.SHORT_DESCRIPTION, undoManager != null ? undoManager.getUndoPresentationName() : ""); //$NON-NLS-1$

		redoAction.setEnabled(undoManager != null && undoManager.canRedo());
		redoAction.putValue(Action.NAME, undoManager != null ? undoManager.getRedoPresentationName() : ""); //$NON-NLS-1$
		redoAction.putValue(Action.SHORT_DESCRIPTION, undoManager != null ? undoManager.getRedoPresentationName() : ""); //$NON-NLS-1$

	}

	public void addActionListener(ActionListener listener) {
		actionListeners.add(listener);
	}

	public void removeActionListener(ActionListener listener) {
		actionListeners.remove(listener);
	}

	private void fireActionPerformed(int id, String command) {

		ActionEvent event = new ActionEvent(this, id, command);

		for (ActionListener listener : actionListeners) {
			listener.actionPerformed(event);
		}

	}

}
