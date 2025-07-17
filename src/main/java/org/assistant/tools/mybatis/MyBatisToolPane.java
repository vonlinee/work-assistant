package org.assistant.tools.mybatis;

import org.assistant.tools.ToolProvider;
import org.assistant.ui.pane.BorderPane;
import org.assistant.util.Messages;

import javax.swing.*;

public class MyBatisToolPane implements ToolProvider {

	BorderPane borderPane;

	public MyBatisToolPane() {
		borderPane = new BorderPane();

	}

	@Override
	public String getLabel() {
		return Messages.getString("tools.mybatis.label");
	}

	@Override
	public JComponent getView() {
		return borderPane;
	}
}
