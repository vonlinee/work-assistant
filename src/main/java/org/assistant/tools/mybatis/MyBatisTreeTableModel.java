package org.assistant.tools.mybatis;

import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

public class MyBatisTreeTableModel extends DefaultTreeTableModel {

	private static final String[] COLUMN_NAMES = { "Statement / Namespace", "Type", "Source File" };

	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	@Override
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}

	@Override
	public Object getValueAt(Object node, int column) {
		if (node instanceof MyBatisNode myNode) {
			switch (column) {
				case 0:
					return myNode.getIdOrNamespace();
				case 1:
					return myNode.getType();
				case 2:
					return myNode.getSourceFile();
			}
		}
		return super.getValueAt(node, column);
	}

	@Override
	public boolean isCellEditable(Object node, int column) {
		return false;
	}
}
