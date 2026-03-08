package org.assistant.tools.mybatis;

import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

import java.util.Arrays;

public class ParamTreeTableModel extends DefaultTreeTableModel {

	public ParamTreeTableModel() {
		super(new ParamNode("Root", "", ParamDataType.UNKNOWN.name()),
				Arrays.asList("Parameter", "JDBC Type", "Data Type", "Value"));
	}

	@Override
	public Object getValueAt(Object node, int column) {
		return switch (column) {
			case 0 -> ((ParamNode) node).getKey();
			case 1 -> ((ParamNode) node).getJdbcType();
			case 2 -> ((ParamNode) node).getDataType();
			case 3 -> ((ParamNode) node).getValue();
			default -> super.getValueAt(node, column);
		};
	}

	@Override
	public boolean isCellEditable(Object node, int column) {
		return isLeaf(node);
	}

	@Override
	public void setValueAt(Object value, Object node, int column) {
		ParamNode paramNode = (ParamNode) node;
		String strVal = value == null ? "" : value.toString();
		switch (column) {
			case 1:
				paramNode.setJdbcType(strVal);
				break;
			case 2:
				paramNode.setDataType(strVal);
				break;
			case 3:
				paramNode.setValue(strVal);
				break;
			default:
				super.setValueAt(value, node, column);
				break;
		}
	}
}
