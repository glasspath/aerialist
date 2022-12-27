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
package org.glasspath.aerialist.layout;

import org.glasspath.aerialist.media.MediaCache;
import org.glasspath.aerialist.text.font.FontCache;

public class DefaultLayoutContext<F, I> implements ILayoutContext<I> {

	private final FontCache<?> fontCache;
	private final MediaCache<I> mediaCache;
	private LayoutPhase layoutPhase = LayoutPhase.IDLE;
	private boolean yPolicyEnabled = false;
	private ExportPhase exportPhase = ExportPhase.IDLE;

	public DefaultLayoutContext(FontCache<?> fontCache, MediaCache<I> mediaCache) {
		this.fontCache = fontCache;
		this.mediaCache = mediaCache;
	}

	@Override
	public FontCache<?> getFontCache() {
		return fontCache;
	}

	@Override
	public MediaCache<I> getMediaCache() {
		return mediaCache;
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

}
