package org.assistant.tools.html;

import org.assistant.tools.ToolProvider;

import javax.swing.*;

class HtmlToolProvider implements ToolProvider {

	@Override
	public String getLabel() {
		return "HTML";
	}

	@Override
	public JComponent getView() {
		return new HtmlToolPane();
	}
}
