package org.assistant.ui.controls.table;

/**
 * This class represents a row in a table that models a tree structure,
 * containing an identifier, a parent identifier, and additional column data.
 */
public class TreeTableRow {

	private Object id;
	private Object parentId;
	private Object[] otherColumns;

	public TreeTableRow() {
	}

	public TreeTableRow(final Object id, final Object parentId, final Object[] otherColumns) {
		this.id = id;
		this.parentId = parentId;
		this.otherColumns = otherColumns;
	}

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
	}

	public Object getParentId() {
		return parentId;
	}

	public void setParentId(Object parentId) {
		this.parentId = parentId;
	}

	public Object[] getRowData() {
		return otherColumns;
	}

	public void setRowData(Object[] otherColumns) {
		this.otherColumns = otherColumns;
	}
}