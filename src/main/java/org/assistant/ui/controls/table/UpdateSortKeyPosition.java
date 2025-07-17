package org.assistant.ui.controls.table;

/**
 * How to update the position of an existing sort key when it is toggled.
 */
public enum UpdateSortKeyPosition {
	/**
	 * Make the sort key the first sorted key.
	 */
	MAKE_FIRST,

	/**
	 * Don't move the sort key from it's current position.
	 */
	KEEP_POSITION
}