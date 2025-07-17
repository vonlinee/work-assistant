package org.assistant.tools.excel;

import org.assistant.ui.controls.table.ColumnInfo;
import org.assistant.ui.controls.table.TablePane;

public class ExcelHeaderTable extends TablePane<TableHeader> {

	public ExcelHeaderTable() {
		super(new ColumnInfo[]{
			new ColumnInfo<String>("标题", String.class),
			new ColumnInfo<String>("字段", String.class),
			new ColumnInfo<String>("索引", String.class)
		});
	}
}
