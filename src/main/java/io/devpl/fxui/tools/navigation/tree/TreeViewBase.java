package io.devpl.fxui.tools.navigation.tree;

import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.util.StringConverter;

public abstract class TreeViewBase<T extends TreeItemObject> extends TreeView<T> {

    class TreeItemObjectConverter extends StringConverter<T> {

        private final TreeView<T> treeView;

        public TreeItemObjectConverter(TreeView<T> treeView) {
            this.treeView = treeView;
        }

        @Override
        public String toString(T object) {
            return object.getDisplayValue();
        }

        @Override
        public T fromString(String value) {
            if (value == null) {
                return null;
            }
            T selectedObject = treeView.getSelectionModel().getSelectedItem().getValue();
            if (selectedObject == null) {
                return null;
            }
            selectedObject.setDisplayValue(value);
            return selectedObject;
        }
    }

    class TreeCellBase extends TreeCell<T> {

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            // If there is no information for the Cell, make it empty
            if (empty) {
                setGraphic(null);
                setText(null);
                // Otherwise if it's not representation as an item of the tree
                // is not a CheckBoxTreeItem, remove the checkbox item
            } else if (!(getTreeItem() instanceof CheckBoxTreeItem)) {
                setGraphic(null);
            }
        }
    }

    protected TreeViewBase() {
        setRoot(new TreeItem<>());
        setShowRoot(false);
    }

    /**
     * 初始化单元格
     *
     * @param treeCell 单元格
     */
    protected abstract void initializeTreeCell(TextFieldTreeCell<T> treeCell);
}
