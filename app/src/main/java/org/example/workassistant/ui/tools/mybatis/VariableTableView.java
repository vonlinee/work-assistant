package org.example.workassistant.ui.tools.mybatis;

import org.example.workassistant.ui.model.CommonJavaType;
import org.example.workassistant.utils.StringConverters;
import org.example.workassistant.utils.util.Visitor;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.ComboBoxTreeTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.util.Callback;

/**
 * 变量表，树形结构，比如
 * user.name, user.age
 * 包含固定的3列
 * 变量名称 变量值 变量值的数据类型
 */
public class VariableTableView extends TreeTableView<VarItem> {

    private final TreeItem<VarItem> root;

    public VariableTableView() {
        setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        setEditable(true);
        setShowRoot(false);
        setRoot(root = new TreeItem<>());
        TreeTableColumn<VarItem, String> col_name = new TreeTableColumn<>("名称");
        col_name.setCellValueFactory(param -> {
            TreeItem<VarItem> item = param.getValue();
            if (item == null) {
                return new SimpleStringProperty();
            }
            VarItem value = item.getValue();
            if (value == null) {
                return new SimpleStringProperty();
            }
            return new SimpleStringProperty(value.getName());
        });
        TreeTableColumn<VarItem, Object> col_value = new TreeTableColumn<>("值");

        setRowFactory(new Callback<>() {
            @Override
            public TreeTableRow<VarItem> call(TreeTableView<VarItem> param) {
                TreeTableRow<VarItem> row = new TreeTableRow<>() {
                    @Override
                    public void startEdit() {
                        TreeItem<VarItem> treeItem = getTreeItem();
                        if (treeItem.getChildren().isEmpty()) {
                            super.startEdit();
                        }
                    }
                };
                row.setEditable(true);
                return row;
            }
        });

        col_value.setCellValueFactory(param -> {
            TreeItem<VarItem> item = param.getValue();
            if (item == null) {
                return new SimpleObjectProperty<>();
            }
            // 目录节点
            if (!item.getChildren().isEmpty()) {
                return new SimpleObjectProperty<>();
            }
            VarItem value = item.getValue();
            if (value == null) {
                return new SimpleObjectProperty<>();
            }
            return value.valueProperty();
        });
        col_value.setCellFactory(param -> {
            TextFieldTreeTableCell<VarItem, Object> cell = new TextFieldTreeTableCell<>(StringConverters.forType(Object.class, Object::toString, t -> t)) {
                @Override
                public void startEdit() {
                    TreeTableRow<VarItem> row = this.getTableRow();
                    TreeItem<VarItem> treeItem = row.getTreeItem();
                    if (treeItem.getChildren().isEmpty()) {
                        // 只有叶子结点可编辑
                        super.startEdit();
                    }
                }
            };
            cell.setEditable(true);
            return cell;
        });
        col_value.setEditable(true);

        col_value.setOnEditCommit(event -> {
            Object newValue = event.getNewValue();
            TreeItem<VarItem> rowValue = event.getRowValue();
            VarItem value = rowValue.getValue();
            // 手动绑定数据
            if (value != null) {
                value.setValue(newValue);
            }
        });

        TreeTableColumn<VarItem, CommonJavaType> col_dataType = new TreeTableColumn<>("数据类型");
        col_dataType.setCellValueFactory(param -> {
            TreeItem<VarItem> treeItem = param.getValue();
            if (treeItem == null) {
                return null;
            }
            VarItem value = treeItem.getValue();
            if (value == null) {
                return null;
            }
            return value.typeProperty();
        });
        col_dataType.setCellFactory(param -> {
            ComboBoxTreeTableCell<VarItem, CommonJavaType> cell = new ComboBoxTreeTableCell<>() {
                @Override
                public void startEdit() {
                    TreeTableRow<VarItem> row = this.getTableRow();
                    TreeItem<VarItem> treeItem = row.getTreeItem();
                    if (treeItem.getChildren().isEmpty()) {
                        // 只有叶子结点可编辑
                        super.startEdit();
                    }
                }
            };
            cell.setEditable(true);
            cell.setComboBoxEditable(true);
            cell.setConverter(StringConverters.forType(CommonJavaType.class, CommonJavaType::getQualifier, string -> {
                if (string == null || string.isEmpty()) {
                    return CommonJavaType.STRING;
                }
                return CommonJavaType.valueOfQulifiedName(string);
            }));
            cell.getItems().addAll(CommonJavaType.values());
            return cell;
        });

        getColumns().add(col_name);
        getColumns().add(col_value);
        getColumns().add(col_dataType);
    }

    public <T> void addItems(TreeNode<T> root) {
        // 去掉根节点
        for (TreeNode<T> child : root.getChildren()) {
            child.accept(new MyVisitor<>(this.root));
        }
    }

    public static class MyVisitor<T> implements Visitor<T> {

        TreeItem<VarItem> parentItem;

        public MyVisitor(TreeItem<VarItem> root) {
            this.parentItem = root;
        }

        @Override
        public Visitor<T> visitTree(TreeNode<T> tree) {
            return new MyVisitor<>(this.parentItem);
        }

        /**
         * 当前节点
         * @param parent 父节点
         * @param data   当前节点的数据
         */
        @Override
        public void visitData(TreeNode<T> parent, T data) {
            // 当前节点
            TreeItem<VarItem> current = new TreeItem<>(new VarItem(String.valueOf(data), null));
            this.parentItem.getChildren().add(current);
            // 默认展开
            current.setExpanded(true);
            if (parent.hasChildren()) {
                // 向子树遍历
                this.parentItem = current;
            }
        }
    }

    public final void clear() {
        this.root.getChildren().clear();
    }
}
