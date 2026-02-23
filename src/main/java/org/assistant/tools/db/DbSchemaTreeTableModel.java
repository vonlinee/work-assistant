package org.assistant.tools.db;

import org.assistant.tools.db.parser.TableInfo;
import org.assistant.ui.controls.table.TreeTableModel;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class DbSchemaTreeTableModel extends TreeTableModel {

    public DbSchemaTreeTableModel(TreeNode rootNode) {
        super(rootNode, false);
    }

    @Override
    public Object getColumnValue(TreeNode node, int column) {
        if (node instanceof DefaultMutableTreeNode) {
            Object data = ((DefaultMutableTreeNode) node).getUserObject();
            if (data instanceof SchemaWrapper) {
                SchemaWrapper sw = (SchemaWrapper) data;
                switch (column) {
                    case 0:
                        return "🗄 " + sw.schema;
                    case 1:
                        return "";
                    case 2:
                        return sw.isChecked;
                }
            } else if (data instanceof TableWrapper) {
                TableWrapper tw = (TableWrapper) data;
                switch (column) {
                    case 0:
                        return "📄 " + tw.table.getName();
                    case 1:
                        return tw.table.getRemarks() != null ? tw.table.getRemarks() : "";
                    case 2:
                        return tw.isChecked;
                }
            }
        }
        return null;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        if (column == 2) {
            return Boolean.class;
        }
        return Object.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 2;
    }

    @Override
    public void setColumnValue(TreeNode node, int column, Object value) {
        if (column == 2 && value instanceof Boolean) {
            boolean isChecked = (Boolean) value;
            if (node instanceof DefaultMutableTreeNode) {
                Object data = ((DefaultMutableTreeNode) node).getUserObject();
                if (data instanceof SchemaWrapper) {
                    ((SchemaWrapper) data).isChecked = isChecked;
                    setChildrenChecked(node, isChecked);
                } else if (data instanceof TableWrapper) {
                    ((TableWrapper) data).isChecked = isChecked;
                    updateParentCheckState(node);
                }
                fireTableDataChanged();
            }
        }
    }

    private void setChildrenChecked(TreeNode parent, boolean checked) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            TreeNode child = parent.getChildAt(i);
            if (child instanceof DefaultMutableTreeNode) {
                Object data = ((DefaultMutableTreeNode) child).getUserObject();
                if (data instanceof TableWrapper) {
                    ((TableWrapper) data).isChecked = checked;
                }
            }
            setChildrenChecked(child, checked);
        }
    }

    private void updateParentCheckState(TreeNode child) {
        TreeNode parent = child.getParent();
        if (parent instanceof DefaultMutableTreeNode) {
            Object data = ((DefaultMutableTreeNode) parent).getUserObject();
            if (data instanceof SchemaWrapper) {
                boolean allChecked = true;
                for (int i = 0; i < parent.getChildCount(); i++) {
                    TreeNode sibling = parent.getChildAt(i);
                    if (sibling instanceof DefaultMutableTreeNode) {
                        Object siblingData = ((DefaultMutableTreeNode) sibling).getUserObject();
                        if (siblingData instanceof TableWrapper) {
                            if (!((TableWrapper) siblingData).isChecked) {
                                allChecked = false;
                                break;
                            }
                        }
                    }
                }
                ((SchemaWrapper) data).isChecked = allChecked;
            }
        }
    }

    @Override
    protected TableColumnModel createTableColumnModel() {
        DefaultTableColumnModel model = new DefaultTableColumnModel();

        TableColumn col0 = new TableColumn(0);
        col0.setHeaderValue("Database / Table Name");
        col0.setPreferredWidth(300);
        model.addColumn(col0);

        TableColumn col1 = new TableColumn(1);
        col1.setHeaderValue("Description");
        col1.setPreferredWidth(250);
        model.addColumn(col1);

        TableColumn col2 = new TableColumn(2);
        col2.setHeaderValue("Selected");
        col2.setPreferredWidth(80);
        col2.setMaxWidth(80);
        model.addColumn(col2);

        return model;
    }

    public static class SchemaWrapper {
        public String schema;
        public boolean isChecked = true;

        public SchemaWrapper(String schema) {
            this.schema = schema;
        }
    }

    public static class TableWrapper {
        public TableInfo table;
        public boolean isChecked = true;

        public TableWrapper(TableInfo table) {
            this.table = table;
        }
    }
}
