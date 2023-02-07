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
package org.glasspath.aerialist.editor.actions;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;

import org.glasspath.aerialist.Aerialist;
import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.Alignment;
import org.glasspath.aerialist.Field.DynamicFieldKey;
import org.glasspath.aerialist.Field.FieldType;
import org.glasspath.aerialist.FitPolicy;
import org.glasspath.aerialist.HeightPolicy;
import org.glasspath.aerialist.IVisible;
import org.glasspath.aerialist.Padding;
import org.glasspath.aerialist.Page.PageSize;
import org.glasspath.aerialist.YPolicy;
import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.editor.EditorPanel;
import org.glasspath.aerialist.editor.ElementData;
import org.glasspath.aerialist.icons.Icons;
import org.glasspath.aerialist.swing.view.FieldUtils;
import org.glasspath.aerialist.swing.view.GroupView;
import org.glasspath.aerialist.swing.view.ISwingElementView;
import org.glasspath.aerialist.swing.view.ImageView;
import org.glasspath.aerialist.swing.view.PageContainer;
import org.glasspath.aerialist.swing.view.PageView;
import org.glasspath.aerialist.swing.view.TableCellView;
import org.glasspath.aerialist.swing.view.TableView;
import org.glasspath.aerialist.swing.view.TextView;
import org.glasspath.common.swing.border.BorderMenu;
import org.glasspath.common.swing.color.ColorChooserPanel;
import org.glasspath.common.swing.file.chooser.FileChooser;
import org.glasspath.common.swing.padding.PaddingDialog;
import org.glasspath.common.swing.padding.PaddingPanel;
import org.glasspath.common.swing.selection.SelectionListener;

public class ActionUtils {

	public static boolean TODO_CREATE_GROUP_MENU_ITEMS = false;

	private ActionUtils() {

	}

	public static void populateMenu(JMenu menu, EditorPanel<? extends EditorPanel<?>> context, List<Component> selection) {

		// TODO: Support deleting of selection across pages

		PageView pageView = null;
		List<Component> elementViews = new ArrayList<>();

		for (Component component : selection) {

			Component element = AerialistUtils.getElementViewAsComponent(component);
			if (element instanceof ISwingElementView<?> && element.getParent() instanceof PageView) {
				if (pageView == null || element.getParent() == pageView) {
					elementViews.add(element);
					pageView = (PageView) element.getParent();
				}
			}

		}

		if (elementViews.size() > 0) {

			if (pageView != null) {
				menu.add(new DeleteElementsAction(context, (PageView) pageView, elementViews));
			}

			menu.addSeparator();

			menu.add(createArrangeMenu(context));
			menu.add(createAlignMenu(context));

			if (context instanceof DocumentEditorPanel && TODO_CREATE_GROUP_MENU_ITEMS) {

				menu.addSeparator();

				menu.add(new GroupElementsAction((DocumentEditorPanel) context, pageView, elementViews));
				menu.add(new UngroupElementsAction((DocumentEditorPanel) context, pageView, elementViews));

			}

		}

	}

	public static void populateMenu(JMenu menu, DocumentEditorPanel context, Component component) {

		if (component instanceof PageContainer) {

			populatePageContainerMenu(context, menu);

		} else if (component instanceof PageView) {

			populatePageViewMenu(context, (PageView) component, menu);

		} else {

			Component element = AerialistUtils.getElementViewAsComponent(component);
			if (element instanceof ISwingElementView<?> && element.getParent() instanceof PageView) {

				ISwingElementView<?> elementView = (ISwingElementView<?>) element;
				PageView pageView = (PageView) element.getParent();

				if (component instanceof TableCellView) {
					populateTableCellViewMenu(context, (TableCellView) component, menu);
					menu.addSeparator();
				} else if (component instanceof TextView) {
					populateTextViewMenu(context, (TextView) component, menu);
					menu.addSeparator();
				} else if (component instanceof ImageView) {
					populateImageViewMenu(context, (ImageView) component, menu);
					menu.addSeparator();
				}

				menu.add(createLayoutMenu(context, elementView, pageView));
				menu.add(createArrangeMenu(context));

				menu.addSeparator();

				menu.add(createBackgroundColorMenu(context.getFrame(), elementView.getBackgroundColor(), new SetBackgroundColorAction(context)));
				if (elementView instanceof TableView) {
					menu.add(createRowColorsMenu(context));
				}
				menu.add(new BorderMenu(new SetBorderTypeAction(context), new SetBorderWidthAction(context), new SetBorderColorAction(context)) {

					@Override
					protected Frame getFrame() {
						return context.getFrame();
					}
				});
				if (SetPaddingAction.isPaddingSupported(elementView)) {
					menu.add(createPaddingMenu(context, elementView));
				}

				GroupView groupView = AerialistUtils.getGroupView(component);
				if (groupView != null && TODO_CREATE_GROUP_MENU_ITEMS) {

					List<Component> elementViews = new ArrayList<>();
					for (int i = 0; i < groupView.getComponentCount(); i++) {
						elementViews.add(groupView.getComponent(i));
					}

					menu.addSeparator();
					menu.add(new UngroupElementsAction((DocumentEditorPanel) context, pageView, elementViews));

				}

				menu.addSeparator();

				// TODO
				// JMenuItem deleteMenuItem = new JMenuItem(Resources.getString("Delete") + " " + elementView.getElementDesctiption()); //$NON-NLS-1$ //$NON-NLS-2$
				// menu.add(deleteMenuItem);
				menu.add(new DeleteElementsAction(context, (PageView) element.getParent(), element));

			}

		}

	}

	private static void populatePageContainerMenu(DocumentEditorPanel context, JMenu menu) {

		menu.add(new InsertPageAction(context, AerialistUtils.createDefaultPage(), InsertPageAction.INSERT));

	}

	private static void populatePageViewMenu(DocumentEditorPanel context, PageView pageView, JMenu menu) {

		menu.add(createInsertElementMenu(context));
		menu.add(createPageSizeMenu(context, (PageView) pageView));

		menu.addSeparator();

		if (context.getPageContainer().isEditingHeader()) {

			JMenuItem finishEditingHeaderMenuItem = new JMenuItem("Finish editing page header");
			menu.add(finishEditingHeaderMenuItem);
			finishEditingHeaderMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					context.getPageContainer().stopEditingHeaderView();
				}
			});

		} else if (context.getPageContainer().isEditingFooter()) {

			JMenuItem finishEditingFooterMenuItem = new JMenuItem("Finish editing page footer");
			menu.add(finishEditingFooterMenuItem);
			finishEditingFooterMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					context.getPageContainer().stopEditingFooterView();
				}
			});

		} else {

			JMenuItem editHeaderMenuItem = new JMenuItem("Edit page header");
			menu.add(editHeaderMenuItem);
			editHeaderMenuItem.setIcon(Icons.pageLayoutHeader);
			editHeaderMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					context.getPageContainer().editHeaderView(pageView);
				}
			});

			JMenuItem editFooterMenuItem = new JMenuItem("Edit page footer");
			menu.add(editFooterMenuItem);
			editFooterMenuItem.setIcon(Icons.pageLayoutFooter);
			editFooterMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					context.getPageContainer().editFooterView(pageView);
				}
			});

		}

		menu.addSeparator();

		JMenuItem updateFieldsMenuItem = new JMenuItem("Update fields");
		menu.add(updateFieldsMenuItem);
		updateFieldsMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				FieldUtils.updateDynamicFields(context.getPageContainer());
			}
		});

		JMenu visibilityMenu = createVisibilityMenu(context, pageView);
		if (visibilityMenu != null) {
			menu.addSeparator();
			menu.add(visibilityMenu);
		}

		menu.addSeparator();

		menu.add(new DeletePageAction(context, pageView));

	}

	public static JMenu createPageSizeMenu(DocumentEditorPanel context, PageView pageView) {

		JMenu menu = new JMenu("Page size");

		menu.add(new SetPageSizeAction(context, pageView, PageSize.A4.getWidth(), PageSize.A4.getHeight(), "A4 Portrait"));
		menu.add(new SetPageSizeAction(context, pageView, PageSize.A4.getHeight(), PageSize.A4.getWidth(), "A4 Landscape"));

		return menu;

	}

	public static JMenu createInsertElementMenu(DocumentEditorPanel context) {

		JMenu menu = new JMenu("Insert");

		if (context.getSelection().size() == 1 && context.getSelection().get(0) instanceof PageView) {

			PageView pageView = (PageView) context.getSelection().get(0);

			menu.add(new InsertPageAction(context, AerialistUtils.createDefaultPage(pageView.getWidth(), pageView.getHeight()), InsertPageAction.ABOVE));
			menu.add(new InsertPageAction(context, AerialistUtils.createDefaultPage(pageView.getWidth(), pageView.getHeight()), InsertPageAction.BELOW));

		} else {
			menu.add(new InsertPageAction(context, AerialistUtils.createDefaultPage(), InsertPageAction.INSERT));
		}

		if (context.getEditorContext() != null) {
			context.getEditorContext().populateInsertElementMenu(context, menu);
		}

		menu.addSeparator();

		menu.add(new InsertElementAction(context, AerialistUtils.createDefaultTextBox(), "Insert text box", Icons.textBoxPlus));
		menu.add(new InsertElementAction(context, AerialistUtils.createDefaultTable(), "Insert table", Icons.tableLargePlus));
		menu.add(new InsertElementAction(context, AerialistUtils.createDefaultImage(), "Insert image", Icons.imagePlus));
		menu.add(new InsertElementAction(context, AerialistUtils.createDefaultQrCode(), "Insert QR code", Icons.qrcodePlus));

		return menu;

	}

	public static JMenu createArrangeMenu(EditorPanel<? extends EditorPanel<?>> context) {

		JMenu menu = new JMenu("Arrange");

		menu.add(new ArrangeElementsAction(context, ArrangeElementsAction.BRING_FORWARD, false));
		menu.add(new ArrangeElementsAction(context, ArrangeElementsAction.BRING_TO_FRONT, false));
		menu.add(new ArrangeElementsAction(context, ArrangeElementsAction.SEND_BACKWARD, false));
		menu.add(new ArrangeElementsAction(context, ArrangeElementsAction.SEND_TO_BACK, false));

		return menu;

	}

	public static JMenu createAlignMenu(EditorPanel<? extends EditorPanel<?>> context) {

		JMenu menu = new JMenu("Align");

		menu.add(new AlignElementsAction(context, AlignElementsAction.HORIZONTAL_LEFT));
		menu.add(new AlignElementsAction(context, AlignElementsAction.HORIZONTAL_CENTER));
		menu.add(new AlignElementsAction(context, AlignElementsAction.HORIZONTAL_RIGHT));

		menu.addSeparator();

		menu.add(new AlignElementsAction(context, AlignElementsAction.VERTICAL_TOP));
		menu.add(new AlignElementsAction(context, AlignElementsAction.VERTICAL_CENTER));
		menu.add(new AlignElementsAction(context, AlignElementsAction.VERTICAL_BOTTOM));

		return menu;

	}

	public static JMenu createInsertFieldMenu(EditorPanel<? extends EditorPanel<?>> context) {
		return createInsertFieldMenu(context, null);
	}

	public static JMenu createInsertFieldMenu(EditorPanel<? extends EditorPanel<?>> context, TextView textView) {

		JMenu menu = new JMenu("Insert field");

		JMenu generalMenu = new JMenu("General");
		menu.add(generalMenu);

		JMenu documentMenu = new JMenu("Document");
		generalMenu.add(documentMenu);

		documentMenu.add(new InsertFieldAction(context, textView, FieldType.DYNAMIC.getIdentifier() + DynamicFieldKey.PAGE, DynamicFieldKey.PAGE.getDescription()));
		documentMenu.add(new InsertFieldAction(context, textView, FieldType.DYNAMIC.getIdentifier() + DynamicFieldKey.PAGES, DynamicFieldKey.PAGES.getDescription()));

		JMenu dateAndTimeMenu = new JMenu("Date & time");
		generalMenu.add(dateAndTimeMenu);

		dateAndTimeMenu.add(new InsertFieldAction(context, textView, FieldType.DYNAMIC.getIdentifier() + DynamicFieldKey.MILLIS, DynamicFieldKey.MILLIS.getDescription()));

		if (context.getEditorContext() != null) {
			context.getEditorContext().populateTemplateFieldsMenu(context, textView, menu);
		}

		return menu;

	}

	public static JMenu createVisibilityMenu(EditorPanel<? extends EditorPanel<?>> context, IVisible view) {

		if (context.getEditorContext() != null && context.getEditorContext().isVisibilityMenuEnabled()) {

			JMenu menu = new JMenu("Visibility");
			context.getEditorContext().populateVisibilityFieldsMenu(context, view, menu);

			return menu;

		} else {
			return null;
		}

	}

	public static void populateTextViewMenu(EditorPanel<? extends EditorPanel<?>> context, TextView textView, JMenu menu) {

		JMenuItem formatTextMenuItem = new JMenuItem("Format text");
		// menu.add(formatTextMenuItem);
		formatTextMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO
			}
		});

		menu.add(createInsertFieldMenu(context));
		menu.add(createInsertImageMenuItem(context, textView));

	}

	public static JMenuItem createInsertImageMenuItem(EditorPanel<? extends EditorPanel<?>> context, TextView textView) {

		JMenuItem insertImageMenuItem = new JMenuItem("Insert image");
		insertImageMenuItem.setIcon(Icons.image);
		insertImageMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				TextView selectedTextView = textView;
				if (selectedTextView == null) {
					for (Component component : context.getSelection()) {
						if (component instanceof TextView) {
							selectedTextView = (TextView) component;
							break;
						}
					}
				}

				if (selectedTextView != null) {

					String filePath = FileChooser.browseForImageFile(Icons.image, false, context.getFrame(), Aerialist.PREFERENCES, "lastImageFilePath");
					if (filePath != null) {

						try {

							File file = new File(filePath);
							String key = file.getName();

							BufferedImage image = context.getMediaCache().putImage(key, Files.readAllBytes(file.toPath()));
							if (image != null) {
								selectedTextView.insertImage(key, image);
							}

						} catch (Exception ex) {
							ex.printStackTrace();
						}

					}

				}

			}
		});

		if (textView == null) {

			insertImageMenuItem.setEnabled(false);
			context.getSelection().addSelectionListener(new SelectionListener() {

				@Override
				public void selectionChanged() {

					insertImageMenuItem.setEnabled(false);
					for (Component component : context.getSelection()) {
						if (component instanceof TextView) {
							insertImageMenuItem.setEnabled(true);
							break;
						}
					}

				}
			});

		}

		return insertImageMenuItem;

	}

	public static void populateImageViewMenu(EditorPanel<? extends EditorPanel<?>> context, ImageView imageView, JMenu menu) {

		menu.add(new SetImageAction(context, imageView));

		menu.addSeparator();

		JMenu alignmentMenu = new JMenu("Image alignment");
		menu.add(alignmentMenu);

		JCheckBoxMenuItem leftAlignmentMenuItem = new JCheckBoxMenuItem(new SetImageAlignmentAction(context, imageView, Alignment.LEFT));
		leftAlignmentMenuItem.setSelected(imageView.getAlignment() == Alignment.LEFT || imageView.getAlignment() == Alignment.DEFAULT);
		alignmentMenu.add(leftAlignmentMenuItem);

		JCheckBoxMenuItem centerAlignmentMenuItem = new JCheckBoxMenuItem(new SetImageAlignmentAction(context, imageView, Alignment.CENTER));
		centerAlignmentMenuItem.setSelected(imageView.getAlignment() == Alignment.CENTER);
		alignmentMenu.add(centerAlignmentMenuItem);

		JCheckBoxMenuItem rightAlignmentMenuItem = new JCheckBoxMenuItem(new SetImageAlignmentAction(context, imageView, Alignment.RIGHT));
		rightAlignmentMenuItem.setSelected(imageView.getAlignment() == Alignment.RIGHT);
		alignmentMenu.add(rightAlignmentMenuItem);

		JMenu imageFitMenu = new JMenu("Image fit");
		menu.add(imageFitMenu);

		JCheckBoxMenuItem fitPolicyDefaultMenuItem = new JCheckBoxMenuItem(new SetFitPolicyAction(context, imageView, FitPolicy.DEFAULT));
		fitPolicyDefaultMenuItem.setSelected(imageView.getFitPolicy() == FitPolicy.DEFAULT);
		imageFitMenu.add(fitPolicyDefaultMenuItem);

		JCheckBoxMenuItem fitPolicyWidthMenuItem = new JCheckBoxMenuItem(new SetFitPolicyAction(context, imageView, FitPolicy.WIDTH));
		fitPolicyWidthMenuItem.setSelected(imageView.getFitPolicy() == FitPolicy.WIDTH);
		imageFitMenu.add(fitPolicyWidthMenuItem);

		JCheckBoxMenuItem fitPolicyHeightMenuItem = new JCheckBoxMenuItem(new SetFitPolicyAction(context, imageView, FitPolicy.HEIGHT));
		fitPolicyHeightMenuItem.setSelected(imageView.getFitPolicy() == FitPolicy.HEIGHT);
		imageFitMenu.add(fitPolicyHeightMenuItem);

	}

	private static void populateTableCellViewMenu(EditorPanel<? extends EditorPanel<?>> context, TableCellView tableCellView, JMenu menu) {

		populateTextViewMenu(context, tableCellView, menu);

		menu.addSeparator();

		JMenu insertMenu = new JMenu("Insert");
		menu.add(insertMenu);

		insertMenu.add(new InsertTableRowAction(context, tableCellView, true));
		insertMenu.add(new InsertTableRowAction(context, tableCellView, false));
		insertMenu.add(new InsertTableColumnAction(context, tableCellView, true));
		insertMenu.add(new InsertTableColumnAction(context, tableCellView, false));

		menu.add(new DeleteTableRowAction(context, tableCellView));
		menu.add(new DeleteTableColumnAction(context, tableCellView));

	}

	public static JMenu createLayoutMenu(DocumentEditorPanel context, ISwingElementView<?> elementView, PageView pageView) {

		JMenu menu = new JMenu("Layout");

		JMenu verticalPositioningMenu = new JMenu("Vertical positioning");
		menu.add(verticalPositioningMenu);

		JCheckBoxMenuItem yPolicyDefaultMenuItem = new JCheckBoxMenuItem(new SetYPolicyAction(context, YPolicy.DEFAULT));
		yPolicyDefaultMenuItem.setSelected(elementView.getYPolicy() == YPolicy.DEFAULT);
		verticalPositioningMenu.add(yPolicyDefaultMenuItem);

		JCheckBoxMenuItem yPolicyFixedMenuItem = new JCheckBoxMenuItem(new SetYPolicyAction(context, YPolicy.FIXED));
		yPolicyFixedMenuItem.setSelected(elementView.getYPolicy() == YPolicy.FIXED);
		verticalPositioningMenu.add(yPolicyFixedMenuItem);

		JMenu heightMenu = new JMenu("Height");
		menu.add(heightMenu);

		JCheckBoxMenuItem heightPolicyAutoMenuItem = new JCheckBoxMenuItem(new SetHeightPolicyAction(context, HeightPolicy.AUTO));
		heightPolicyAutoMenuItem.setSelected(elementView.getHeightPolicy() == HeightPolicy.AUTO);
		heightMenu.add(heightPolicyAutoMenuItem);

		JCheckBoxMenuItem heightPolicyDefaultMenuItem = new JCheckBoxMenuItem(new SetHeightPolicyAction(context, HeightPolicy.DEFAULT));
		heightPolicyDefaultMenuItem.setSelected(elementView.getHeightPolicy() == HeightPolicy.DEFAULT);
		heightMenu.add(heightPolicyDefaultMenuItem);

		return menu;

	}

	public static JMenu createBackgroundColorMenu(Frame frame, Color color, Action action) {
		return createColorMenu(frame, "Background color", color, action);
	}

	public static JMenu createRowColorsMenu(DocumentEditorPanel context) {

		JMenu rowColorsMenu = new JMenu("Row colors");

		rowColorsMenu.add(createColorMenu(context.getFrame(), "Header", null, new SetRowColorAction(context, 1, 0)));
		rowColorsMenu.add(createColorMenu(context.getFrame(), "Even Rows", null, new SetRowColorAction(context, 2, 2)));
		rowColorsMenu.add(createColorMenu(context.getFrame(), "Odd rows", null, new SetRowColorAction(context, 1, 2)));

		return rowColorsMenu;

	}

	public static JMenu createColorMenu(Frame frame, String text, Color color, Action action) {

		JMenu editColorMenu = new JMenu(text);

		ColorChooserPanel colorChooserPanel = new ColorChooserPanel(color) {

			@Override
			protected Frame getFrame() {
				return frame;
			}
		};
		editColorMenu.add(colorChooserPanel);
		colorChooserPanel.getColorChooser().getActionButton().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				MenuSelectionManager.defaultManager().clearSelectedPath();
			}
		});
		colorChooserPanel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				MenuSelectionManager.defaultManager().clearSelectedPath();

				if (action != null) {
					action.actionPerformed(e);
				}

			}
		});

		return editColorMenu;

	}

	public static JMenuItem createPaddingMenu(DocumentEditorPanel context, ISwingElementView<?> elementView) {

		JMenu paddingMenu = new JMenu("Padding");
		paddingMenu.add(new SetPaddingAction(context, 0));
		paddingMenu.add(new SetPaddingAction(context, 1));
		paddingMenu.add(new SetPaddingAction(context, 2));
		paddingMenu.add(new SetPaddingAction(context, 3));
		paddingMenu.add(new SetPaddingAction(context, 4));
		paddingMenu.add(new SetPaddingAction(context, 5));

		paddingMenu.addSeparator();

		JMenuItem customMenuItem = new JMenuItem("Custom");
		paddingMenu.add(customMenuItem);
		customMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				Padding padding = SetPaddingAction.getPadding(elementView);
				if (padding == null) {
					padding = new Padding();
				}

				PaddingDialog paddingDialog = new PaddingDialog(context.getContext(), padding.top, padding.right, padding.bottom, padding.left);
				if (paddingDialog.setVisibleAndGetAction()) {
					PaddingPanel p = paddingDialog.getPaddingPanel();
					new SetPaddingAction(context).actionPerformed(new Padding(p.getTopPadding(), p.getRightPadding(), p.getBottomPadding(), p.getLeftPadding()));
				}

			}
		});

		return paddingMenu;

	}

	public static void setSelection(EditorPanel<? extends EditorPanel<?>> editor, PageView pageView, List<ElementData> elementsData) {

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				editor.focusContentContainer();
				editor.getSelection().clear();
				for (ElementData elementData : elementsData) {
					editor.getSelection().add(elementData.element);
				}
				editor.getSelection().fireSelectionChanged();
				editor.refresh(pageView);

			}
		});

	}

}
