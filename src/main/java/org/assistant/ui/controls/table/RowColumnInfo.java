package org.assistant.ui.controls.table;

public abstract class RowColumnInfo<R, T> extends ColumnInfo<T> {

	public RowColumnInfo(String title, Class<?> type) {
		super(title, type);
	}

	public abstract T getValue(R row);
}
