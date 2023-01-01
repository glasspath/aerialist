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
package org.glasspath.aerialist.swing.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.undo.UndoableEdit;

import org.glasspath.aerialist.Email;

public abstract class EmailContainer extends JPanel implements ISwingViewContext {

	private LayoutPhase layoutPhase = LayoutPhase.IDLE;
	private ExportPhase exportPhase = ExportPhase.IDLE;

	private TableView tableView = null;

	public EmailContainer(Color background) {

		if (background != null) {
			setBackground(background);
		}
		setLayout(new BorderLayout());

	}

	public void init(Email email) {

		if (email.getTable() != null) {
			tableView = new TableView(this);
			tableView.setVerticalFillEnabled(true);
			tableView.init(email.getTable());
		} else {
			tableView = null;
		}

		loadTableView();

	}

	public Email toEmail() {

		Email email = new Email();

		if (tableView != null) {
			email.setTable(tableView.toElement());
		}

		return email;

	}

	@Override
	public LayoutPhase getLayoutPhase() {
		return layoutPhase;
	}

	@Override
	public void setLayoutPhase(LayoutPhase layoutPhase) {
		this.layoutPhase = layoutPhase;
	}

	@Override
	public boolean isHeightPolicyEnabled() {
		return layoutPhase == LayoutPhase.IDLE || layoutPhase == LayoutPhase.LAYOUT_CONTENT;
	}

	@Override
	public boolean isYPolicyEnabled() {
		return false;
	}

	@Override
	public void setYPolicyEnabled(boolean yPolicyEnabled) {

	}

	@Override
	public ExportPhase getExportPhase() {
		return exportPhase;
	}

	@Override
	public void setExportPhase(ExportPhase exportPhase) {
		this.exportPhase = exportPhase;
	}

	@Override
	public void focusGained(JComponent component) {

	}

	@Override
	public void undoableEditHappened(UndoableEdit edit) {

	}

	@Override
	public void refresh(Component component) {

	}

	public TableView getTableView() {
		return tableView;
	}

	public void setTableView(TableView tableView) {
		this.tableView = tableView;
	}

	public void loadTableView() {

		removeAll();

		if (tableView != null) {
			add(tableView, BorderLayout.CENTER);
		}

	}

}
