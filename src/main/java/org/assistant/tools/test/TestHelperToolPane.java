package org.assistant.tools.test;

import org.assistant.tools.ToolProvider;
import org.assistant.ui.controls.Button;
import org.assistant.ui.controls.TextEditor;
import org.assistant.ui.pane.BorderPane;
import org.assistant.ui.pane.SplitPane;
import org.assistant.ui.pane.VBox;
import org.assistant.util.Messages;
import org.assistant.util.ULID;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.UUID;

class TestHelperToolPane extends BorderPane implements ToolProvider {

	SplitPane splitPane;
	TextEditor resultEditor;

	public TestHelperToolPane() {
		splitPane = new SplitPane();

		resultEditor = new TextEditor();

		VBox vBox = new VBox();

		Button btn = new Button("UUID");
		btn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				resultEditor.setText(UUID.randomUUID().toString());
			}
		});

		Button btn1 = new Button("ULID");
		btn1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				resultEditor.setText(ULID.randomULID());
			}
		});

		vBox.add(btn);
		vBox.add(btn1);

		splitPane.setRightComponent(resultEditor);
		splitPane.setLeftComponent(vBox);
		setCenter(splitPane);
	}

	@Override
	public String getLabel() {
		return Messages.getString("control.label.test");
	}

	@Override
	public JComponent getView() {
		return this;
	}
}
