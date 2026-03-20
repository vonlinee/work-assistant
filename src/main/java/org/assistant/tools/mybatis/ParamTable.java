package org.assistant.tools.mybatis;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.assistant.ui.ExceptionDialog;
import org.assistant.ui.controls.table.SwingTreeTable;
import org.assistant.ui.pane.BorderPane;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		BoundSql boundSql;
		try {
			boundSql = ms.getBoundSql(null);
		} catch (Exception e) {
			ExceptionDialog.showError(this, e);
			return;
		}

		List<ParameterMapping> mappings = boundSql.getParameterMappings();

		if (mappings != null) {
			ParamNode root = getRootNode();
			for (ParameterMapping pm : mappings) {
				ParamNode node = getOrCreatePath(root, pm.getProperty(), false);
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
			}
		}

		treeTable.updateUI();
		treeTable.expandAll();
	}

	public ParamNode getRootNode() {
		return (ParamNode) treeTable.getTreeTableModel().getRoot();
	}

	public void importParameters(Map<String, String> params, boolean override) {
		if (params == null || params.isEmpty()) {
			return;
		}

		ParamNode root = getRootNode();
		if (root != null) {
			if (override) {
				clearValues(root);
			}
			for (Map.Entry<String, String> entry : params.entrySet()) {
				String key = entry.getKey();
				String mapValue = entry.getValue();

				// Skip empty values during merge if they're coming from URL keys without values
				if (!override && (mapValue == null || mapValue.isEmpty())) {
					continue;
				}

				ParamNode leaf = getOrCreatePath(root, key, !override);
				leaf.setValue(mapValue);
			}
			treeTable.updateUI();
			treeTable.expandAll();
		}
	}

	private void clearValues(ParamNode node) {
		node.setValue("");
		for (int i = 0; i < node.getChildCount(); i++) {
			clearValues((ParamNode) node.getChildAt(i));
		}
	}

	private ParamNode getOrCreatePath(ParamNode parent, String fullKey, boolean mergeExistingOnly) {
		List<String> parts = new ArrayList<>();
		Matcher m = Pattern.compile("([^\\[\\]\\.]+)|(\\[[0-9]+\\])").matcher(fullKey);
		while (m.find()) {
			parts.add(m.group());
		}

		ParamNode current = parent;

		for (int i = 0; i < parts.size(); i++) {
			String part = parts.get(i);
			ParamNode child = findChildByKey(current, part);

			if (child == null) {
				// If we are in merge mode, we don't automatically create undefined nested
				// properties
				// unless explicitly allowed. For now, since it mimics standard import behavior,
				// if mergeExistingOnly is true and the leaf doesn't exist, we still create it
				// to capture
				// the import value, but it ensures we aren't completely deleting the tree
				child = new ParamNode();
				child.setKey(part);
				child.setDataType(ParamDataType.STRING.name());
				treeTableModel.insertNodeInto(child, current, current.getChildCount());
			}

			current = child;
		}
		return current;
	}

	private ParamNode findChildByKey(ParamNode parent, String key) {
		if (parent.getChildCount() == 0)
			return null;
		for (int i = 0; i < parent.getChildCount(); i++) {
			ParamNode child = (ParamNode) parent.getChildAt(i);
			if (key.equals(child.getKey())) {
				return child;
			}
		}
		return null;
	}
}
