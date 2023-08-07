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

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.glasspath.aerialist.Document;
import org.glasspath.aerialist.HeightPolicy;
import org.glasspath.aerialist.IPagination;
import org.glasspath.aerialist.Page;
import org.glasspath.aerialist.Pagination;

public abstract class PageContainer extends JPanel implements ISwingViewContext, IPagination {

	public static final int PAGE_MODE_MULTIPLE = 0;
	public static final int PAGE_MODE_SINGLE = 1;

	private LayoutPhase layoutPhase = LayoutPhase.IDLE;
	private boolean yPolicyEnabled = false;
	private ExportPhase exportPhase = ExportPhase.IDLE;

	private String margin = null;
	private int headerHeight = 0;
	private int footerHeight = 0;
	private HeaderPageView headerPageView = null;
	private FooterPageView footerPageView = null;
	private Pagination pagination = null;
	private List<PageView> pageViews = new ArrayList<>();
	private int pageMode = PAGE_MODE_MULTIPLE;
	private int pageIndex = 0;

	public PageContainer() {

		setOpaque(false);
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

	}

	public void init(Document document) {

		margin = document.getMargin();
		headerHeight = document.getHeaderHeight();
		footerHeight = document.getFooterHeight();

		if (document.getHeader() != null) {
			headerPageView = createHeaderPageView(document.getHeader(), this);
		} else {
			headerPageView = null;
		}

		if (document.getFooter() != null) {
			footerPageView = createFooterPageView(document.getFooter(), this);
		} else {
			footerPageView = null;
		}

		if (document.getPagination() != null) {
			pagination = new Pagination(document.getPagination());
		} else {
			pagination = null;
		}

		pageViews = createLayeredPageViews(document.getPages(), this);

		loadPageViews();

	}

	public Document toDocument() {

		Document document = new Document();

		document.setMargin(margin);
		document.setHeaderHeight(headerHeight);
		document.setFooterHeight(footerHeight);

		if (headerPageView != null) {
			document.setHeader(headerPageView.toPage());
		}

		if (footerPageView != null) {
			document.setFooter(footerPageView.toPage());
		}

		if (pagination != null) {
			document.setPagination(new Pagination(pagination));
		}

		for (PageView pageView : pageViews) {
			document.getPages().add(pageView.toPage());
		}

		return document;

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
		return yPolicyEnabled;
	}

	@Override
	public void setYPolicyEnabled(boolean yPolicyEnabled) {
		this.yPolicyEnabled = yPolicyEnabled;
	}

	@Override
	public ExportPhase getExportPhase() {
		return exportPhase;
	}

	@Override
	public void setExportPhase(ExportPhase exportPhase) {
		this.exportPhase = exportPhase;
	}

	public String getMargin() {
		return margin;
	}

	public void setMargin(String margin) {
		this.margin = margin;
	}

	public int getHeaderHeight() {
		return headerHeight;
	}

	public void setHeaderHeight(int headerHeight) {
		this.headerHeight = headerHeight;
	}

	public int getFooterHeight() {
		return footerHeight;
	}

	public void setFooterHeight(int footerHeight) {
		this.footerHeight = footerHeight;
	}

	public HeaderPageView getHeaderPageView() {
		return headerPageView;
	}

	public void setHeaderPageView(HeaderPageView headerPageView) {
		this.headerPageView = headerPageView;
	}

	public FooterPageView getFooterPageView() {
		return footerPageView;
	}

	public void setFooterPageView(FooterPageView footerPageView) {
		this.footerPageView = footerPageView;
	}

	@Override
	public Pagination getPagination() {
		return pagination;
	}

	@Override
	public void setPagination(Pagination pagination) {
		this.pagination = pagination;
	}

	public List<PageView> getPageViews() {
		return pageViews;
	}

	public void setPageViews(List<PageView> pageViews) {
		this.pageViews = pageViews;
	}

	public int getPageMode() {
		return pageMode;
	}

	public void setPageMode(int pageMode) {
		this.pageMode = pageMode;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public boolean previousPage() {

		if (pageIndex > 0) {

			pageIndex--;
			loadPageViews();

			return true;

		} else {
			return false;
		}

	}

	public boolean nextPage() {

		if (pageIndex < pageViews.size() - 1) {

			pageIndex++;
			loadPageViews();

			return true;

		} else {
			return false;
		}

	}

	public void loadPageViews() {

		removeAll();

		add(Box.createRigidArea(new Dimension(25, 20)));

		if (pageMode == PAGE_MODE_SINGLE) {

			if (pageIndex >= 0 && pageIndex < pageViews.size()) {
				add(pageViews.get(pageIndex));
				add(Box.createRigidArea(new Dimension(25, 25)));
			}

		} else {

			for (PageView pageView : pageViews) {
				add(pageView);
				add(Box.createRigidArea(new Dimension(25, 25)));
			}

		}

	}

	public void insertPageView(PageView pageView, int index) {

		if (index >= 0 && index <= pageViews.size()) {

			pageViews.add(index, pageView);

			int viewIndex = 1 + (index * 2);
			if (viewIndex >= 1 && viewIndex <= getComponentCount()) {

				add(pageView, viewIndex);
				add(Box.createRigidArea(new Dimension(25, 25)), viewIndex + 1);

			}

		}

	}

	public void removePageView(PageView pageView) {

		int index = pageViews.indexOf(pageView);
		if (index >= 0) {

			int viewIndex = 1 + (index * 2);

			pageViews.remove(index);

			remove(viewIndex + 1);
			remove(viewIndex);

		}

	}

	protected void updateLayers() {
		for (PageView pageView : pageViews) {
			createLayers((LayeredPageView) pageView);
		}
	}

	protected void createLayers(LayeredPageView pageView) {

		pageView.getLayers().clear();

		if (headerPageView != null) {
			pageView.getLayers().add(new HeaderPageView(this, headerPageView.toPage()));
		}

		if (footerPageView != null) {
			pageView.getLayers().add(new FooterPageView(this, footerPageView.toPage()));
		}

	}

	public void invalidate(HeightPolicy heightPolicy) {
		for (PageView pageView : pageViews) {
			pageView.invalidate(heightPolicy);
		}
	}

	public static List<PageView> createLayeredPageViews(List<Page> pages, PageContainer pageContainer) {

		List<PageView> pageViews = new ArrayList<>();

		for (Page page : pages) {
			pageViews.add(createLayeredPageView(page, pageContainer));
		}

		return pageViews;

	}

	public static LayeredPageView createLayeredPageView(Page page, PageContainer pageContainer) {

		LayeredPageView pageView = new LayeredPageView(pageContainer);
		pageView.init(page);

		pageContainer.createLayers(pageView);

		return pageView;

	}

	public static HeaderPageView createHeaderPageView(Page page, PageContainer pageContainer) {

		HeaderPageView pageView = new HeaderPageView(pageContainer);
		pageView.init(page);

		return pageView;

	}

	public static FooterPageView createFooterPageView(Page page, PageContainer pageContainer) {

		FooterPageView pageView = new FooterPageView(pageContainer);
		pageView.init(page);

		return pageView;

	}

}
