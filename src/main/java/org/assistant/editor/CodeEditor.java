package org.assistant.editor;

import java.awt.*;

public interface CodeEditor {

	Language setLanguage(Language language);

	Language getCurrentLanguage();

	String getText();

	String setText(String text);

	void setFont(Font font);

	static CodeEditor create() {
		return new RSyntaxCodeEditor();
	}
}
