package org.assistant.ui.controls;

import org.assistant.util.Messages;

import javax.swing.*;

public class Label extends JLabel {

	public Label() {
	}

	public Label(String text) {
		this(text, false);
	}

	public Label(String text, boolean localedText) {
		super(localedText ? Messages.getString(text) : text);
	}
}
