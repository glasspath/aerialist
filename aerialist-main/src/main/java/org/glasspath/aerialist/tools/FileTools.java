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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.glasspath.aerialist.Aerialist;
import org.glasspath.aerialist.AerialistUtils;
import org.glasspath.aerialist.Content;
import org.glasspath.aerialist.Document;
import org.glasspath.aerialist.HtmlExporter;
import org.glasspath.aerialist.IFieldContext;
import org.glasspath.aerialist.MainPanel;
import org.glasspath.aerialist.XDoc;
import org.glasspath.aerialist.editor.DocumentEditorContext;
import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.icons.Icons;
import org.glasspath.aerialist.layout.IElementLayoutMetrics;
import org.glasspath.aerialist.layout.ILayoutContext.ExportPhase;
import org.glasspath.aerialist.layout.ILayoutContext.LayoutPhase;
import org.glasspath.aerialist.layout.LayoutListener;
import org.glasspath.aerialist.pdfbox.PdfBoxDocumentLoader;
import org.glasspath.aerialist.reader.XDocReader;
import org.glasspath.aerialist.swing.BufferedImageMediaCache;
import org.glasspath.aerialist.swing.SwingLayoutMetrics;
import org.glasspath.aerialist.swing.view.DefaultSwingViewContext;
import org.glasspath.aerialist.swing.view.FieldUtils;
import org.glasspath.aerialist.swing.view.TextView;
import org.glasspath.aerialist.template.TemplateDocumentLoader;
import org.glasspath.aerialist.writer.XDocWriter;
import org.glasspath.common.GlasspathSystemProperties;
import org.glasspath.common.os.OsUtils;
import org.glasspath.common.swing.DesktopUtils;
import org.glasspath.common.swing.dialog.DialogUtils;
import org.glasspath.common.swing.file.chooser.FileChooser;
import org.glasspath.common.swing.tools.AbstractTools;

import com.lowagie.text.DocumentException;

public class FileTools extends AbstractTools<Aerialist> {

	public static boolean TEMP_TEST_EXPORT_ON_LOAD = false;
	public static boolean TODO_ADD_EXPORT_HTML_MENU_ITEM = false;
	public static boolean TODO_ADD_PRINT_MENU_ITEM = false;

	private final JMenuItem exportPdfMenuItem;
	private final JMenuItem exportHtmlMenuItem;

	private String currentFilePath = null;

	public FileTools(Aerialist context) {
		super(context, "File");

		JMenuItem newMenuItem = new JMenuItem("New");
		newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, OsUtils.CTRL_OR_CMD_MASK));
		menu.add(newMenuItem);
		newMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				newAction();
			}
		});

		JMenuItem openMenuItem = new JMenuItem("Open");
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, OsUtils.CTRL_OR_CMD_MASK));
		menu.add(openMenuItem);
		openMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				openAction();
			}
		});

		JMenuItem saveMenuItem = new JMenuItem("Save");
		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, OsUtils.CTRL_OR_CMD_MASK));
		saveMenuItem.setIcon(Icons.contentSave);
		menu.add(saveMenuItem);
		saveMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				saveAction();
			}
		});

		JMenuItem saveAsMenuItem = new JMenuItem("Save as");
		menu.add(saveAsMenuItem);
		saveAsMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				saveAsAction();
			}
		});

		menu.addSeparator();

		exportPdfMenuItem = new JMenuItem("Export to PDF");
		exportPdfMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, OsUtils.CTRL_OR_CMD_MASK));
		exportPdfMenuItem.setEnabled(false);
		menu.add(exportPdfMenuItem);
		exportPdfMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				exportToPdfAction();
			}
		});

		exportHtmlMenuItem = new JMenuItem("Export to html");
		exportHtmlMenuItem.setEnabled(false);
		if (TODO_ADD_EXPORT_HTML_MENU_ITEM) {
			menu.add(exportHtmlMenuItem);
		}
		exportHtmlMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				exportToHtml();
			}
		});

		if (TODO_ADD_PRINT_MENU_ITEM) {
			menu.addSeparator();
		}

		JMenuItem printItem = new JMenuItem("Print");
		if (TODO_ADD_PRINT_MENU_ITEM) {
			menu.add(printItem);
		}
		printItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				// TODO
			}
		});

		menu.addSeparator();

		JMenuItem exitMenuItem = new JMenuItem("Exit");
		menu.add(exitMenuItem);
		exitMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				context.exit();
			}
		});

		JButton saveButton = new JButton();
		saveButton.setIcon(Icons.contentSave);
		saveButton.setToolTipText("Save");
		toolBar.add(saveButton);
		saveButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				saveAction();
			}
		});

	}

	public String getCurrentFilePath() {
		return currentFilePath;
	}

	public void setExportActionsEnabled(boolean enabled) {
		exportPdfMenuItem.setEnabled(enabled);
		exportHtmlMenuItem.setEnabled(enabled);
	}

	private void clearUndoHistory() {
		context.getUndoActions().getUndoManager().discardAllEdits();
		context.getUndoActions().updateActions();
	}

	private void newAction() {

		if (checkFileSaved()) {

			currentFilePath = null;
			context.setContentChanged(false);
			clearUndoHistory();

			DocumentEditorPanel editor = context.getMainPanel().getDocumentEditor();

			editor.getSelection().clear();

			Document document;
			if (editor.getEditorContext() instanceof DocumentEditorContext) {
				document = ((DocumentEditorContext) editor.getEditorContext()).createDocument();
			} else {
				document = AerialistUtils.createDefaultDocument();
			}

			editor.getPageContainer().init(document);

			editor.invalidate();
			editor.validate();
			editor.repaint();

		}

	}

	private void openAction() {

		if (checkFileSaved()) {

			// TODO: Icon
			String path = FileChooser.browseForFile(XDoc.DOCUMENT_EXTENSION, Icons.image, false, context.getFrame(), context.getPreferences(), "lastFilePath"); //$NON-NLS-1$
			if (path != null) {
				loadDocument(path, null);
			}

		}

	}

	private boolean saveAction() {

		if (currentFilePath != null) {

			boolean saved = saveCurrentDocument(currentFilePath);
			if (saved) {
				context.setContentChanged(false);
			}

			return saved;

		} else {
			return saveAsAction();
		}

	}

	private boolean saveAsAction() {

		boolean saved = false;

		String suggestedName = null;
		if (context.getEditorContext() != null) {
			suggestedName = context.getEditorContext().getSuggestedFileName();
		}

		// TODO: Icon
		String newFilePath = FileChooser.browseForFile(XDoc.DOCUMENT_EXTENSION, Icons.image, true, context.getFrame(), context.getPreferences(), "lastFilePath", suggestedName); //$NON-NLS-1$
		if (newFilePath != null) {

			saved = saveCurrentDocument(newFilePath);

			if (saved) {
				currentFilePath = newFilePath;
				context.setContentChanged(false);
			}

		}

		return saved;

	}

	public void loadDocument(String documentPath, IFieldContext templateFieldContext) {
		loadDocument(documentPath, templateFieldContext, true, null);
	}

	public void loadDocument(String documentPath, IFieldContext templateFieldContext, boolean loadInBackground, Runnable action) {

		currentFilePath = null;
		context.setContentChanged(false);
		clearUndoHistory();

		DocumentEditorPanel editor = context.getMainPanel().getDocumentEditor();
		editor.getSelection().clear();

		Document document = null;
		BufferedImageMediaCache mediaCache = new BufferedImageMediaCache();

		if (documentPath != null && new File(documentPath).exists()) {

			XDoc xDoc = XDocReader.read(documentPath, mediaCache);
			if (xDoc != null && xDoc.getContent() != null && xDoc.getContent().getRoot() instanceof Document) {

				document = (Document) xDoc.getContent().getRoot();

				// When creating a new document by parsing template data we don't want to set currentPath
				// because this would overwrite the template document when saving
				if (templateFieldContext == null) {
					currentFilePath = documentPath;
					context.setContentChanged(false);
				}

			}

		}

		if (document == null) {

			if (documentPath != null && documentPath.trim().length() > 0) {
				DialogUtils.showWarningMessage(context.getFrame(), "Loading failed", "The document could not be opened");
			}

			if (editor.getEditorContext() instanceof DocumentEditorContext) {
				document = ((DocumentEditorContext) editor.getEditorContext()).createDocument();
			} else {
				document = AerialistUtils.createDefaultDocument();
			}

		}

		editor.setMediaCache(mediaCache);

		if (templateFieldContext != null) {

			final Document templateDocument = document;

			Runnable loadDocumentRunnable = new Runnable() {

				@Override
				public void run() {

					LayoutListener listener = new LayoutListener() {

						@Override
						public void statusChanged(String status) {
							// TODO?
						}
					};

					DefaultSwingViewContext layoutContext = new DefaultSwingViewContext(mediaCache);

					TemplateDocumentLoader documentLoader = new TemplateDocumentLoader(listener, layoutContext) {

						@Override
						protected IElementLayoutMetrics createLayoutMetrics() {
							return new SwingLayoutMetrics(layoutContext);
						}
					};

					documentLoader.loadDocument(templateDocument, templateFieldContext, mediaCache);

					Runnable showDowcumentRunnable = new Runnable() {

						@Override
						public void run() {

							showDocument(editor, templateDocument);

							if (action != null) {
								action.run();
							}

							if (editor.getEditorContext() instanceof DocumentEditorContext) {
								((DocumentEditorContext) editor.getEditorContext()).documentShown(editor, templateDocument, documentPath);
							}

							// TODO
							if (TEMP_TEST_EXPORT_ON_LOAD) {

								new Thread(new Runnable() {

									@Override
									public void run() {

										// OpenPdfDocumentLoader documentLoader = new OpenPdfDocumentLoader();
										PdfBoxDocumentLoader documentLoader = new PdfBoxDocumentLoader();
										documentLoader.setLayoutListener(listener);
										documentLoader.loadDocument(new File(documentPath), templateFieldContext, System.getProperty(GlasspathSystemProperties.BUNDLED_FONTS_PATH), new File("export.pdf"));

										DesktopUtils.open("export.pdf", context.getFrame());

									}
								}).start();

							}

						}
					};

					if (loadInBackground) {
						SwingUtilities.invokeLater(showDowcumentRunnable);
					} else {
						showDowcumentRunnable.run();
					}

				}
			};

			if (loadInBackground) {
				new Thread(loadDocumentRunnable).start();
			} else {
				loadDocumentRunnable.run();
			}

		} else {

			showDocument(editor, document);

			if (action != null) {
				action.run();
			}

			if (editor.getEditorContext() instanceof DocumentEditorContext) {
				((DocumentEditorContext) editor.getEditorContext()).documentShown(editor, document, documentPath);
			}

		}

	}

	private void showDocument(DocumentEditorPanel editor, Document document) {

		editor.getPageContainer().setLayoutPhase(LayoutPhase.LOAD_CONTENT);
		editor.getPageContainer().setYPolicyEnabled(false);

		editor.getPageContainer().setVisible(false);
		editor.getPageContainer().init(document);

		FieldUtils.updateDynamicFields(AerialistUtils.getDynamicFieldContext(editor), editor.getPageContainer());

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				editor.getPageContainer().setLayoutPhase(LayoutPhase.LAYOUT_CONTENT);
				editor.getPageContainer().setYPolicyEnabled(true);

				editor.getPageContainer().setVisible(true);
				editor.scrollToTop();

				editor.getPageContainer().setLayoutPhase(LayoutPhase.IDLE);

			}
		});

	}

	public boolean saveCurrentDocument(String path) {

		DocumentEditorPanel editor = context.getMainPanel().getDocumentEditor();

		// The contents from the design view-mode are always used for saving, so in
		// source view-mode we need to apply any changes to the design view-mode first
		if (context.getMainPanel().getViewMode() == MainPanel.VIEW_MODE_SOURCE) {
			context.getMainPanel().updateDocumentEditor();
		}

		XDoc xDoc = new XDoc();

		Content content = new Content();
		xDoc.setContent(content);

		Document document = editor.getPageContainer().toDocument();
		content.setRoot(document);

		xDoc.setMediaCache(editor.getMediaCache());

		boolean saved = XDocWriter.write(xDoc, new File(path));

		if (saved && editor.getEditorContext() instanceof DocumentEditorContext) {
			((DocumentEditorContext) editor.getEditorContext()).documentSaved(editor, document, path);
		}

		return saved;

	}

	public void closeDocument() {

		if (checkFileSaved()) {

			currentFilePath = null;
			context.setContentChanged(false);
			clearUndoHistory();

			context.getUndoActions().getUndoManager().discardAllEdits();
			context.getUndoActions().updateActions();

			DocumentEditorPanel editor = context.getMainPanel().getDocumentEditor();

			editor.getSelection().clear();
			editor.getPageContainer().init(AerialistUtils.createEmptyDocument());

			editor.invalidate();
			editor.validate();
			editor.repaint();

		}

	}

	public boolean checkFileSaved() {

		if (context.isContentChanged()) {

			int chosenOption = JOptionPane.showOptionDialog(context.getFrame(), "The file has been modified, save changes?", "Save changes?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[] { "Yes", "No", "Cancel" }, "Cancel");

			if (chosenOption == JOptionPane.YES_OPTION) {
				return saveAction();
			} else if (chosenOption == JOptionPane.NO_OPTION) {
				return true;
			} else {
				return false;
			}

		} else {
			return true;
		}

	}

	private void exportToPdfAction() {

		String suggestedFileName = null;

		if (context.getEditorContext() != null) {
			suggestedFileName = context.getEditorContext().getSuggestedFileName();
		}

		if (suggestedFileName == null) {
			suggestedFileName = "export";
		}

		// TODO
		String filePath = FileChooser.browseForFile("pdf", Icons.image, true, context.getFrame(), context.getPreferences(), "generateInvoiceDestinationPath", suggestedFileName + ".pdf"); //$NON-NLS-1$
		if (filePath != null) {
			exportToPdf(filePath, true);
		}

	}

	public void exportToPdf(String filePath, boolean open) {

		DocumentEditorPanel documentEditor = context.getMainPanel().getDocumentEditor();

		documentEditor.cancelSearch();

		// Clear text selection (otherwise it gets painted by PDF graphics2D)
		for (Component component : documentEditor.getSelection()) {

			if (component instanceof TextView) {

				TextView textView = (TextView) component;
				textView.setSelectionStart(-1);
				textView.setSelectionEnd(-1);

			}

		}

		documentEditor.deselectAll();

		try {

			documentEditor.getPageContainer().setExportPhase(ExportPhase.EXPORT);

			File outputFile = new File(filePath);

			AerialistUtils.writeToPDF(documentEditor.getPageContainer(), outputFile);

			documentEditor.getPageContainer().setExportPhase(ExportPhase.IDLE);

			if (open) {
				DesktopUtils.open(filePath, context.getFrame());
			}

		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void exportToHtml() {

		DocumentEditorPanel documentEditor = context.getMainPanel().getDocumentEditor();

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				try {

					Document document = documentEditor.getPageContainer().toDocument();

					String html = new HtmlExporter().toHtml(document);

					Files.write(Paths.get("export.html"), html.getBytes());

					documentEditor.getPageContainer().setExportPhase(ExportPhase.IDLE);

					DesktopUtils.open("export.html", context.getFrame());

				} catch (DocumentException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		});

	}

}
