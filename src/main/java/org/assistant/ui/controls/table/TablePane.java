package org.assistant.ui.controls.table;

import javax.swing.*;

public class TablePane<T> extends JScrollPane {

	JTable table;

	public TablePane(ColumnInfo<?>[] columns) {
		ColumnInfoListTableModel model = new ColumnInfoListTableModel(columns);
		table = new JTable(model);
		table.setCellSelectionEnabled(true);
		setViewportView(table);
	}
}
