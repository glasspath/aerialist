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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import org.glasspath.aerialist.editor.DocumentEditorContext;
import org.glasspath.aerialist.editor.DocumentEditorPanel;
import org.glasspath.aerialist.editor.EditorContext;
import org.glasspath.aerialist.icons.Icons;
import org.glasspath.aerialist.text.font.FontWeight;
import org.glasspath.aerialist.tools.EditTools;
import org.glasspath.aerialist.tools.FileTools;
import org.glasspath.aerialist.tools.HelpTools;
import org.glasspath.aerialist.tools.ObjectFormatTools;
import org.glasspath.aerialist.tools.SearchTools;
import org.glasspath.aerialist.tools.TextFormatTools;
import org.glasspath.aerialist.tools.UndoActions;
import org.glasspath.aerialist.tools.ViewTools;
import org.glasspath.common.Args;
import org.glasspath.common.Common;
import org.glasspath.common.GlasspathSystemProperties;
import org.glasspath.common.font.Fonts;
import org.glasspath.common.font.Fonts.FontFilter;
import org.glasspath.common.macos.MacOSUtils;
import org.glasspath.common.os.OsUtils;
import org.glasspath.common.swing.FrameContext;
import org.glasspath.common.swing.border.HidpiMatteBorder;
import org.glasspath.common.swing.color.ColorUtils;
import org.glasspath.common.swing.frame.FrameUtils;
import org.glasspath.common.swing.glasspane.GlassPane;
import org.glasspath.common.swing.search.SearchField.SearchAdapter;
import org.glasspath.common.swing.statusbar.StatusBar;
import org.glasspath.common.swing.theme.Theme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("nls")
public class Aerialist implements FrameContext {

	public static boolean TODO_TEST_SHEET_MODE = false;

	public static final int VERSION_CODE = 1;
	public static final String VERSION_NAME = "1.0";

	// To log to console run with VM-argument: -Dlogback.configurationFile=logback-stdout.xml
	public static final String LOG_PATH = System.getProperty("user.home") + "/.aerialist/log";
	public static final String LOG_BACKUP_PATH = LOG_PATH + ".1";
	public static final String LOG_EXTENSION = ".txt";
	public static Logger LOGGER;

	public static final String APP_TITLE = "Aerialist";

	private static final Preferences preferences = Preferences.userNodeForPackage(Aerialist.class);

	private final JFrame frame;
	private final ToolBarPanel toolBarPanel;
	private final UndoActions undoActions;
	private final GlassPane glassPane;
	private final MainPanel mainPanel;
	private final StatusBar statusBar;
	private final FileTools fileTools;
	private final EditTools editTools;
	private final ViewTools viewTools;
	private final SearchTools searchTools;
	private final TextFormatTools textFormatTools;
	private final ObjectFormatTools objectFormatTools;
	private final HelpTools helpTools;
	private final JLabel statusLabel;

	private boolean fullScreen = false;
	private boolean sourceEditorEnabled = true;
	private boolean contentChanged = false;

	public Aerialist() {
		this(null, null, null);
	}

	public Aerialist(String openFile) {
		this(null, null, openFile);
	}

	public Aerialist(DocumentEditorContext editorContext, IFieldContext templateFieldContext, String openFile) {
		this(editorContext, templateFieldContext, openFile, true);
	}

	public Aerialist(DocumentEditorContext editorContext, IFieldContext templateFieldContext, String openFile, boolean show) {

		this.frame = new JFrame() {

			@Override
			public void revalidate() {
				super.revalidate();
				glassPane.updateLayout(true);
			}
		};
		this.toolBarPanel = new ToolBarPanel();
		this.undoActions = new UndoActions();
		this.glassPane = new GlassPane(frame);
		this.mainPanel = new MainPanel(this, editorContext);
		this.statusBar = new StatusBar();
		this.fileTools = new FileTools(this);
		this.editTools = new EditTools(this, undoActions);
		this.viewTools = new ViewTools(this);
		this.searchTools = new SearchTools(this);
		this.textFormatTools = new TextFormatTools(this, mainPanel.getDocumentEditor());
		this.objectFormatTools = new ObjectFormatTools(this, mainPanel.getDocumentEditor());
		this.helpTools = new HelpTools(this);
		this.statusLabel = new JLabel();

		glassPane.setContentComponent(mainPanel);
		glassPane.setRightMargin(20);
		glassPane.getSearchField().addSearchListener(new SearchAdapter() {

			@Override
			public void searchCleared() {
				mainPanel.clearSearch();
			}

			@Override
			public void searchNext(String text) {
				mainPanel.searchNext(text);
			}

			@Override
			public void searchPrevious(String text) {
				mainPanel.searchPrevious(text);
			}
		});

		List<String> registeredFonts = Fonts.registerBundledFonts(System.getProperty(GlasspathSystemProperties.BUNDLED_FONTS_PATH), new FontFilter() {

			@Override
			public boolean filter(File file) {

				String name = file.getName().toLowerCase().replaceAll("[^A-Za-z0-9]", "");
				FontWeight weight = FontWeight.getFontWeight(name);

				return weight == FontWeight.REGULAR || weight == FontWeight.BOLD;

			}
		});

		textFormatTools.setFontFamilyNames(registeredFonts);
		fileTools.setExportActionsEnabled(true);

		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setIconImages(Icons.appIcon);
		frame.setTitle(APP_TITLE);

		JRootPane rootPane = frame.getRootPane();
		rootPane.setBackground(ColorUtils.TITLE_BAR_COLOR);
		if (OsUtils.PLATFORM_MACOS) {
			MacOSUtils.hideTitleBar(frame);
		}

		FrameUtils.loadFrameDimensions(frame, preferences, 30, 30, 885, 785, 0);

		frame.getContentPane().setLayout(new BorderLayout());

		JMenuBar menuBar = new JMenuBar();
		FrameUtils.applyMenuBarStyle(menuBar);
		menuBar.add(fileTools.getMenu());
		menuBar.add(editTools.getMenu());
		menuBar.add(viewTools.getMenu());
		menuBar.add(searchTools.getMenu());
		menuBar.add(helpTools.getMenu());
		frame.setJMenuBar(menuBar);

		statusBar.setPreferredSize(new Dimension(100, 20));

		// TODO
		statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
		// statusBar.setPanelLabel(statusLabel);

		if (editorContext != null && editorContext.getHeaderComponent() != null) {
			toolBarPanel.add(editorContext.getHeaderComponent(), BorderLayout.NORTH);
		}

		frame.getContentPane().add(toolBarPanel, BorderLayout.NORTH);
		frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
		frame.getContentPane().add(statusBar, BorderLayout.SOUTH);

		frame.setGlassPane(glassPane);
		glassPane.setVisible(true);

		undoActions.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO: This is a bit of a hack to trigger updating of tool-bar actions
				mainPanel.getDocumentEditor().getSelection().fireSelectionChanged();
			}
		});

		frame.addWindowListener(new WindowAdapter() {

			boolean inited = false;

			@Override
			public void windowActivated(WindowEvent e) {

				mainPanel.windowActivated(e);
				mainPanel.requestFocusInWindow();

				if (!inited) {

					frame.addComponentListener(new ComponentAdapter() {

						@Override
						public void componentResized(ComponentEvent e) {
							FrameUtils.saveFrameDimensions(frame, preferences);

							if (OsUtils.PLATFORM_MACOS) {

								boolean fullScreen = FrameUtils.isFullScreen(frame);

								if (Aerialist.this.fullScreen && !fullScreen) {
									rootPane.setBorder(BorderFactory.createEmptyBorder(MacOSUtils.DEFAULT_HIDDEN_TITLE_BAR_HEIGHT, 0, 0, 0));
								} else if (!Aerialist.this.fullScreen && fullScreen) {
									rootPane.setBorder(BorderFactory.createEmptyBorder());
								}

								Aerialist.this.fullScreen = fullScreen;

							}

						}

						@Override
						public void componentMoved(ComponentEvent e) {
							FrameUtils.saveFrameDimensions(frame, preferences);
						}
					});

					if (openFile != null) {

						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								fileTools.loadDocument(openFile, templateFieldContext);
							}
						});

					}

					frame.toFront();

					inited = true;

				}

			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				mainPanel.windowDeactivated(e);
			}

			@Override
			public void windowClosing(WindowEvent event) {
				exit();
			}
		});

		undoActions.setUndoManager(mainPanel.getDocumentEditor().getUndoManager());
		updateToolBars();

		if (show) {
			frame.setVisible(true);
		}

	}

	public void openDocument(String documentPath, IFieldContext templateFieldContext) {
		fileTools.loadDocument(documentPath, templateFieldContext);
	}

	public void openDocument(String documentPath, IFieldContext templateFieldContext, Runnable action) {
		fileTools.loadDocument(documentPath, templateFieldContext, true, action);
	}

	public void saveDocument(String documentPath) {
		fileTools.saveCurrentDocument(documentPath);
	}

	public void exportDocument(String filePath) {
		fileTools.exportToPdf(filePath, false);
	}

	public void closeDocument() {
		fileTools.closeDocument();
	}

	public void showOverlay(String text) {
		glassPane.showOverlayLabel(text);
	}

	public boolean isOverlayVisible() {
		return glassPane.isOverlayLabelVisible();
	}

	public void hideOverlay() {
		glassPane.hideOverlayLabel();
	}

	@Override
	public JFrame getFrame() {
		return frame;
	}

	public Preferences getPreferences() {
		return preferences;
	}

	@Override
	public boolean isContentChanged() {
		return contentChanged;
	}

	@Override
	public void setContentChanged(boolean changed) {
		this.contentChanged = changed;
		updateTitle();
	}

	private void updateTitle() {

		String title = APP_TITLE;

		String fileName = null;
		if (fileTools.getCurrentFilePath() != null) {
			fileName = new File(fileTools.getCurrentFilePath()).getName();
		}
		if (fileName == null && getEditorContext() != null) {
			fileName = getEditorContext().getSuggestedFileName();
		}
		if (fileName == null) {
			fileName = "untitled";
		}
		if (fileName != null && fileName.trim().length() > 0) {
			title += " - " + fileName;
		}

		if (contentChanged) {
			title += "*";
		}

		frame.setTitle(title);

	}

	public GlassPane getGlassPane() {
		return glassPane;
	}

	public MainPanel getMainPanel() {
		return mainPanel;
	}

	public FileTools getFileTools() {
		return fileTools;
	}

	public EditTools getEditTools() {
		return editTools;
	}

	public ViewTools getViewTools() {
		return viewTools;
	}

	public TextFormatTools getTextFormatTools() {
		return textFormatTools;
	}

	public ObjectFormatTools getObjectFormatTools() {
		return objectFormatTools;
	}

	public SearchTools getSearchTools() {
		return searchTools;
	}

	public JLabel getStatusLabel() {
		return statusLabel;
	}

	public UndoActions getUndoActions() {
		return undoActions;
	}

	public void setStatusBarVisible(boolean visible) {
		statusBar.setVisible(visible);
		revalidateFrame();
	}

	private void revalidateFrame() {

		frame.invalidate(); // TODO: Remove?
		frame.revalidate();
		frame.repaint();

	}

	public boolean isSourceEditorEnabled() {
		return sourceEditorEnabled;
	}

	public void setSourceEditorEnabled(boolean sourceEditorEnabled) {
		this.sourceEditorEnabled = sourceEditorEnabled;
		updateToolBars();
	}

	public EditorContext<DocumentEditorPanel> getEditorContext() {
		return mainPanel.getDocumentEditor().getEditorContext();
	}

	public void updateToolBars() {

		toolBarPanel.top.removeAll();
		toolBarPanel.bottom.removeAll();

		if (getEditorContext() != null && !getEditorContext().isEditable()) {

			toolBarPanel.top.setVisible(false);
			toolBarPanel.bottom.setVisible(false);
			// toolBarPanel.setBorder(BorderFactory.createEmptyBorder());

		} else {

			toolBarPanel.top.setVisible(true);
			toolBarPanel.top.add(fileTools.getToolBar());
			toolBarPanel.top.add(editTools.getToolBar());
			toolBarPanel.top.add(textFormatTools.getToolBar());
			toolBarPanel.top.add(Box.createHorizontalGlue());
			if (sourceEditorEnabled) {
				toolBarPanel.top.add(viewTools.getViewModeToolBar());
			}

			toolBarPanel.bottom.setVisible(true);
			if (mainPanel.getViewMode() == MainPanel.VIEW_MODE_DESIGN) {
				toolBarPanel.bottom.add(objectFormatTools.getToolBar());
			}
			toolBarPanel.bottom.add(Box.createHorizontalGlue());
			// toolBarPanel.setBorder(ToolBarPanel.BORDER);

		}

		toolBarPanel.revalidate();
		toolBarPanel.repaint();

	}

	public void exit() {

		FrameUtils.saveFrameDimensions(frame, preferences);

		if (fileTools.checkFileSaved()) {
			exitApplication();
		}

	}

	protected void exitApplication() {
		frame.setVisible(false);
	}

	private static class ToolBarPanel extends JPanel {

		private static final HidpiMatteBorder BORDER = new HidpiMatteBorder(new Insets(0, 0, 1, 0), Theme.isDark() ? HidpiMatteBorder.COLOR : new Color(230, 230, 230));

		private final JPanel top;
		private final JPanel bottom;

		public ToolBarPanel() {

			setLayout(new BorderLayout());
			setBackground(ColorUtils.TITLE_BAR_COLOR);
			setBorder(BORDER);

			top = new JPanel();
			top.setLayout(new BoxLayout(top, BoxLayout.LINE_AXIS));
			top.setBackground(ColorUtils.TITLE_BAR_COLOR);
			top.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 10));
			add(top, BorderLayout.CENTER);

			bottom = new JPanel();
			bottom.setLayout(new BoxLayout(bottom, BoxLayout.LINE_AXIS));
			bottom.setBackground(ColorUtils.TITLE_BAR_COLOR);
			bottom.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
			add(bottom, BorderLayout.SOUTH);

		}

	}

	public static void main(String[] args) {
		launch(null, null, args);
	}

	public static void launch(DocumentEditorContext editorContext, IFieldContext templateFieldContext, String[] args) {

		System.setProperty("log.path", LOG_PATH);
		LOGGER = LoggerFactory.getLogger(Aerialist.class);
		Common.LOGGER = LOGGER; // TODO
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread thread, Throwable e) {
				LOGGER.error("Uncaught exception in thread: " + thread.getName(), e);
			}
		});

		File applicationJarFile = OsUtils.getApplicationJarFile(Aerialist.class);

		LOGGER.info("Application path: " + applicationJarFile);
		LOGGER.info("user.dir: " + System.getProperty("user.dir"));
		LOGGER.info("user.home: " + System.getProperty("user.home"));
		LOGGER.info("Application versionCode: " + VERSION_CODE + " versionName: " + VERSION_NAME);

		String openFileArgument = null;
		String themeArgument = null;
		String nativesArgument = null;
		String fontsArgument = null;

		if (args != null) {

			for (String arg : args) {

				try {

					LOGGER.info("Parsing argument: " + arg);

					boolean argParsed = false;

					if (!argParsed && openFileArgument == null) {
						if (arg.toLowerCase().endsWith("." + XDoc.DOCUMENT_EXTENSION) && new File(arg).exists()) {
							openFileArgument = arg;
						}
						argParsed = openFileArgument != null;
					}

					if (!argParsed && themeArgument == null) {
						themeArgument = Theme.parseArgument(arg);
						argParsed = themeArgument != null;
					}

					if (!argParsed && nativesArgument == null) {
						nativesArgument = Args.parseArgument(arg, "-natives");
						argParsed = nativesArgument != null;
					}

					if (!argParsed && fontsArgument == null) {
						fontsArgument = Args.parseArgument(arg, "-fonts");
						argParsed = fontsArgument != null;
					}

				} catch (Exception e) {
					LOGGER.error("Exception while parsing argument", e);
					e.printStackTrace();
				}

			}

		}

		final String nativeLibraryPath = nativesArgument != null ? nativesArgument : applicationJarFile.getParent();
		System.setProperty(GlasspathSystemProperties.NATIVE_LIBRARY_PATH, nativeLibraryPath);

		OsUtils.configureJna(nativeLibraryPath);

		String bundledFontsPath = fontsArgument != null ? fontsArgument : applicationJarFile.getParent() + "/fonts";
		System.setProperty(GlasspathSystemProperties.BUNDLED_FONTS_PATH, bundledFontsPath);

		if (openFileArgument == null && preferences.getBoolean("openLastFileAtStartup", true)) {
			openFileArgument = preferences.get("lastOpenedFile", "");
		}

		final String openFile = openFileArgument;

		if (themeArgument != null) {
			Theme.load(themeArgument);
		} else {
			Theme.load(preferences);
		}

		FrameUtils.setSystemLookAndFeelProperties(APP_TITLE);

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				try {
					FrameUtils.installLookAndFeel(nativeLibraryPath);
				} catch (Exception e) {
					LOGGER.error("Exception while setting look and feel", e);
					e.printStackTrace();
				}

				new Aerialist(editorContext, templateFieldContext, openFile) {

					@Override
					protected void exitApplication() {
						super.exitApplication();
						System.exit(0);
					}
				};

			}
		});

	}

}
