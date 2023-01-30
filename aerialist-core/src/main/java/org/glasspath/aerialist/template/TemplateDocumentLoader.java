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
package org.glasspath.aerialist.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.glasspath.aerialist.Document;
import org.glasspath.aerialist.Element;
import org.glasspath.aerialist.Field;
import org.glasspath.aerialist.Field.FieldType;
import org.glasspath.aerialist.IFieldContext;
import org.glasspath.aerialist.Page;
import org.glasspath.aerialist.Page.PageSize;
import org.glasspath.aerialist.layout.DefaultVerticalLayout;
import org.glasspath.aerialist.layout.DocumentLayoutInfo;
import org.glasspath.aerialist.layout.DocumentLayoutInfo.PageLayoutInfo;
import org.glasspath.aerialist.layout.IElementLayoutMetrics;
import org.glasspath.aerialist.layout.ILayoutContext;
import org.glasspath.aerialist.layout.ILayoutContext.LayoutPhase;
import org.glasspath.aerialist.layout.LayoutListener;
import org.glasspath.aerialist.layout.Paginator;
import org.glasspath.aerialist.media.MediaCache;
import org.glasspath.aerialist.media.MediaCache.ImageResource;
import org.glasspath.aerialist.writer.DocumentWriter;

public abstract class TemplateDocumentLoader {

	public static final int LAYOUT_MODE_ALL = 0;
	public static final int LAYOUT_MODE_AUTO_HEIGHT_ONLY = 1;

	private final LayoutListener listener;
	private final ILayoutContext<?> layoutContext;
	private int layoutMode = LAYOUT_MODE_ALL;
	private DocumentWriter documentWriter = null;

	private long start = 0;

	public TemplateDocumentLoader(LayoutListener listener, ILayoutContext<?> layoutContext) {
		this.listener = listener;
		this.layoutContext = layoutContext;
	}

	public int getLayoutMode() {
		return layoutMode;
	}

	public void setLayoutMode(int layoutMode) {
		this.layoutMode = layoutMode;
	}

	public DocumentWriter getDocumentWriter() {
		return documentWriter;
	}

	public void setDocumentWriter(DocumentWriter documentWriter) {
		this.documentWriter = documentWriter;
	}

	protected abstract IElementLayoutMetrics createLayoutMetrics();

	protected DefaultVerticalLayout createVerticalLayout(IElementLayoutMetrics layoutMetrics) {
		return new DefaultVerticalLayout(layoutContext, layoutMetrics);
	}

	protected ExecutorService createExecutorService() {
		return Executors.newFixedThreadPool(4);
	}

	public void loadDocument(Document document, IFieldContext templateFieldContext, MediaCache<?> mediaCache) {

		start = System.currentTimeMillis();

		if (templateFieldContext != null) {
			parseTemplate(document, templateFieldContext, mediaCache);
		}

		layoutContext.setLayoutPhase(LayoutPhase.LAYOUT_CONTENT);
		layoutContext.setYPolicyEnabled(true);

		DocumentLayoutInfo documentLayoutInfo = layoutDocument(document);

		writeDocument(documentLayoutInfo);

		layoutContext.setLayoutPhase(LayoutPhase.IDLE);

	}

	protected void parseTemplate(Document document, IFieldContext templateFieldContext, MediaCache<?> mediaCache) {

		if (mediaCache != null) {
			replaceImages(mediaCache, templateFieldContext);
		}

		TemplateParser templateParser = new TemplateParser() {

			@Override
			public void parsePage(Page page) {
				fireStatusChanged("Loading template data, page " + (document.getPages().indexOf(page) + 1) + " of " + document.getPages().size());
				super.parsePage(page);
			}
		};
		templateParser.parseTemplate(document, templateFieldContext);

		fireStatusChanged("Template parsed after " + (System.currentTimeMillis() - start) + " milliseconds");

	}

	protected void replaceImages(MediaCache<?> mediaCache, IFieldContext templateFieldContext) {

		Map<String, byte[]> images = new HashMap<>();

		for (Entry<String, ImageResource> entry : mediaCache.getImageResources().entrySet()) {

			Field field = new Field(entry.getKey());
			if (field.isTemplateField()) {

				Object object = templateFieldContext.getObject(field.key);
				if (object instanceof byte[]) {
					images.put(entry.getKey(), (byte[]) object);
				}

			}

		}

		for (Entry<String, byte[]> entry : images.entrySet()) {
			mediaCache.putImage(entry.getKey(), entry.getValue());
		}

	}

	protected DocumentLayoutInfo layoutDocument(Document document) {

		DocumentLayoutInfo documentLayoutInfo = new DocumentLayoutInfo();

		ExecutorService executorService = createExecutorService();

		List<Future<LayoutPageResult>> futures = new ArrayList<Future<LayoutPageResult>>();

		if (document.getHeader() != null) {
			futures.add(executorService.submit(new LayoutPageCallable(document, document.getHeader(), -1)));
		}

		if (document.getFooter() != null) {
			futures.add(executorService.submit(new LayoutPageCallable(document, document.getFooter(), -2)));
		}

		for (int i = 0; i < document.getPages().size(); i++) {
			futures.add(executorService.submit(new LayoutPageCallable(document, document.getPages().get(i), i)));
		}

		for (Future<LayoutPageResult> future : futures) {

			try {

				LayoutPageResult layoutPageResult = future.get();

				if (layoutPageResult.pageIndex >= 0 && layoutPageResult.pageIndex < document.getPages().size()) {
					documentLayoutInfo.pages.addAll(layoutPageResult.pages);
				} else if (layoutPageResult.pageIndex == -1 && layoutPageResult.pages.size() == 1) {
					documentLayoutInfo.header = layoutPageResult.pages.get(0);
				} else if (layoutPageResult.pageIndex == -2 && layoutPageResult.pages.size() == 1) {
					documentLayoutInfo.footer = layoutPageResult.pages.get(0);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		executorService.shutdown();

		document.getPages().clear();
		for (PageLayoutInfo pageLayoutInfo : documentLayoutInfo.pages) {
			document.getPages().add(pageLayoutInfo.page);
		}

		fireStatusChanged("Laying out document finished after " + (System.currentTimeMillis() - start) + " milliseconds");

		return documentLayoutInfo;

	}

	protected void writeDocument(DocumentLayoutInfo documentLayoutInfo) {

		if (documentWriter != null) {

			try {

				if (!documentWriter.isDocumentOpen()) {
					documentWriter.open(PageSize.A4.getWidth(), PageSize.A4.getHeight()); // TODO
				}

				for (PageLayoutInfo pageLayoutInfo : documentLayoutInfo.pages) {

					documentWriter.openPage(pageLayoutInfo.page.getWidth(), pageLayoutInfo.page.getHeight());

					if (documentLayoutInfo.header != null) {
						documentWriter.writePage(documentLayoutInfo.header);
					}

					if (documentLayoutInfo.footer != null) {
						documentWriter.writePage(documentLayoutInfo.footer);
					}

					documentWriter.writePage(pageLayoutInfo);

					documentWriter.closePage();

				}

				documentWriter.close();

				fireStatusChanged("Writing document finished after " + (System.currentTimeMillis() - start) + " milliseconds");

			} catch (Exception e) {
				e.printStackTrace(); // TODO
				fireStatusChanged("Writing document failed after " + (System.currentTimeMillis() - start) + " milliseconds");
			}

		}

	}

	private void fireStatusChanged(String status) {
		if (listener != null) {
			listener.statusChanged(status);
		}
	}

	private class LayoutPageCallable implements Callable<LayoutPageResult> {

		private final Document document;
		private final Page page;
		private final LayoutPageResult result = new LayoutPageResult();

		private LayoutPageCallable(Document document, Page page, int pageIndex) {
			this.document = document;
			this.page = page;
			this.result.pageIndex = pageIndex;
		}

		@Override
		public LayoutPageResult call() throws Exception {

			long pageStart = System.currentTimeMillis();

			if (result.pageIndex == -1) {
				fireStatusChanged("Laying out header page");
			} else if (result.pageIndex == -2) {
				fireStatusChanged("Laying out footer page");
			} else if (result.pageIndex >= 0) {
				fireStatusChanged("Laying out page " + (result.pageIndex + 1) + " of " + document.getPages().size());
			}

			IElementLayoutMetrics layoutMetrics = createLayoutMetrics();

			DefaultVerticalLayout layout = createVerticalLayout(layoutMetrics);
			layout.setContainer(page);

			if (layoutMode == LAYOUT_MODE_ALL) {
				for (Element element : page.getElements()) {
					layoutMetrics.getElementLayoutInfo(element);
				}
			}

			layout.updateVerticalAnchors();
			layout.validateLayout();

			PageLayoutInfo pageLayoutInfo = new PageLayoutInfo(page);
			pageLayoutInfo.layoutInfo = layoutMetrics.getLayoutInfo();
			result.pages.add(pageLayoutInfo);

			if (result.pageIndex >= 0) {

				Paginator paginator = new Paginator(layout, listener);
				result.pages.addAll(paginator.paginate(pageLayoutInfo, document.getHeaderHeight(), page.getHeight() - document.getFooterHeight()));

			}

			fireStatusChanged("Laying out page " + (result.pageIndex + 1) + " finished in " + (System.currentTimeMillis() - pageStart) + " milliseconds");

			return result;

		}

	}

	private static class LayoutPageResult {

		private int pageIndex = 0;
		private List<PageLayoutInfo> pages = new ArrayList<>();

	}

}
