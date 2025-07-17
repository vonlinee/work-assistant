package org.assistant.tools.text;

import org.assistant.tools.ToolProvider;
import org.assistant.ui.controls.Button;
import org.assistant.ui.controls.Label;
import org.assistant.ui.controls.TextField;
import org.assistant.ui.pane.BorderPane;
import org.assistant.ui.pane.FlowPane;
import org.assistant.ui.pane.HBox;
import org.assistant.util.Messages;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class StringToolPane extends BorderPane implements ToolProvider {

	TextHandlePane textHandlePane;
	FlowPane flowPane;

	public StringToolPane() {
		textHandlePane = new TextHandlePane();

		HBox top = new HBox();
		top.setEmptyBorder(5);
		top.add(new Label("tools.textHandle.mode", true));
		top.addSpacing(10);
		top.add(new TextField());
		setTop(top);

		setCenter(textHandlePane);
		flowPane = new FlowPane();

		addTextHandlers(TextHandlerEnum.values());

		setBottom(flowPane);
	}

	public void addTextHandlers(TextHandler... textHandlers) {
		for (TextHandler textHandler : textHandlers) {
			Button btn = new Button(textHandler.getLabel());
			btn.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					textHandlePane.accept(textHandler);
				}
			});
			flowPane.add(btn);
		}
	}

	@Override
	public String getLabel() {
		return Messages.getString("control.label.textHandle");
	}

	@Override
	public JComponent getView() {
		return this;
	}
}
