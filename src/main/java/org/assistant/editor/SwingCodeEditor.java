package org.assistant.editor;

import java.awt.*;

public class SwingCodeEditor implements CodeEditor {

	private Language language;

	@Override
	public Language setLanguage(Language language) {
		Language oldLanguage = this.language;
		this.language = language;
		return oldLanguage;
	}

	@Override
	public Language getCurrentLanguage() {
		return language;
	}

	@Override
	public String getText() {
		return "";
	}

	@Override
	public String setText(String text) {
		return "";
	}

	@Override
	public void setFont(Font font) {

	}
}
