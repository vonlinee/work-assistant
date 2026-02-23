package org.assistant.tools.doc;

import org.assistant.ui.controls.table.TreeTableModel;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class ApiTreeTableModel extends TreeTableModel {

    public ApiTreeTableModel(TreeNode rootNode) {
        super(rootNode, false);
    }

    @Override
    public Object getColumnValue(TreeNode node, int column) {
        if (node instanceof DefaultMutableTreeNode) {
            Object data = ((DefaultMutableTreeNode) node).getUserObject();
            if (data instanceof ApiGroupWrapper) {
                ApiGroupWrapper gw = (ApiGroupWrapper) data;
                switch (column) {
                    case 0:
                        return "📂 " + gw.group.getName();
                    case 1:
                        return "";
                    case 2:
                        return "";
                    case 3:
                        return gw.isChecked;
                }
            } else if (data instanceof ApiInfoWrapper) {
                ApiInfoWrapper aw = (ApiInfoWrapper) data;
                switch (column) {
                    case 0:
                        return aw.api.getPath();
                    case 1:
                        return aw.api.getMethod().toUpperCase();
                    case 2:
                        return aw.api.getSummary() != null
                                ? aw.api.getSummary() + (aw.api.isDeprecated() ? " (DEPRECATED)" : "")
                                : (aw.api.isDeprecated() ? "DEPRECATED" : "");
                    case 3:
                        return aw.isChecked;
                }
            }
        }
        return null;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        if (column == 3) {
            return Boolean.class; // Renders as Checkbox
        }
        return Object.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 3; // Only the selected checkbox column is editable
    }

    @Override
    public void setColumnValue(TreeNode node, int column, Object value) {
        if (column == 3 && value instanceof Boolean) {
            boolean isChecked = (Boolean) value;
            if (node instanceof DefaultMutableTreeNode) {
                Object data = ((DefaultMutableTreeNode) node).getUserObject();
                if (data instanceof ApiGroupWrapper) {
                    ((ApiGroupWrapper) data).isChecked = isChecked;
                    setChildrenChecked(node, isChecked);
                } else if (data instanceof ApiInfoWrapper) {
                    ((ApiInfoWrapper) data).isChecked = isChecked;
                    updateParentCheckState(node);
                }

                // Notify the table that data has changed. In TreeTableModel we typically just
                // fireTableDataChanged or fire tree events.
                // But TreeTableModel usually listens to TreeModel events. We can just fire tree
                // node changed.
                // However, since we might modify multiple nodes (children), firing table data
                // changed is safest to update the view.
                fireTableDataChanged();
            }
        }
    }

    private void setChildrenChecked(TreeNode parent, boolean checked) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            TreeNode child = parent.getChildAt(i);
            if (child instanceof DefaultMutableTreeNode) {
                Object data = ((DefaultMutableTreeNode) child).getUserObject();
                if (data instanceof ApiInfoWrapper) {
                    ((ApiInfoWrapper) data).isChecked = checked;
                }
            }
            setChildrenChecked(child, checked);
        }
    }

    private void updateParentCheckState(TreeNode child) {
        TreeNode parent = child.getParent();
        if (parent instanceof DefaultMutableTreeNode) {
            Object data = ((DefaultMutableTreeNode) parent).getUserObject();
            if (data instanceof ApiGroupWrapper) {
                boolean allChecked = true;
                for (int i = 0; i < parent.getChildCount(); i++) {
                    TreeNode sibling = parent.getChildAt(i);
                    if (sibling instanceof DefaultMutableTreeNode) {
                        Object siblingData = ((DefaultMutableTreeNode) sibling).getUserObject();
                        if (siblingData instanceof ApiInfoWrapper) {
                            if (!((ApiInfoWrapper) siblingData).isChecked) {
                                allChecked = false;
                                break;
                            }
                        }
                    }
                }
                ((ApiGroupWrapper) data).isChecked = allChecked;
            }
        }
    }

    @Override
    protected TableColumnModel createTableColumnModel() {
        DefaultTableColumnModel model = new DefaultTableColumnModel();

        TableColumn col0 = new TableColumn(0);
        col0.setHeaderValue("API Path");
        col0.setPreferredWidth(350);
        model.addColumn(col0);

        TableColumn col1 = new TableColumn(1);
        col1.setHeaderValue("Method");
        col1.setPreferredWidth(80);
        col1.setMaxWidth(80);
        model.addColumn(col1);

        TableColumn col2 = new TableColumn(2);
        col2.setHeaderValue("Summary");
        col2.setPreferredWidth(250);
        model.addColumn(col2);

        TableColumn col3 = new TableColumn(3);
        col3.setHeaderValue("Selected");
        col3.setPreferredWidth(80);
        col3.setMaxWidth(80);
        model.addColumn(col3);

        return model;
    }

    public static class ApiGroupWrapper {
        public ApiGroup group;
        public boolean isChecked = true;

        public ApiGroupWrapper(ApiGroup group) {
            this.group = group;
        }
    }

    public static class ApiInfoWrapper {
        public WebApiInfo api;
        public boolean isChecked = true;

        public ApiInfoWrapper(WebApiInfo api) {
            this.api = api;
        }
    }
}
