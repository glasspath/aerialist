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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
import org.glasspath.aerialist.swing.view.ISwingElementView;
import org.glasspath.aerialist.swing.view.PageView;
import org.glasspath.common.swing.SwingUtils;
import org.glasspath.common.swing.border.BorderButton;
import org.glasspath.common.swing.button.SplitButton;
import org.glasspath.common.swing.color.ColorButton;
import org.glasspath.common.swing.color.ColorUtils;
import org.glasspath.common.swing.selection.SelectionListener;

public class ObjectFormatTools {

	private final DocumentEditorPanel context;
	private final JMenu menu;
	private final JToolBar toolBar;
	private final InsertButton insertButton;

	private boolean updatingTools = false;

	public ObjectFormatTools(DocumentEditorPanel context) {

		this.context = context;
		this.menu = new JMenu("Layout");
		this.toolBar = new JToolBar("Layout");
		toolBar.setRollover(true);
		toolBar.setBackground(ColorUtils.TITLE_BAR_COLOR);

		JToggleButton snapToGridButton = new JToggleButton();
		snapToGridButton.setIcon(Icons.magnet);
		snapToGridButton.setSelected(context.isGridEnabled());
		snapToGridButton.setToolTipText("Snap to grid");
		toolBar.add(snapToGridButton);
		snapToGridButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				context.setGridEnabled(snapToGridButton.isSelected());
			}
		});

		JToggleButton showGridButton = new JToggleButton();
		showGridButton.setIcon(Icons.dotsGrid);
		showGridButton.setSelected(context.isGridVisible());
		showGridButton.setToolTipText("Show grid");
		toolBar.add(showGridButton);
		showGridButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				context.setGridVisible(showGridButton.isSelected());
				context.repaint();
			}
		});

		JToggleButton showGuidesButton = new JToggleButton();
		showGuidesButton.setIcon(Icons.viewAgendaOutline);
		showGuidesButton.setSelected(context.isGuidesVisible());
		showGuidesButton.setToolTipText("Show guides");
		toolBar.add(showGuidesButton);
		showGuidesButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				context.setGuidesVisible(showGuidesButton.isSelected());
				context.repaint();
			}
		});

		JToggleButton layoutLockedButton = new JToggleButton();
		layoutLockedButton.setIcon(Icons.lock);
		layoutLockedButton.setSelected(context.isLayoutLocked());
		layoutLockedButton.setToolTipText("Lock layout");
		toolBar.add(layoutLockedButton);
		layoutLockedButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				context.setLayoutLocked(layoutLockedButton.isSelected());
				context.repaint();
			}
		});

		insertButton = new InsertButton(context);
		insertButton.setToolTipText("Insert");
		toolBar.add(insertButton);

		AlignButton alignButton = new AlignButton(context);
		alignButton.setToolTipText("Align");
		toolBar.add(alignButton);

		toolBar.add(new ArrangeElementsAction(context, ArrangeElementsAction.BRING_FORWARD, true));
		toolBar.add(new ArrangeElementsAction(context, ArrangeElementsAction.BRING_TO_FRONT, true));
		toolBar.add(new ArrangeElementsAction(context, ArrangeElementsAction.SEND_BACKWARD, true));
		toolBar.add(new ArrangeElementsAction(context, ArrangeElementsAction.SEND_TO_BACK, true));

		ColorButton backgroundColorButton = new ColorButton(new SetBackgroundColorAction(context)) {

			@Override
			protected Frame getFrame() {
				return context.getFrame();
			}
		};
		backgroundColorButton.setToolTipText("Background color");
		toolBar.add(backgroundColorButton);

		BorderButton borderButton = new BorderButton(new SetBorderTypeAction(context), new SetBorderWidthAction(context), new SetBorderColorAction(context)) {

			@Override
			protected Frame getFrame() {
				return context.getFrame();
			}
		};
		borderButton.setToolTipText("Border");
		toolBar.add(borderButton);

		BoundsSpinner xSpinner = new BoundsSpinner("x");
		xSpinner.setFont(xSpinner.getFont().deriveFont(10.0F));
		xSpinner.setMaximumSize(new Dimension(100, 50));
		toolBar.add(xSpinner);

		BoundsSpinner ySpinner = new BoundsSpinner("y");
		ySpinner.setFont(ySpinner.getFont().deriveFont(10.0F));
		ySpinner.setMaximumSize(new Dimension(100, 50));
		toolBar.add(ySpinner);

		BoundsSpinner wSpinner = new BoundsSpinner("w");
		wSpinner.setFont(wSpinner.getFont().deriveFont(10.0F));
		wSpinner.setMaximumSize(new Dimension(100, 50));
		toolBar.add(wSpinner);

		BoundsSpinner hSpinner = new BoundsSpinner("h");
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

		JToggleButton yPolicyFixedButton = new JToggleButton(new SetYPolicyAction(context, YPolicy.FIXED, true));
		toolBar.add(yPolicyFixedButton);

		JToggleButton heightPolicyAutoButton = new JToggleButton(new SetHeightPolicyAction(context, HeightPolicy.AUTO, true));
		toolBar.add(heightPolicyAutoButton);

		context.getSelection().addSelectionListener(new SelectionListener() {

			@Override
			public void selectionChanged() {

				updatingTools = true;

				updateInsertButton();

				ISwingElementView<?> elementView = null;
				PageView pageView = null;

				Rectangle bounds = new Rectangle();

				if (context.getSelection().size() == 1) {

					elementView = AerialistUtils.getElementView(context.getSelection().get(0));
					if (elementView != null) {
						bounds = ((JComponent) elementView).getBounds();
					}

					pageView = AerialistUtils.getPageView(context.getSelection().get(0));

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

	public JMenu getMenu() {
		return menu;
	}

	public JToolBar getToolBar() {
		return toolBar;
	}

	private void setSelectionBounds(Object xValue, Object yValue, Object wValue, Object hValue) {

		if (context.getSelection().size() == 1) {

			ISwingElementView<?> elementView = AerialistUtils.getElementView(context.getSelection().get(0));
			if (elementView != null) {

				JComponent component = (JComponent) elementView;

				PageView pageView = AerialistUtils.getPageView(component);
				if (pageView != null) {

					Rectangle oldBounds = component.getBounds();
					Rectangle newBounds = new Rectangle(oldBounds);

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

					context.undoableEditHappened(new ResizeUndoable(context, component, pageView, oldBounds, newBounds, context.getPageContainer().isYPolicyEnabled()));
					context.refresh(pageView);

				}

			}

		}

	}

	public void updateInsertButton() {

		boolean editingHeader = context.getPageContainer().isEditingHeader();
		boolean editingFooter = context.getPageContainer().isEditingFooter();

		insertButton.setPopupMenu(ActionUtils.createInsertElementMenu(context, !editingHeader && !editingFooter).getPopupMenu());

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
