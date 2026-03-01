package org.assistant.tools.excel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TableData {

	/**
	 * 表头
	 */
	List<TableHeader> headers;

	/**
	 * 行数据
	 */
	List<Map<Integer, Object>> rows;

	public List<TableHeader> getHeaders() {
		return headers;
	}

	public void setHeaders(List<TableHeader> headers) {
		this.headers = headers;
	}

	public List<Map<Integer, Object>> getRows() {
		return rows;
	}

	public void setRows(List<Map<Integer, Object>> rows) {
		this.rows = rows;
	}

	public List<List<String>> getHeads() {
		List<List<String>> heads = new ArrayList<>();
		for (TableHeader header : headers) {
			if (header.hasChildren()) {
				// 多级表头，目前支持2级
				for (TableHeader child : header.getChildren()) {
					List<String> head = new ArrayList<>();
					head.add(header.getTitle());
					head.add(child.getTitle());
					heads.add(head);
				}
			} else {
				List<String> head = new ArrayList<>();
				head.add(header.getTitle());
				heads.add(head);
			}
		}
		return heads;
	}
}
