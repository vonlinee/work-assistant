package org.assistant.ui.controls.table;

import javax.swing.table.DefaultTableModel;

public class ColumnInfoListTableModel extends DefaultTableModel {

	ColumnInfo<?>[] columns;

	public ColumnInfoListTableModel(ColumnInfo<?>[] columns) {
		this.columns = columns;
		for (ColumnInfo<?> column : columns) {
			addColumn(column.getTitle());
		}
	}
}
