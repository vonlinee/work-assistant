package org.assistant.tools.prompt;

import org.assistant.tools.ToolProvider;

import javax.swing.*;

/**
 * 提示词管理工具提供者
 */
public class PromptToolProvider implements ToolProvider {

	@Override
	public String getLabel() {
		return "Prompt Manager";
	}

	@Override
	public JComponent getView() {
		return new PromptManagerPanel();
	}

	@Override
	public int getOrder() {
		return 100;
	}
}
