package org.assistant.tools.text;

import org.assistant.ui.controls.TextEditor;
import org.assistant.ui.pane.SplitPane;

public class TextHandlePane extends SplitPane {

	TextEditor input;
	TextEditor output;

	public TextHandlePane() {
		input = new TextEditor();
		output = new TextEditor();
		setResizeWeight(0.5);
		setComponent(input, output);
	}

	public void accept(TextHandler handler) {
		String text = input.getText();
		if (text == null || text.isEmpty()) {
			return;
		}
		String outputText = handler.handle(text);
		if (output != null) {
			output.setText(outputText);
		}
	}

}
