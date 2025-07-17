package org.assistant.ui.controls.table;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A class that implements a double click listener for JTableHeaders to toggle sorting.
 * <p>
 * The built-in mouse listener used by default in many Look and Feels in Swing descends from
 * the listener defined in the {@link javax.swing.plaf.basic.BasicTableHeaderUI} class.
 * This explicitly does not respond to double-clicks - or quadruple clicks - or any even number of clicks.
 * It will only trigger a sort when the click == 1, or click == 3 - all the odd clicks.
 * <p>
 * This listener plugs that gap, by also triggering a sort on the even clicks.  Adding this
 * listener to the table header component will cause the header to sort on all clicks.  It will
 * then respond to the odd clicks with the original listener, and to the even ones using this one.
 * <p>
 * <pre>
 *   JTableHeader header = table.getTableHeader();
 *   header.addMouseListener(new TableUtils.HeaderDoubleClickSortMouseListener(header));
 * </pre>
 */
public class HeaderSortMouseDoubleClickListener extends MouseAdapter {

	private final JTableHeader header;

	public HeaderSortMouseDoubleClickListener(JTableHeader header) {
		this.header = header;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() % 2 == 0 && SwingUtilities.isLeftMouseButton(e) && header.isEnabled()) {
			TableUtils.toggleSortColumn(header.getTable(), header.columnAtPoint(e.getPoint()));
		}
	}
}