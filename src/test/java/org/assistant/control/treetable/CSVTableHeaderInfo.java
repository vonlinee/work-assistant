package org.assistant.control.treetable;

/**
 * Represents information about the header of a CSV table.
 * This class stores details about the headers and identifies specific columns
 * by their indices, including unique identifier and parent identifier columns.
 */
public final class CSVTableHeaderInfo {
	private final Object[] headers;
	private final int idColumnIndex;
	private final int parentIdColumnIndex;

	public CSVTableHeaderInfo(Object[] headers, int idColumnIndex, int parentIdColumnIndex) {
		this.headers = headers;
		this.idColumnIndex = idColumnIndex;
		this.parentIdColumnIndex = parentIdColumnIndex;
	}

	public Object[] getHeaders() {
		return headers;
	}

	public int getIdColumnIndex() {
		return idColumnIndex;
	}

	public int getParentIdColumnIndex() {
		return parentIdColumnIndex;
	}
}
