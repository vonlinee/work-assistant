package org.assistant.ui.controls.table;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ColumnInfo<T> {

	private String title;
	private Class<?> type;

	public ColumnInfo(String title, Class<?> type) {
		this.title = title;
		this.type = type;
	}
}
