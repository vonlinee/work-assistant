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
		while (root.getChildCount() > 0) {
			tableModel.removeNodeFromParent((org.jdesktop.swingx.treetable.MutableTreeTableNode) root.getChildAt(0));
		}
	}
}
