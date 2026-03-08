package org.assistant.tools.mybatis;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.assistant.ui.controls.table.SwingTreeTable;
import org.assistant.ui.pane.BorderPane;

import javax.swing.*;
import java.util.List;

public class ParamTable extends BorderPane {

	private final SwingTreeTable treeTable;
	private final ParamTreeTableModel treeTableModel;

	public ParamTable() {
		this.treeTableModel = new ParamTreeTableModel();
		treeTable = new SwingTreeTable(treeTableModel);
		treeTable.setRowHeight(24);

		// Inject Configurable Dropdowns into Col 1 (JDBC Type) & Col 2 (Data Type)
		refreshTypeEditors();

		JScrollPane scrollPane = new JScrollPane(treeTable);
		setCenter(scrollPane);
	}

	public void refreshTypeEditors() {
		JComboBox<String> jdbcCombo = new JComboBox<>(MyBatisTypeManager.getJdbcTypes());
		treeTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(jdbcCombo));

		JComboBox<String> dataCombo = new JComboBox<>(MyBatisTypeManager.getDataTypes());
		treeTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(dataCombo));
	}

	public void setStatement(MappedStatement ms) {
		if (ms == null) {
			treeTable.updateUI();
			return;
		}
		treeTable.removeAllNodes();
		// Get BoundSql with null parameter to see required mappings
		BoundSql boundSql = ms.getBoundSql(null);
		List<ParameterMapping> mappings = boundSql.getParameterMappings();

		if (mappings != null) {
			for (ParameterMapping pm : mappings) {
				ParamNode node = new ParamNode();
				node.setKey(pm.getProperty());
				node.setJdbcType(pm.getJdbcType() != null ? pm.getJdbcType().name() : "");
				// Guess ParamDataType from javaType
				ParamDataType dataType = ParamDataType.STRING;
				Class<?> javaType = pm.getJavaType();
				if (javaType != null) {
					if (Number.class.isAssignableFrom(javaType) || javaType.isPrimitive()
							&& !javaType.equals(boolean.class) && !javaType.equals(char.class)) {
						dataType = ParamDataType.NUMERIC;
					} else if (javaType.equals(Boolean.class) || javaType.equals(boolean.class)) {
						dataType = ParamDataType.BOOLEAN;
					}
				}
				node.setDataType(dataType.name());
				node.setParameterMapping(pm);
				((ParamNode) treeTableModel.getRoot()).addChild(node);
			}
		}

		treeTable.updateUI();
		treeTable.expandAll();
	}

	public ParamNode getRootNode() {
		return (ParamNode) treeTable.getTreeTableModel().getRoot();
	}

	public void importParameters(java.util.Map<String, String> params) {
		if (params == null || params.isEmpty()) {
			return;
		}

		ParamNode root = getRootNode();
		if (root != null) {
			traverseAndApplyParams(root, params);
			treeTable.updateUI();
		}
	}

	private void traverseAndApplyParams(ParamNode node, java.util.Map<String, String> params) {
		if (node.getKey() != null && params.containsKey(node.getKey())) {
			node.setValue(params.get(node.getKey()));
		}
		// If nested
		for (int i = 0; i < node.getChildCount(); i++) {
			traverseAndApplyParams((ParamNode) node.getChildAt(i), params);
		}
	}
}
