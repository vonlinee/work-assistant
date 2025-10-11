package org.assistant.ui.controls;

import javax.swing.*;
import java.awt.*;

public class TextField extends JTextField {

	public TextField() {
	}

	public TextField(String text) {
		super(text);
	}

	public void setPreferredHeight(int height) {
		Dimension preferredSize = getPreferredSize();
		preferredSize.setSize(preferredSize.getWidth(), height);
		setPreferredSize(preferredSize);
	}

	public void setPreferredWidth(int width) {
		Dimension preferredSize = getPreferredSize();
		preferredSize.setSize(width, preferredSize.getHeight());
		setPreferredSize(preferredSize);
	}

	public void setMaximumWidth(int width) {
		Dimension maximumSize = getMaximumSize();
		maximumSize.setSize(width, maximumSize.getHeight());
		setMaximumSize(maximumSize);
	}

	public void setMaximumHeight(int height) {
		Dimension maximumSize = getMaximumSize();
		maximumSize.setSize(maximumSize.getWidth(), height);
		setMaximumSize(maximumSize);
	}

	/**
	 * 保持宽度可扩展，但高度固定为偏好高度
	 * 解决TextField放在VBox里，高度被撑满问题
	 */
	@Override
	public Dimension getMaximumSize() {
		Dimension max = super.getMaximumSize();
		Dimension pref = getPreferredSize();
		return new Dimension(max.width, pref.height);
	}
}
