package org.assistant.tools.html;

import org.assistant.tools.excel.ExcelUtils;
import org.assistant.tools.excel.TableData;
import org.assistant.ui.controls.Button;
import org.assistant.ui.controls.Label;
import org.assistant.ui.controls.TextArea;
import org.assistant.ui.controls.TextField;
import org.assistant.ui.pane.BorderPane;
import org.assistant.ui.pane.HBox;
import org.assistant.ui.pane.SplitPane;
import org.assistant.ui.pane.VBox;
import org.assistant.util.FileUtils;
import org.assistant.util.Messages;
import org.assistant.util.StringUtils;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class HtmlToolPane extends BorderPane {

	TextArea textArea = new TextArea();

	public HtmlToolPane() {
		setTop(new Label("使用说明: 浏览器控制台，选择<table></table>元素 -> 右键 -> 复制 -> 复制元素"));

		SplitPane splitPane = new SplitPane();

		splitPane.setLeftComponent(textArea);

		VBox vBox = new VBox();
		TextField textField = new TextField(FileUtils.getDesktopAbsolutePath());
		textField.setMaximumHeight(20);
		vBox.add(textField);
		splitPane.setRightComponent(vBox);
		setCenter(splitPane);

		Button btn = new Button(Messages.getString("tools.html.btnText"));
		btn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String text = textArea.getText();
				if (StringUtils.isEmpty(text)) {
					return;
				}
				TableData tableData = HtmlTableParser.parseTable(text);
				if (!StringUtils.isEmpty(textField.getText())) {
					ExcelUtils.writeXlsx(tableData, new File(textField.getText()));
				}
			}
		});
		HBox bottom = new HBox();
		bottom.add(btn);
		setBottom(bottom);
	}
}
