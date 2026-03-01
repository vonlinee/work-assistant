package org.assistant.tools.mybatis;

import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

public class MyBatisTreeTableModel extends DefaultTreeTableModel  {

	@Override
	public boolean isCellEditable(Object node, int column) {
		return false;
	}
}
