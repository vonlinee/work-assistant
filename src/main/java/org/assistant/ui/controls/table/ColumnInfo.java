package org.assistant.ui.controls.table;

public class ColumnInfo<T> {

	private String title;
	private Class<?> type;

	public ColumnInfo(String title, Class<?> type) {
		this.title = title;
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}
}
