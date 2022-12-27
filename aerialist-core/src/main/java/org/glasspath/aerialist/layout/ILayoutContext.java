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

public interface ILayoutContext<I> {

	public enum LayoutPhase {
		IDLE, LOAD_CONTENT, LAYOUT_CONTENT
	}

	public enum ExportPhase {
		IDLE, EXPORT
	}

	public FontCache<?> getFontCache();

	public MediaCache<I> getMediaCache();

	public LayoutPhase getLayoutPhase();

	public void setLayoutPhase(LayoutPhase layoutPhase);

	public boolean isHeightPolicyEnabled();

	public boolean isYPolicyEnabled();

	public void setYPolicyEnabled(boolean yPolicyEnabled);

	public ExportPhase getExportPhase();

	public void setExportPhase(ExportPhase exportPhase);

}
