package org.assistant.ui.controls.table;

/**
 * The action to take when removing a sort key.
 */
public enum RemoveSortKeyAction {
	/**
	 * Removes the sort key from the list of sort keys. This is the default.
	 */
	REMOVE,

	/**
	 * Removes the sort key from the list of sort keys, and also any subsequent keys defined.
	 * If we have 3 sort keys, and we make the second one unsorted, the second and third sort key would
	 * be removed from the list.
	 */
	REMOVE_SUBSEQUENT,

	/**
	 * Clears all sort keys.
	 */
	REMOVE_ALL
}