package org.assistant.ui.controls;

import javax.swing.*;

public class Button extends JButton {

	public Button(String text) {
		super(text);
	}

	public Button() {
	}

	/**
	 * 调用setAction会导致，按钮的高度被压缩
	 * 建议调用
	 *
	 * @param a the <code>Action</code> for the <code>AbstractButton</code>,
	 *          or <code>null</code>
	 */
	@Override
	public void setAction(Action a) {
		super.setAction(a);
	}
}
