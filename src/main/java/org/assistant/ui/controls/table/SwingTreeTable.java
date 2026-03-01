package org.assistant.ui.controls.table;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

public class SwingTreeTable extends JXTreeTable {

	private final DefaultTreeTableModel tableModel;

	public SwingTreeTable(DefaultTreeTableModel tableModel) {
		super(tableModel);
		this.tableModel = tableModel;
	}

	public void removeAllNodes() {
		DefaultMutableTreeTableNode root = (DefaultMutableTreeTableNode) tableModel.getRoot();
		for (int i = 0; i < root.getChildCount(); i++) {
			root.remove(i);
		}
	}
}
