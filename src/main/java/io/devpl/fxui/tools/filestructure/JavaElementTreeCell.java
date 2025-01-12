package io.devpl.fxui.tools.filestructure;

import io.devpl.fxui.controller.fields.FieldsManageView;
import io.devpl.fxui.fxtras.utils.StageManager;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.util.converter.DefaultStringConverter;

/**
 * 单元格
 */
public class JavaElementTreeCell extends TextFieldTreeCell<String> {

    public JavaElementTreeCell() {
        super(new DefaultStringConverter());
        // 初始化单元格的菜单项
        this.treeItemProperty().addListener((observable, oldValue, newValue) -> initializeTreeItemContextMenuIfNeeded(newValue));
    }

    private void initializeTreeItemContextMenuIfNeeded(TreeItem<String> treeItem) {
        ContextMenu contextMenu = getContextMenu();
        if (contextMenu != null) {
            return;
        }
        if (treeItem instanceof TopLevelClassItem) {
            contextMenu = initClassContextMenu();
        } else if (treeItem instanceof MethodItem) {
            contextMenu = initMethodContextMenu((MethodItem) treeItem);
        } else if (treeItem instanceof FieldItem) {
            contextMenu = initFieldContextMenu((FieldItem) treeItem);
        }
        setContextMenu(contextMenu);
    }

    /**
     * 更新选择项
     * 当从TreeItem A切换到另一个TreeItem B时，需要更新 A 的选中状态为false，B的状态为true，因此会调用2次
     *
     * @param selected whether to select this cell.
     */
    @Override
    public void updateSelected(boolean selected) {
        super.updateSelected(selected);
    }

    private ContextMenu initClassContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem addMethodMenu = new MenuItem("添加方法");
        addMethodMenu.setOnAction(event -> {
            MethodItem methodItem = new MethodItem();
            methodItem.setValue("New Method");
            TreeItem<String> selectedItem = getTreeView().getSelectionModel().getSelectedItem();
            selectedItem.getChildren().add(methodItem);
        });
        MenuItem addFieldMenu = new MenuItem("添加字段");
        addFieldMenu.setOnAction(event -> {
            FieldItem fieldItem = new FieldItem();
            fieldItem.setValue("New Field");
            TreeItem<String> selectedItem = getTreeView().getSelectionModel().getSelectedItem();
            selectedItem.getChildren().add(fieldItem);
        });
        MenuItem chooseFieldMenu = new MenuItem("选择字段");
        chooseFieldMenu.setOnAction(event -> {
            StageManager.show(FieldsManageView.class);
        });
        MenuItem deleteThisItem = new MenuItem("删除");
        deleteThisItem.setOnAction(event -> {
            TreeItem<String> selectedItem = getTreeView().getSelectionModel().getSelectedItem();
            TreeItem<String> parent = selectedItem.getParent();
            if (parent != null) {
                parent.getChildren().remove(selectedItem);
            }
        });
        contextMenu.getItems().addAll(addMethodMenu, addFieldMenu, chooseFieldMenu, deleteThisItem);
        setContextMenu(contextMenu);
        return contextMenu;
    }

    private ContextMenu initMethodContextMenu(MethodItem methodItem) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editMenu = new MenuItem("编辑");
        MenuItem deleteThisItem = new MenuItem("删除");
        deleteThisItem.setOnAction(event -> {
            TreeItem<String> selectedItem = getTreeView().getSelectionModel().getSelectedItem();
            TreeItem<String> parent = selectedItem.getParent();
            if (parent != null) {
                parent.getChildren().remove(selectedItem);
            }
        });

        contextMenu.getItems().add(editMenu);
        contextMenu.getItems().add(deleteThisItem);
        setContextMenu(contextMenu);
        return contextMenu;
    }

    private ContextMenu initFieldContextMenu(FieldItem fieldItem) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editMenu = new MenuItem("编辑");
        MenuItem deleteThisItem = new MenuItem("删除");
        deleteThisItem.setOnAction(event -> {
            TreeItem<String> selectedItem = getTreeView().getSelectionModel().getSelectedItem();
            TreeItem<String> parent = selectedItem.getParent();
            if (parent != null) {
                parent.getChildren().remove(selectedItem);
            }
        });

        contextMenu.getItems().add(editMenu);
        contextMenu.getItems().add(deleteThisItem);
        setContextMenu(contextMenu);
        return contextMenu;
    }

    /**
     * 更新单元格时触发
     *
     * @param item  The new item for the cell.
     * @param empty whether this cell represents data from the list. If it
     *              is empty, then it does not represent any domain data, but is a cell
     *              being used to render an "empty" row.
     */
    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
        } else {
            setText(item);
        }
    }
}
