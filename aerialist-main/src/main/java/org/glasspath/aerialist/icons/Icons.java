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
package org.glasspath.aerialist.icons;

import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import org.glasspath.common.swing.icon.SvgIcon;

@SuppressWarnings("nls")
public class Icons {

	public static final Icons INSTANCE = new Icons();
	public static final ClassLoader CLASS_LOADER = INSTANCE.getClass().getClassLoader();

	private Icons() {

	}

	private static URL getSvg(String name) {
		return CLASS_LOADER.getResource("org/glasspath/aerialist/icons/svg/" + name);
	}

	public static final ArrayList<Image> appIcon = new ArrayList<Image>();
	static {
		appIcon.add(new ImageIcon(CLASS_LOADER.getResource("org/glasspath/aerialist/icons/16x16/app_icon.png")).getImage());
		appIcon.add(new ImageIcon(CLASS_LOADER.getResource("org/glasspath/aerialist/icons/22x22/app_icon.png")).getImage());
		appIcon.add(new ImageIcon(CLASS_LOADER.getResource("org/glasspath/aerialist/icons/24x24/app_icon.png")).getImage());
		appIcon.add(new ImageIcon(CLASS_LOADER.getResource("org/glasspath/aerialist/icons/32x32/app_icon.png")).getImage());
		appIcon.add(new ImageIcon(CLASS_LOADER.getResource("org/glasspath/aerialist/icons/48x48/app_icon.png")).getImage());
	}

	public static final SvgIcon alignHorizontalCenter = new SvgIcon(16, 1, getSvg("align-horizontal-center.svg"));
	public static final SvgIcon alignHorizontalLeft = new SvgIcon(16, 1, getSvg("align-horizontal-left.svg"));
	public static final SvgIcon alignHorizontalRight = new SvgIcon(16, 1, getSvg("align-horizontal-right.svg"));
	public static final SvgIcon alignVerticalBottom = new SvgIcon(16, 1, getSvg("align-vertical-bottom.svg"));
	public static final SvgIcon alignVerticalCenter = new SvgIcon(16, 1, getSvg("align-vertical-center.svg"));
	public static final SvgIcon alignVerticalTop = new SvgIcon(16, 1, getSvg("align-vertical-top.svg"));
	public static final SvgIcon arrangeBringForward = new SvgIcon(16, 1, getSvg("arrange-bring-forward.svg"));
	public static final SvgIcon arrangeBringToFront = new SvgIcon(16, 1, getSvg("arrange-bring-to-front.svg"));
	public static final SvgIcon arrangeSendBackward = new SvgIcon(16, 1, getSvg("arrange-send-backward.svg"));
	public static final SvgIcon arrangeSendToBack = new SvgIcon(16, 1, getSvg("arrange-send-to-back.svg"));
	public static final SvgIcon arrowExpandVertical = new SvgIcon(16, 1, getSvg("arrow-expand-vertical.svg"));
	public static final SvgIcon arrowVerticalLock = new SvgIcon(getSvg("arrow-vertical-lock.svg"));
	public static final SvgIcon borderNoneVariant = new SvgIcon(getSvg("border-none-variant.svg"));
	public static final SvgIcon contentSave = new SvgIcon(getSvg("content-save.svg"));
	public static final SvgIcon dotsGrid = new SvgIcon(16, 1, getSvg("dots-grid.svg"));
	public static final SvgIcon fileDocumentPlus = new SvgIcon(getSvg("file-document-plus.svg"));
	public static final SvgIcon fileDocumentPlusOutline = new SvgIcon(getSvg("file-document-plus-outline.svg"));
	public static final SvgIcon formatAlignCenter = new SvgIcon(16, 2, getSvg("format-align-center-2.svg"));
	public static final SvgIcon formatAlignLeft = new SvgIcon(16, 2, getSvg("format-align-left-2.svg"));
	public static final SvgIcon formatAlignRight = new SvgIcon(16, 2, getSvg("format-align-right-2.svg"));
	public static final SvgIcon formatBold = new SvgIcon(getSvg("format-bold.svg"));
	public static final SvgIcon formatColorText = new SvgIcon(getSvg("format-color-text.svg"));

	// TODO: indent & outdent icons don't really match
	public static final SvgIcon formatIndentDecrease = new SvgIcon(16, 2, getSvg("outdent.svg"));
	public static final SvgIcon formatIndentIncrease = new SvgIcon(16, 2, getSvg("indent.svg"));

	public static final SvgIcon formatItalic = new SvgIcon(16, 1, getSvg("format-italic.svg"));
	public static final SvgIcon formatLineSpacing = new SvgIcon(getSvg("format-line-spacing.svg"));
	public static final SvgIcon formatListBulletedSquare = new SvgIcon(16, 2, getSvg("list.svg"));
	public static final SvgIcon formatStrikethrough = new SvgIcon(16, 1, getSvg("format-strikethrough.svg"));
	public static final SvgIcon formatUnderline = new SvgIcon(16, 1, getSvg("format-underline.svg"));
	public static final SvgIcon image = new SvgIcon(getSvg("image.svg"));
	public static final SvgIcon imagePlus = new SvgIcon(getSvg("image-plus.svg"));
	public static final SvgIcon lock = new SvgIcon(16, 2, getSvg("lock.svg"));
	public static final SvgIcon magnet = new SvgIcon(16, 2, getSvg("magnet.svg"));
	public static final SvgIcon pageLayoutFooter = new SvgIcon(16, 2, getSvg("page-layout-footer.svg"));
	public static final SvgIcon pageLayoutHeader = new SvgIcon(16, 2, getSvg("page-layout-header.svg"));
	public static final SvgIcon qrcodePlus = new SvgIcon(getSvg("qrcode-plus.svg"));
	public static final SvgIcon redo = new SvgIcon(getSvg("redo.svg"));
	public static final SvgIcon send = new SvgIcon(getSvg("send.svg"));
	public static final SvgIcon sendLarge = new SvgIcon(22, 0, getSvg("send.svg"));
	public static final SvgIcon tableLarge = new SvgIcon(getSvg("table-large.svg"));
	public static final SvgIcon tableLargePlus = new SvgIcon(getSvg("table-large-plus.svg"));
	public static final SvgIcon textBoxPlus = new SvgIcon(getSvg("text-box-plus.svg"));
	public static final SvgIcon undo = new SvgIcon(getSvg("undo.svg"));
	public static final SvgIcon viewAgendaOutline = new SvgIcon(16, 1, getSvg("view-agenda-outline.svg"));

	static {
		alignHorizontalCenter.setColorFilter(SvgIcon.PURPLE);
		alignHorizontalLeft.setColorFilter(SvgIcon.PURPLE);
		alignHorizontalRight.setColorFilter(SvgIcon.PURPLE);
		alignVerticalBottom.setColorFilter(SvgIcon.PURPLE);
		alignVerticalCenter.setColorFilter(SvgIcon.PURPLE);
		alignVerticalTop.setColorFilter(SvgIcon.PURPLE);
		arrangeBringForward.setColorFilter(SvgIcon.GREEN);
		arrangeBringToFront.setColorFilter(SvgIcon.GREEN);
		arrangeSendBackward.setColorFilter(SvgIcon.GREEN);
		arrangeSendToBack.setColorFilter(SvgIcon.GREEN);
		arrowExpandVertical.setColorFilter(SvgIcon.GREEN);
		arrowVerticalLock.setColorFilter(SvgIcon.RED);
		fileDocumentPlus.setColorFilter(SvgIcon.BLUE);
		imagePlus.setColorFilter(SvgIcon.BLUE);
		lock.setColorFilter(SvgIcon.RED);
		magnet.setColorFilter(SvgIcon.RED);
		qrcodePlus.setColorFilter(SvgIcon.BLUE);
		redo.setColorFilter(SvgIcon.BLUE);
		tableLargePlus.setColorFilter(SvgIcon.BLUE);
		textBoxPlus.setColorFilter(SvgIcon.BLUE);
		undo.setColorFilter(SvgIcon.BLUE);
		viewAgendaOutline.setColorFilter(SvgIcon.BLUE);
	}

}
