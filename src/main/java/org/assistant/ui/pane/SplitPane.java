package org.assistant.ui.pane;

import javax.swing.*;
import java.awt.*;

public class SplitPane extends JSplitPane {

	public SplitPane() {
		this(false);
	}

	public SplitPane(boolean vertical) {
		super(vertical ? JSplitPane.VERTICAL_SPLIT : JSplitPane.HORIZONTAL_SPLIT);
		this.setResizeWeight(0.5);
	}

	public void setComponent(Component left, Component right) {
		setLeftComponent(left);
		setRightComponent(right);
	}

	/**
	 * https://stackoverflow.com/questions/1879091/jsplitpane-setdividerlocation-problem
	 * The setDividerLocation( double ) method only works on a "realized" frame, which means after you've packed or made the frame visible.
	 * <p>
	 * The setDividerLocation( int ) method can be used at any time.
	 *
	 * @param location location
	 */
	@Override
	public void setDividerLocation(int location) {
		super.setDividerLocation(location);
	}
}
