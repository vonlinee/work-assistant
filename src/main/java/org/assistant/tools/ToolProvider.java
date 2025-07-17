package org.assistant.tools;

import javax.swing.*;

public interface ToolProvider {

	String getLabel();

	JComponent getView();

	default int getOrder() {
		return Integer.MIN_VALUE;
	}
}
