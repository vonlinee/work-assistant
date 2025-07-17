package org.assistant.tools.excel;

import org.assistant.tools.ToolProvider;
import org.assistant.ui.pane.BorderPane;

import javax.swing.*;

class ExcelToolPane extends BorderPane implements ToolProvider {

	public ExcelToolPane() {

	}

	@Override
	public String getLabel() {
		return "Excel";
	}

	@Override
	public JComponent getView() {
		return this;
	}
}
