package org.glasspath.aerialist.swing.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.swing.event.CaretEvent;

import org.glasspath.aerialist.media.BufferedImageMediaCache;
import org.glasspath.aerialist.media.MediaCache;
import org.glasspath.aerialist.swing.view.TextView.TextData;
import org.glasspath.aerialist.text.font.FontCache;

public class DefaultSwingViewContext implements ISwingViewContext {

	private final BufferedImageMediaCache mediaCache;
	private LayoutPhase layoutPhase = LayoutPhase.IDLE;
	private boolean yPolicyEnabled = false;
	private ExportPhase exportPhase = ExportPhase.IDLE;

	public DefaultSwingViewContext(BufferedImageMediaCache mediaCache) {
		this.mediaCache = mediaCache;
	}

	@Override
	public FontCache<?> getFontCache() {
		return null;
	}

	@Override
	public MediaCache<BufferedImage> getMediaCache() {
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

	@Override
	public boolean isRightMouseSelectionAllowed() {
		return false;
	}

	@Override
	public void focusGained(FocusEvent e) {

	}

	@Override
	public void focusLost(FocusEvent e) {

	}

	@Override
	public void caretUpdate(CaretEvent e) {

	}

	@Override
	public void viewEventHappened(ViewEvent viewEvent) {

	}

	@Override
	public Map<Component, Rectangle> getAnchoredElementBounds(Component component) {
		return null;
	}

	@Override
	public void createUndoableEdit(ISwingViewContext viewContext, TextView textView, TextData oldTextData, TextData newTextData, Map<Component, Rectangle> anchoredElementBounds) {

	}

	@Override
	public void refresh(Component component, Map<Component, Rectangle> anchoredElementBounds) {

	}

	@Override
	public Color getDefaultForeground() {
		return Color.black;
	}

	@Override
	public int getContainerPaintFlags() {
		return 0;
	}

	public int getViewPaintFlags(Component view) {
		return 0;
	}

}
