package org.assistant.tools.sql;

import org.assistant.tools.ToolProvider;
import org.assistant.ui.controls.DbDialectComboBox;
import org.assistant.ui.controls.Label;
import org.assistant.ui.pane.BorderPane;
import org.assistant.ui.pane.HBox;
import org.assistant.ui.pane.SplitPane;

import javax.swing.*;

class SqlToolPane extends BorderPane implements ToolProvider {

	SqlEditor editor;

	public SqlToolPane() {
		HBox hBox = new HBox();
		hBox.add(new Label("tools.sql.dbType", true));
		hBox.setEmptyBorder(5);
		hBox.addSpacing(5);
		DbDialectComboBox comboBox = new DbDialectComboBox();
		hBox.add(comboBox);

		setTop(hBox);

		editor = new SqlEditor();

		SplitPane splitPane = new SplitPane();
		splitPane.setLeftComponent(editor);

		BorderPane right = new BorderPane();
		splitPane.setRightComponent(right);
		setCenter(splitPane);
	}

	@Override
	public String getLabel() {
		return "SQL";
	}

	@Override
	public JComponent getView() {
		return this;
	}
}
