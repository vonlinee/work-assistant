package org.assistant.tools.fileannotate;

import org.assistant.tools.ToolProvider;

import javax.swing.*;

/**
 * 文件标注工具提供者
 */
public class FileAnnotationToolProvider implements ToolProvider {

	@Override
	public String getLabel() {
		return "File Annotator";
	}

	@Override
	public JComponent getView() {
		return new FileTreeTablePanel();
	}

	@Override
	public int getOrder() {
		return 200;
	}
}
