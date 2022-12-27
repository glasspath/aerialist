package org.glasspath.aerialist.swing.view;

import java.awt.Component;

import org.glasspath.aerialist.layout.ILayoutMetrics;

public class LayoutMetrics implements ILayoutMetrics<ISwingElementView<?>> {

	@Override
	public int getPreferredHeight(ISwingElementView<?> element) {
		return ((Component) element).getPreferredSize().height;
	}

}
