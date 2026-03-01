package org.assistant.tools.excel;

import java.util.List;

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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public int getColumnNum() {
		return columnNum;
	}

	public void setColumnNum(int columnNum) {
		this.columnNum = columnNum;
	}

	public String getColumnNo() {
		return columnNo;
	}

	public void setColumnNo(String columnNo) {
		this.columnNo = columnNo;
	}

	public List<TableHeader> getChildren() {
		return children;
	}

	public void setChildren(List<TableHeader> children) {
		this.children = children;
	}

	public boolean hasChildren() {
		return children != null && !children.isEmpty();
	}
}
