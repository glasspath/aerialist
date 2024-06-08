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
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;

import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.Alignment;
import org.glasspath.aerialist.Document;
import org.glasspath.aerialist.Field.DynamicFieldKey;
import org.glasspath.aerialist.Field.FieldType;
import org.glasspath.aerialist.FitPolicy;
import org.glasspath.aerialist.HeightPolicy;
import org.glasspath.aerialist.IPagination;
import org.glasspath.aerialist.IVisible;
import org.glasspath.aerialist.Margin;
import org.glasspath.aerialist.Padding;
import org.glasspath.aerialist.Page.PageSize;
import org.glasspath.aerialist.YPolicy;
import org.glasspath.aerialist.editor.AbstractEditorPanel;
import org.glasspath.aerialist.editor.DocumentEditorContext;
import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.editor.EditorPanel;
import org.glasspath.aerialist.editor.ElementData;
import org.glasspath.aerialist.icons.Icons;
import org.glasspath.aerialist.resources.Resources;
import org.glasspath.aerialist.swing.view.FieldUtils;
import org.glasspath.aerialist.swing.view.FooterPageView;
import org.glasspath.aerialist.swing.view.GroupView;
import org.glasspath.aerialist.swing.view.HeaderPageView;
import org.glasspath.aerialist.swing.view.IScalableView;
import org.glasspath.aerialist.swing.view.ISwingElementView;
import org.glasspath.aerialist.swing.view.ImageView;
import org.glasspath.aerialist.swing.view.LayeredPageView;
import org.glasspath.aerialist.swing.view.PageContainer;
import org.glasspath.aerialist.swing.view.PageView;
import org.glasspath.aerialist.swing.view.QrCodeView;
import org.glasspath.aerialist.swing.view.TableCellView;
import org.glasspath.aerialist.swing.view.TableView;
import org.glasspath.aerialist.swing.view.TextView;
import org.glasspath.common.os.OsUtils;
import org.glasspath.common.swing.border.BorderMenu;
import org.glasspath.common.swing.color.ColorChooserDialog;
import org.glasspath.common.swing.color.ColorChooserPanel;
import org.glasspath.common.swing.color.ColorChooserPanel.ColorEvent;
import org.glasspath.common.swing.file.chooser.FileChooser;
import org.glasspath.common.swing.padding.PaddingDialog;
import org.glasspath.common.swing.padding.PaddingPanel;
import org.glasspath.common.swing.resources.CommonResources;
import org.glasspath.common.swing.selection.SelectionListener;

public class ActionUtils {

	public static boolean TODO_CREATE_GROUP_MENU_ITEMS = false;
	public static boolean TODO_CREATE_CUSTOM_PAGE_MARGINS_ITEM = false;

	private ActionUtils() {

	}

	public static void populateMenu(JMenu menu, EditorPanel<? extends EditorPanel<?>> context, List<Component> selection) {

		// TODO: Support deleting of selection across pages

		DocumentEditorPanel documentEditor = null;
		if (context instanceof DocumentEditorPanel) {
			documentEditor = (DocumentEditorPanel) context;
		}

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

			if (documentEditor != null) {
				menu.add(documentEditor.getCopyAction());
				menu.add(documentEditor.getPasteAction());
			}

			if (pageView != null) {
				menu.add(new DeleteElementsAction(context, pageView, elementViews));
			}

			menu.addSeparator();

			menu.add(createArrangeMenu(context));
			menu.add(createAlignMenu(context));

			if (documentEditor != null && TODO_CREATE_GROUP_MENU_ITEMS) {

				menu.addSeparator();

				menu.add(new GroupElementsAction(documentEditor, pageView, elementViews));
				menu.add(new UngroupElementsAction(documentEditor, pageView, elementViews));

			}

		}

	}

	public static void populateMenu(JMenu menu, DocumentEditorPanel context, Component component, boolean menuBarMenu) {

		if (component instanceof PageContainer) {
			populatePageContainerMenu(context, menu);
		} else if (component instanceof PageView) {
			populatePageViewMenu(context, (PageView) component, menu);
		} else {
			populateElementViewMenu(context, component, menu, menuBarMenu);
		}

	}

	private static void populatePageContainerMenu(DocumentEditorPanel context, JMenu menu) {

		menu.add(context.getPasteAction());

		menu.addSeparator();

		menu.add(new InsertPageAction(context, AerialistUtils.createDefaultPage(), InsertPageAction.INSERT));

	}

	private static void populatePageViewMenu(DocumentEditorPanel context, PageView pageView, JMenu menu) {

		boolean showMinimalMenu = false;
		boolean editingHeader = context.getPageContainer().isEditingHeader();
		boolean editingFooter = context.getPageContainer().isEditingFooter();

		// Normal page selected while editing header/footer, don't show full menu
		if (editingHeader && !(pageView instanceof HeaderPageView)) {
			showMinimalMenu = true;
		} else if (editingFooter && !(pageView instanceof FooterPageView)) {
			showMinimalMenu = true;
		}

		if (!showMinimalMenu) {

			menu.add(createInsertElementMenu(context, !editingHeader && !editingFooter));
			menu.add(createPageSizeMenu(context, (PageView) pageView));

			menu.addSeparator();

			// Header/footer cannot be copied or deleted, pasting page while
			// editing header/footer leads to problems with paste index..
			if (!editingHeader && !editingFooter) {

				menu.add(context.getCopyAction());
				menu.add(context.getPasteAction());
				menu.add(new DeletePageAction(context, pageView));

				menu.addSeparator();

			}

		}

		if (editingHeader) {

			JMenuItem finishEditingHeaderMenuItem = new JMenuItem("Finish editing page header");
			menu.add(finishEditingHeaderMenuItem);
			finishEditingHeaderMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					context.getPageContainer().stopEditingHeaderView();
				}
			});

		} else if (editingFooter) {

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
					context.getPageContainer().editHeaderPageView(pageView);
				}
			});

			JMenuItem editFooterMenuItem = new JMenuItem("Edit page footer");
			menu.add(editFooterMenuItem);
			editFooterMenuItem.setIcon(Icons.pageLayoutFooter);
			editFooterMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					context.getPageContainer().editFooterPageView(pageView);
				}
			});

		}

		if (!showMinimalMenu) {

			menu.addSeparator();

			JMenuItem updateFieldsMenuItem = new JMenuItem("Update fields");
			menu.add(updateFieldsMenuItem);
			updateFieldsMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					FieldUtils.updateDynamicFields(AerialistUtils.getDynamicFieldContext(context), context.getPageContainer());
					context.getPageContainer().refresh(null, null);
				}
			});

			JMenu visibilityMenu = createVisibilityMenu(context, pageView);
			if (visibilityMenu != null) {
				menu.addSeparator();
				menu.add(visibilityMenu);
			}

			// TODO: For now we only configure pagination settings globally on
			// document level, later we may also want to edit them on page or
			// on element level..
			if (pageView instanceof LayeredPageView && pageView.getParent() instanceof IPagination) {
				menu.addSeparator();
				menu.add(createPaginationSettingsMenu(context, (IPagination) pageView.getParent()));
			}

		}

	}

	private static void populateElementViewMenu(DocumentEditorPanel context, Component component, JMenu menu, boolean menuBarMenu) {

		Component element = AerialistUtils.getElementViewAsComponent(component);
		if (element instanceof ISwingElementView<?> && element.getParent() instanceof PageView) {

			ISwingElementView<?> elementView = (ISwingElementView<?>) element;
			PageView pageView = (PageView) element.getParent();

			menu.add(context.getCopyAction());
			menu.add(context.getPasteAction());
			menu.add(new DeleteElementsAction(context, (PageView) element.getParent(), element));

			menu.addSeparator();

			if (component instanceof TableCellView && component.getParent() instanceof TableView) {
				populateTableCellViewMenu(context, (TableView) component.getParent(), (TableCellView) component, menu, menuBarMenu);
				menu.addSeparator();
			} else if (component instanceof TableView) {
				populateTableCellViewMenu(context, (TableView) component, null, menu, menuBarMenu);
				menu.addSeparator();
			} else if (component instanceof QrCodeView) {
				populateQrCodeViewMenu(context, (QrCodeView) component, menu);
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

			menu.add(createBackgroundColorMenuItem(context.getFrameContext().getFrame(), elementView.getBackgroundColor(), new SetBackgroundColorAction(context), menuBarMenu));
			if (elementView instanceof TableView) {
				menu.add(createRowColorsMenu(context, (TableView) elementView, menuBarMenu));
			}
			menu.add(new BorderMenu(new SetBorderTypeAction(context), new SetBorderWidthAction(context), new SetBorderColorAction(context), menuBarMenu) {

				@Override
				protected Frame getFrame() {
					return context.getFrameContext().getFrame();
				}
			});
			if (SetPaddingAction.isPaddingSupported(elementView)) {
				menu.add(createPaddingMenu(context, elementView));
			}
			if (SetRadiusAction.isRadiusSupported(elementView)) {
				menu.add(createRadiusMenu(context, elementView));
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

			if (context.getEditorContext() != null) {
				context.getEditorContext().populateElementMenu(context, elementView, menu);
			}

		}

	}

	public static JMenu createPageSizeMenu(DocumentEditorPanel context, PageView pageView) {

		JMenu menu = new JMenu("Page size");

		menu.add(new JCheckBoxMenuItem(new SetPageSizeAction(context, pageView, PageSize.A4.getWidth(), PageSize.A4.getHeight(), "A4 Portrait")));
		menu.add(new JCheckBoxMenuItem(new SetPageSizeAction(context, pageView, PageSize.A4.getHeight(), PageSize.A4.getWidth(), "A4 Landscape")));

		if (SetMarginAction.isMarginSupported(pageView)) {

			menu.addSeparator();

			JMenu marginMenu = new JMenu("Margins");
			menu.add(marginMenu);

			marginMenu.add(new JCheckBoxMenuItem(new SetMarginAction(context, new Margin(40, 45, 40, 40), "Small", false)));
			marginMenu.add(new JCheckBoxMenuItem(new SetMarginAction(context, new Margin(Document.DEFAULT_MARGIN_TOP, Document.DEFAULT_MARGIN_RIGHT, Document.DEFAULT_MARGIN_BOTTOM, Document.DEFAULT_MARGIN_LEFT), CommonResources.getString("Default"), true))); //$NON-NLS-1$
			marginMenu.add(new JCheckBoxMenuItem(new SetMarginAction(context, new Margin(80, 85, 80, 80), "Large", false)));

			if (TODO_CREATE_CUSTOM_PAGE_MARGINS_ITEM) {

				marginMenu.addSeparator();

				JMenuItem customMenuItem = new JMenuItem("Custom");
				marginMenu.add(customMenuItem);
				customMenuItem.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {

						Margin margin = SetMarginAction.getMargin(pageView);
						if (margin == null) {
							margin = new Margin(Document.DEFAULT_MARGIN_TOP, Document.DEFAULT_MARGIN_RIGHT, Document.DEFAULT_MARGIN_BOTTOM, Document.DEFAULT_MARGIN_LEFT);
						}

						PaddingDialog marginDialog = new PaddingDialog(context.getFrameContext(), margin.top, margin.right, margin.bottom, margin.left);
						if (marginDialog.setVisibleAndGetAction()) {
							PaddingPanel p = marginDialog.getPaddingPanel();
							new SetMarginAction(context, new Margin(p.getTopPadding(), p.getRightPadding(), p.getBottomPadding(), p.getLeftPadding()), "Custom", false).actionPerformed();
						}

					}
				});

			}

		}

		return menu;

	}

	public static JMenu createPaginationSettingsMenu(DocumentEditorPanel context, IPagination view) {

		JMenu menu = new JMenu("Pagination");

		JMenu minHeightMenu = new JMenu("Minimum element height");
		menu.add(minHeightMenu);

		minHeightMenu.add(new JCheckBoxMenuItem(new SetPaginationMinHeightAction(context, view, 0)));
		minHeightMenu.addSeparator();
		minHeightMenu.add(new JCheckBoxMenuItem(new SetPaginationMinHeightAction(context, view, 50)));
		minHeightMenu.add(new JCheckBoxMenuItem(new SetPaginationMinHeightAction(context, view, 100)));
		minHeightMenu.add(new JCheckBoxMenuItem(new SetPaginationMinHeightAction(context, view, 150)));
		minHeightMenu.add(new JCheckBoxMenuItem(new SetPaginationMinHeightAction(context, view, 200)));
		minHeightMenu.add(new JCheckBoxMenuItem(new SetPaginationMinHeightAction(context, view, 250)));

		return menu;

	}

	public static JMenu createInsertElementMenu(DocumentEditorPanel context, boolean createInsertPageItems) {

		JMenu menu = new JMenu("Insert");

		if (createInsertPageItems) {

			if (context.getSelection().size() == 1 && context.getSelection().get(0) instanceof PageView) {

				PageView pageView = (PageView) context.getSelection().get(0);

				menu.add(new InsertPageAction(context, AerialistUtils.createDefaultPage(pageView.getWidth(), pageView.getHeight()), InsertPageAction.ABOVE));
				menu.add(new InsertPageAction(context, AerialistUtils.createDefaultPage(pageView.getWidth(), pageView.getHeight()), InsertPageAction.BELOW));

			} else {
				menu.add(new InsertPageAction(context, AerialistUtils.createDefaultPage(), InsertPageAction.INSERT));
			}

			menu.addSeparator();

		}

		int itemCount = menu.getItemCount();

		if (context.getEditorContext() != null) {
			context.getEditorContext().populateInsertElementMenu(context, menu);
		}

		if (menu.getItemCount() > itemCount) {
			menu.addSeparator();
		}

		if (context.getEditorContext() instanceof DocumentEditorContext) {
			menu.add(new InsertElementAction(context, ((DocumentEditorContext) context.getEditorContext()).createTextBox(), "Insert text box", Icons.textBoxPlus));
		} else {
			menu.add(new InsertElementAction(context, AerialistUtils.createDefaultTextBox(), "Insert text box", Icons.textBoxPlus));
		}
		menu.add(new InsertElementAction(context, AerialistUtils.createDefaultTable(), "Insert table", Icons.tablePlus));
		menu.add(new InsertElementAction(context, AerialistUtils.createDefaultImage(), "Insert image", Icons.imagePlus));
		menu.add(new InsertElementAction(context, AerialistUtils.createDefaultQrCode(), "Insert QR code", Icons.qrcodePlus));

		return menu;

	}

	public static JMenu createArrangeMenu(EditorPanel<? extends EditorPanel<?>> context) {

		JMenu menu = new JMenu(Resources.getString("Arrange")); //$NON-NLS-1$

		menu.add(new ArrangeElementsAction(context, ArrangeElementsAction.BRING_FORWARD, false));
		menu.add(new ArrangeElementsAction(context, ArrangeElementsAction.BRING_TO_FRONT, false));
		menu.add(new ArrangeElementsAction(context, ArrangeElementsAction.SEND_BACKWARD, false));
		menu.add(new ArrangeElementsAction(context, ArrangeElementsAction.SEND_TO_BACK, false));

		return menu;

	}

	public static JMenu createAlignMenu(EditorPanel<? extends EditorPanel<?>> context) {

		JMenu menu = new JMenu(Resources.getString("Align")); //$NON-NLS-1$

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

		JMenu generalMenu = new JMenu(CommonResources.getString("General")); //$NON-NLS-1$
		menu.add(generalMenu);

		JMenu documentMenu = new JMenu(Resources.getString("Document")); //$NON-NLS-1$
		generalMenu.add(documentMenu);

		documentMenu.add(new InsertFieldAction(context, textView, FieldType.DYNAMIC.getIdentifier() + DynamicFieldKey.PAGE.getKey(), DynamicFieldKey.PAGE.getDescription()));
		documentMenu.add(new InsertFieldAction(context, textView, FieldType.DYNAMIC.getIdentifier() + DynamicFieldKey.PAGES.getKey(), DynamicFieldKey.PAGES.getDescription()));

		JMenu dateAndTimeMenu = new JMenu(Resources.getString("DateTime")); //$NON-NLS-1$
		generalMenu.add(dateAndTimeMenu);

		dateAndTimeMenu.add(new InsertFieldAction(context, textView, FieldType.DYNAMIC.getIdentifier() + DynamicFieldKey.MILLIS.getKey(), DynamicFieldKey.MILLIS.getDescription()));

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

					String filePath = FileChooser.browseForImageFile(Icons.image, false, context.getFrameContext().getFrame(), context.getFrameContext().getPreferences(), "lastImageFilePath");
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

	public static void populateImageViewMenu(DocumentEditorPanel context, ImageView imageView, JMenu menu) {

		menu.add(new SetImageAction(context, imageView));

		menu.addSeparator();

		populateIScalableViewMenu(context, imageView, menu);

	}

	public static void populateQrCodeViewMenu(DocumentEditorPanel context, QrCodeView qrCodeView, JMenu menu) {

		menu.add(createInsertFieldMenu(context));

		menu.addSeparator();

		populateIScalableViewMenu(context, qrCodeView, menu);

	}

	public static void populateIScalableViewMenu(DocumentEditorPanel context, IScalableView imageView, JMenu menu) {

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

	private static void populateTableCellViewMenu(DocumentEditorPanel context, TableView tableView, TableCellView tableCellView, JMenu menu, boolean menuBarMenu) {

		if (tableCellView != null) {

			populateTextViewMenu(context, tableCellView, menu);

			menu.addSeparator();

		}

		menu.add(createTableHeaderMenu(context, tableView, menuBarMenu));

		JMenu insertMenu = new JMenu("Insert");
		insertMenu.setIcon(Icons.table);
		menu.add(insertMenu);

		if (tableCellView != null) {

			int row = tableCellView.getRow();

			insertMenu.add(new InsertTableRowAction(context, tableView, row, "Insert row above"));
			insertMenu.add(new InsertTableRowAction(context, tableView, row + (tableCellView.getRowSpan() - 1) + 1, "Insert row below"));

			int col = tableCellView.getCol();

			insertMenu.add(new InsertTableColumnAction(context, tableView, col, "Insert column left"));
			insertMenu.add(new InsertTableColumnAction(context, tableView, col + (tableCellView.getColSpan() - 1) + 1, "Insert column right"));

			menu.add(new DeleteTableRowAction(context, tableCellView));
			menu.add(new DeleteTableColumnAction(context, tableCellView));

		} else {

			int row = tableView.getRowCount() + 1;

			insertMenu.add(new InsertTableRowAction(context, tableView, row, "Insert row"));

			int col = tableView.getColumnCount() + 1;

			insertMenu.add(new InsertTableColumnAction(context, tableView, col, "Insert column"));

		}

	}

	public static JMenu createTableHeaderMenu(DocumentEditorPanel context, TableView tableView, boolean menuBarMenu) {

		JMenu headerMenu = new JMenu("Table header");

		headerMenu.add(new JCheckBoxMenuItem(new SetHeaderRowsAction(context, tableView, 0)));

		headerMenu.addSeparator();

		headerMenu.add(new JCheckBoxMenuItem(new SetHeaderRowsAction(context, tableView, 1)));
		headerMenu.add(new JCheckBoxMenuItem(new SetHeaderRowsAction(context, tableView, 2)));
		headerMenu.add(new JCheckBoxMenuItem(new SetHeaderRowsAction(context, tableView, 3)));

		headerMenu.addSeparator();

		JMenuItem headerColorMenuItem = createColorMenuItem(context.getFrameContext().getFrame(), Resources.getString("BackgroundColor"), null, new SetRowColorAction(context, tableView, 0, 0), menuBarMenu); // Use row 0 for header //$NON-NLS-1$
		headerColorMenuItem.setEnabled(tableView.getHeaderRows() > 0);
		headerMenu.add(headerColorMenuItem);

		return headerMenu;

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

	public static JMenuItem createBackgroundColorMenuItem(Frame frame, Color color, Action action, boolean menuBarMenu) {
		return createColorMenuItem(frame, Resources.getString("BackgroundColor"), color, action, menuBarMenu); //$NON-NLS-1$
	}

	public static JMenu createRowColorsMenu(DocumentEditorPanel context, TableView tableView, boolean menuBarMenu) {

		JMenu rowColorsMenu = new JMenu("Row colors");

		rowColorsMenu.add(createColorMenuItem(context.getFrameContext().getFrame(), "Even Rows", null, new SetRowColorAction(context, tableView, 2, 2), menuBarMenu));
		rowColorsMenu.add(createColorMenuItem(context.getFrameContext().getFrame(), "Odd rows", null, new SetRowColorAction(context, tableView, 1, 2), menuBarMenu));

		return rowColorsMenu;

	}

	public static JMenuItem createColorMenuItem(Frame frame, String text, Color color, Action action, boolean menuBarMenu) {

		if (menuBarMenu && OsUtils.PLATFORM_MACOS) {

			JMenuItem editColorMenuItem = new JMenuItem(text);
			editColorMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					JColorChooser colorChooser = new JColorChooser();
					colorChooser.setColor(color);

					JDialog dialog = new ColorChooserDialog(frame, CommonResources.getString("EditColor"), true, null, colorChooser, new ActionListener() { //$NON-NLS-1$

						@Override
						public void actionPerformed(ActionEvent e) {

							final Color color;
							if (ColorChooserDialog.NULL_COLOR.equals(colorChooser.getColor())) {
								color = null;
							} else {
								color = colorChooser.getColor();
							}

							action.actionPerformed(new ColorEvent(e, color));

						}
					}, null);

					dialog.setLocationRelativeTo(frame);
					dialog.setVisible(true);
					dialog.requestFocusInWindow();

				}
			});

			return editColorMenuItem;

		} else {

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

	}

	public static JMenuItem createPaddingMenu(DocumentEditorPanel context, ISwingElementView<?> elementView) {

		JMenu paddingMenu = new JMenu(CommonResources.getString("Padding")); //$NON-NLS-1$
		paddingMenu.add(new JCheckBoxMenuItem(new SetPaddingAction(context, 0)));
		paddingMenu.add(new JCheckBoxMenuItem(new SetPaddingAction(context, 1)));
		paddingMenu.add(new JCheckBoxMenuItem(new SetPaddingAction(context, 2)));
		paddingMenu.add(new JCheckBoxMenuItem(new SetPaddingAction(context, 3)));
		paddingMenu.add(new JCheckBoxMenuItem(new SetPaddingAction(context, 4)));
		paddingMenu.add(new JCheckBoxMenuItem(new SetPaddingAction(context, 5)));

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

				PaddingDialog paddingDialog = new PaddingDialog(context.getFrameContext(), padding.top, padding.right, padding.bottom, padding.left);
				if (paddingDialog.setVisibleAndGetAction()) {
					PaddingPanel p = paddingDialog.getPaddingPanel();
					new SetPaddingAction(context).actionPerformed(new Padding(p.getTopPadding(), p.getRightPadding(), p.getBottomPadding(), p.getLeftPadding()));
				}

			}
		});

		return paddingMenu;

	}

	public static JMenuItem createRadiusMenu(AbstractEditorPanel context, ISwingElementView<?> elementView) {

		JMenu radiusMenu = new JMenu("Radius");
		radiusMenu.add(new JCheckBoxMenuItem(new SetRadiusAction(context, 0)));
		radiusMenu.add(new JCheckBoxMenuItem(new SetRadiusAction(context, 5)));
		radiusMenu.add(new JCheckBoxMenuItem(new SetRadiusAction(context, 10)));
		radiusMenu.add(new JCheckBoxMenuItem(new SetRadiusAction(context, 15)));
		radiusMenu.add(new JCheckBoxMenuItem(new SetRadiusAction(context, 20)));
		radiusMenu.add(new JCheckBoxMenuItem(new SetRadiusAction(context, 25)));

		// TODO: Add menu item for setting custom radius

		return radiusMenu;

	}

	public static void setSelection(AbstractEditorPanel editor, PageView pageView, List<ElementData> elementsData) {

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				editor.focusContentContainer();
				editor.getSelection().clear();

				// TODO? In single page layout mode the PageView will not have a
				// parent if it is not visible, so we don't want to select anything..
				if (pageView.getParent() != null) {

					for (ElementData elementData : elementsData) {
						editor.getSelection().add(elementData.element);
					}
					editor.getSelection().fireSelectionChanged();
					editor.refresh(pageView);

				} else {
					editor.getSelection().fireSelectionChanged();
				}

			}
		});

	}

}
