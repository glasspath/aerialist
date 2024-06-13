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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.glasspath.aerialist.Aerialist;
import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.HeightPolicy;
import org.glasspath.aerialist.YPolicy;
import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.editor.ResizeUndoable;
import org.glasspath.aerialist.editor.actions.ActionUtils;
import org.glasspath.aerialist.editor.actions.AlignElementsAction;
import org.glasspath.aerialist.editor.actions.ArrangeElementsAction;
import org.glasspath.aerialist.editor.actions.SetBackgroundColorAction;
import org.glasspath.aerialist.editor.actions.SetBorderColorAction;
import org.glasspath.aerialist.editor.actions.SetBorderTypeAction;
import org.glasspath.aerialist.editor.actions.SetBorderWidthAction;
import org.glasspath.aerialist.editor.actions.SetHeightPolicyAction;
import org.glasspath.aerialist.editor.actions.SetYPolicyAction;
import org.glasspath.aerialist.icons.Icons;
import org.glasspath.aerialist.resources.AerialistResources;
import org.glasspath.aerialist.swing.view.ISwingElementView;
import org.glasspath.aerialist.swing.view.PageView;
import org.glasspath.common.swing.SwingUtils;
import org.glasspath.common.swing.border.BorderButton;
import org.glasspath.common.swing.button.SplitButton;
import org.glasspath.common.swing.color.ColorButton;
import org.glasspath.common.swing.resources.CommonResources;
import org.glasspath.common.swing.selection.SelectionListener;
import org.glasspath.common.swing.tools.AbstractTools;

public class ObjectFormatTools extends AbstractTools<Aerialist> {

	private final DocumentEditorPanel editor;
	private final InsertButton insertButton;

	private boolean updatingTools = false;

	public ObjectFormatTools(Aerialist context, DocumentEditorPanel editor) {
		super(context, AerialistResources.getString("Layout")); //$NON-NLS-1$

		this.editor = editor;

		JToggleButton snapToGridButton = new JToggleButton();
		snapToGridButton.setIcon(Icons.magnet);
		snapToGridButton.setSelected(editor.isGridEnabled());
		snapToGridButton.setToolTipText(AerialistResources.getString("SnapToGrid")); //$NON-NLS-1$
		toolBar.add(snapToGridButton);
		snapToGridButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				editor.setGridEnabled(snapToGridButton.isSelected());
			}
		});

		JToggleButton showGridButton = new JToggleButton();
		showGridButton.setIcon(Icons.dotsGrid);
		showGridButton.setSelected(editor.isGridVisible());
		showGridButton.setToolTipText(AerialistResources.getString("ShowGrid")); //$NON-NLS-1$
		toolBar.add(showGridButton);
		showGridButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				editor.setGridVisible(showGridButton.isSelected());
				editor.repaint();
			}
		});

		JToggleButton showGuidesButton = new JToggleButton();
		showGuidesButton.setIcon(Icons.viewAgendaOutline);
		showGuidesButton.setSelected(editor.isGuidesVisible());
		showGuidesButton.setToolTipText(AerialistResources.getString("ShowGuides")); //$NON-NLS-1$
		toolBar.add(showGuidesButton);
		showGuidesButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				editor.setGuidesVisible(showGuidesButton.isSelected());
				editor.repaint();
			}
		});

		JToggleButton layoutLockedButton = new JToggleButton();
		layoutLockedButton.setIcon(Icons.lock);
		layoutLockedButton.setSelected(editor.isLayoutLocked());
		layoutLockedButton.setToolTipText(AerialistResources.getString("LockLayout")); //$NON-NLS-1$
		toolBar.add(layoutLockedButton);
		layoutLockedButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				editor.setLayoutLocked(layoutLockedButton.isSelected());
				editor.repaint();
			}
		});

		insertButton = new InsertButton(editor);
		insertButton.setToolTipText(CommonResources.getString("Insert")); //$NON-NLS-1$
		toolBar.add(insertButton);

		AlignButton alignButton = new AlignButton(editor);
		alignButton.setToolTipText(AerialistResources.getString("Align")); //$NON-NLS-1$
		toolBar.add(alignButton);

		toolBar.add(new ArrangeElementsAction(editor, ArrangeElementsAction.BRING_FORWARD, true));
		toolBar.add(new ArrangeElementsAction(editor, ArrangeElementsAction.BRING_TO_FRONT, true));
		toolBar.add(new ArrangeElementsAction(editor, ArrangeElementsAction.SEND_BACKWARD, true));
		toolBar.add(new ArrangeElementsAction(editor, ArrangeElementsAction.SEND_TO_BACK, true));

		ColorButton backgroundColorButton = new ColorButton(new SetBackgroundColorAction(editor)) {

			@Override
			protected Frame getFrame() {
				return editor.getFrameContext().getFrame();
			}
		};
		backgroundColorButton.setToolTipText(AerialistResources.getString("BackgroundColor")); //$NON-NLS-1$
		toolBar.add(backgroundColorButton);

		BorderButton borderButton = new BorderButton(new SetBorderTypeAction(editor), new SetBorderWidthAction(editor), new SetBorderColorAction(editor)) {

			@Override
			protected Frame getFrame() {
				return editor.getFrameContext().getFrame();
			}
		};
		borderButton.setToolTipText(CommonResources.getString("Border")); //$NON-NLS-1$
		toolBar.add(borderButton);

		BoundsSpinner xSpinner = new BoundsSpinner(AerialistResources.getString("layoutX")); //$NON-NLS-1$
		xSpinner.setFont(xSpinner.getFont().deriveFont(10.0F));
		xSpinner.setMaximumSize(new Dimension(100, 50));
		toolBar.add(xSpinner);

		BoundsSpinner ySpinner = new BoundsSpinner(AerialistResources.getString("layoutY")); //$NON-NLS-1$
		ySpinner.setFont(ySpinner.getFont().deriveFont(10.0F));
		ySpinner.setMaximumSize(new Dimension(100, 50));
		toolBar.add(ySpinner);

		BoundsSpinner wSpinner = new BoundsSpinner(AerialistResources.getString("layoutW")); //$NON-NLS-1$
		wSpinner.setFont(wSpinner.getFont().deriveFont(10.0F));
		wSpinner.setMaximumSize(new Dimension(100, 50));
		toolBar.add(wSpinner);

		BoundsSpinner hSpinner = new BoundsSpinner(AerialistResources.getString("layoutH")); //$NON-NLS-1$
		hSpinner.setFont(hSpinner.getFont().deriveFont(10.0F));
		hSpinner.setMaximumSize(new Dimension(100, 50));
		toolBar.add(hSpinner);

		ChangeListener changeListener = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if (!updatingTools) {
					setSelectionBounds(xSpinner.getValue(), ySpinner.getValue(), wSpinner.getValue(), hSpinner.getValue());
				}
			}
		};
		xSpinner.addChangeListener(changeListener);
		ySpinner.addChangeListener(changeListener);
		wSpinner.addChangeListener(changeListener);
		hSpinner.addChangeListener(changeListener);

		JToggleButton yPolicyFixedButton = new JToggleButton(new SetYPolicyAction(editor, YPolicy.FIXED, true));
		toolBar.add(yPolicyFixedButton);

		JToggleButton heightPolicyAutoButton = new JToggleButton(new SetHeightPolicyAction(editor, HeightPolicy.AUTO, true));
		toolBar.add(heightPolicyAutoButton);

		editor.getSelection().addSelectionListener(new SelectionListener() {

			@Override
			public void selectionChanged() {

				updatingTools = true;

				updateInsertButton();

				ISwingElementView<?> elementView = null;
				PageView pageView = null;

				Rectangle bounds = new Rectangle();

				if (editor.getSelection().size() == 1) {

					elementView = AerialistUtils.getElementView(editor.getSelection().get(0));
					if (elementView != null) {
						bounds = ((JComponent) elementView).getBounds();
					}

					pageView = AerialistUtils.getPageView(editor.getSelection().get(0));

				}

				insertButton.setEnabled(pageView != null);

				xSpinner.setValue(bounds.x);
				ySpinner.setValue(bounds.y);
				wSpinner.setValue(bounds.width);
				hSpinner.setValue(bounds.height);

				if (elementView != null) {

					yPolicyFixedButton.setSelected(elementView.getYPolicy() == YPolicy.FIXED);
					heightPolicyAutoButton.setSelected(elementView.getHeightPolicy() == HeightPolicy.AUTO);

				} else {

					yPolicyFixedButton.setSelected(false);
					heightPolicyAutoButton.setSelected(false);

				}

				updatingTools = false;

			}
		});

	}

	private void setSelectionBounds(Object xValue, Object yValue, Object wValue, Object hValue) {

		if (editor.getSelection().size() == 1) {

			ISwingElementView<?> elementView = AerialistUtils.getElementView(editor.getSelection().get(0));
			if (elementView != null) {

				JComponent component = (JComponent) elementView;

				PageView pageView = AerialistUtils.getPageView(component);
				if (pageView != null) {

					Rectangle oldBounds = component.getBounds();
					Rectangle newBounds = new Rectangle(oldBounds);
					Map<Component, Rectangle> anchoredElementBounds = AerialistUtils.getAnchoredElementBounds(component);

					if (xValue instanceof Number) {
						newBounds.x = ((Number) xValue).intValue();
					}
					if (yValue instanceof Number) {
						newBounds.y = ((Number) yValue).intValue();
					}
					if (wValue instanceof Number) {
						newBounds.width = ((Number) wValue).intValue();
					}
					if (hValue instanceof Number) {
						newBounds.height = ((Number) hValue).intValue();
					}

					component.setBounds(newBounds);

					pageView.elementResized(component, oldBounds);

					editor.undoableEditHappened(new ResizeUndoable(editor, component, pageView, oldBounds, newBounds, anchoredElementBounds));
					editor.refresh(pageView);

				}

			}

		}

	}

	public void updateInsertButton() {

		boolean editingHeader = editor.getPageContainer().isEditingHeader();
		boolean editingFooter = editor.getPageContainer().isEditingFooter();

		insertButton.setPopupMenu(ActionUtils.createInsertElementMenu(editor, !editingHeader && !editingFooter).getPopupMenu());

	}

	public static class InsertButton extends SplitButton {

		public InsertButton(DocumentEditorPanel editor) {

			configureForToolBar();
			setArrowOffset(-3);
			setIcon(Icons.fileDocumentPlus);

			boolean editingHeader = editor.getPageContainer().isEditingHeader();
			boolean editingFooter = editor.getPageContainer().isEditingFooter();

			setPopupMenu(ActionUtils.createInsertElementMenu(editor, !editingHeader && !editingFooter).getPopupMenu());

		}

	}

	public static class AlignButton extends SplitButton {

		public AlignButton(DocumentEditorPanel editor) {

			configureForToolBar();
			setArrowOffset(-3);
			setIcon(Icons.alignHorizontalLeft);

			JMenu alignMenu = new JMenu();

			alignMenu.add(new AlignElementsAction(editor, AlignElementsAction.HORIZONTAL_LEFT));
			alignMenu.add(new AlignElementsAction(editor, AlignElementsAction.HORIZONTAL_CENTER));
			alignMenu.add(new AlignElementsAction(editor, AlignElementsAction.HORIZONTAL_RIGHT));

			alignMenu.addSeparator();

			alignMenu.add(new AlignElementsAction(editor, AlignElementsAction.VERTICAL_TOP));
			alignMenu.add(new AlignElementsAction(editor, AlignElementsAction.VERTICAL_CENTER));
			alignMenu.add(new AlignElementsAction(editor, AlignElementsAction.VERTICAL_BOTTOM));

			setPopupMenu(alignMenu.getPopupMenu());

		}

	}

	public static class BoundsSpinner extends JSpinner {

		private final String text;

		public BoundsSpinner(String text) {
			this.text = text;
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			g.setColor(Color.gray);
			SwingUtils.drawString(this, (Graphics2D) g, text, 8, getHeight() - 9); // TODO

		}

	}
}
