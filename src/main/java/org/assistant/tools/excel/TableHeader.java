package org.assistant.tools.excel;

import lombok.Data;

import java.util.List;

@Data
public class TableHeader {

	/**
	 * 标题文本
	 */
	private String title;

	/**
	 * 字段名
	 */
	private String field;

	/**
	 * 从0开始
	 */
	private int columnNum;

	/**
	 * 列编号，对于普通Excel的编号是从A开始
	 */
	private String columnNo;

	/**
	 * 子标题
	 */
	private List<TableHeader> children;

	public boolean hasChildren() {
		return children != null && !children.isEmpty();
	}
}
