package io.devpl.fxui.controller.fields;

import io.devpl.fxui.controls.MFXTextFieldTreeTableCell;
import io.devpl.fxui.model.FieldNode;
import io.devpl.fxui.utils.FXUtils;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.converter.DefaultStringConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 字段表
 */
public class FieldTreeTable extends TreeTableView<FieldNode> {

    private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
    private TreeItem<FieldNode> rootNode;

    public FieldTreeTable() {
        // 自动拉伸长度撑满整个表格
        setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        setEditable(true);

        TreeTableColumn<FieldNode, String> nameCol = new TreeTableColumn<>("名称");
        TreeTableColumn<FieldNode, String> dataTypeCol = new TreeTableColumn<>("数据类型");
        TreeTableColumn<FieldNode, String> descCol = new TreeTableColumn<>("描述信息");

        nameCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        dataTypeCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("dataType"));
        descCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("description"));

        nameCol.setCellFactory(ttc -> new MFXTextFieldTreeTableCell<>(new DefaultStringConverter()));
        descCol.setCellFactory(ttc -> new MFXTextFieldTreeTableCell<>(new DefaultStringConverter()));
        this.getColumns().add(nameCol);
        this.getColumns().add(dataTypeCol);
        this.getColumns().add(descCol);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem addMenuItem = new MenuItem("新增");
        addMenuItem.setMnemonicParsing(true);
        addMenuItem.setOnAction(e -> {
            FieldNode newNode = new FieldNode("A");
            newNode.setName("New Field");
            newNode.setName("New Field");
            newNode.setName("String");
            rootNode.getChildren().add(new TreeItem<>(newNode));
        });
        contextMenu.getItems().add(addMenuItem);
        contextMenu.setWidth(300);
        contextMenu.setPrefWidth(300);
        setContextMenu(contextMenu);

        setShowRoot(false);
        setRoot(rootNode = new TreeItem<>(null));

        this.setRowFactory(ttv -> {
            TreeTableRow<FieldNode> row = new TreeTableRow<>();

            if (rowContextMenu == null) {
                rowContextMenu = new RowContextMenu();
            }
            // 给行添加右键菜单
            row.setOnContextMenuRequested(event -> {
                @SuppressWarnings("unchecked")
                TreeTableRow<FieldNode> curRow = (TreeTableRow<FieldNode>) event.getSource();
                if (curRow.isEmpty()) {
                    return;
                }
                rowContextMenu.curRow = curRow;
                // 阻止 ContextMenuRequested 事件冒泡
                event.consume();
            });
            row.setContextMenu(rowContextMenu);
            enableDrag(row);
            return row;
        });
        expandAll();
    }

    RowContextMenu rowContextMenu;

    static class RowContextMenu extends ContextMenu {

        TreeTableRow<FieldNode> curRow;

        public RowContextMenu() {
            MenuItem menuItem1 = new MenuItem("添加子节点");
            menuItem1.setOnAction(e -> {
                if (!FXUtils.isRowEmpty(curRow)) {
                    curRow.getTreeItem().getChildren().add(new TreeItem<>(new FieldNode("Unknown", "", "")));
                    curRow.getTreeItem().setExpanded(true);
                }
            });
            MenuItem menuItem2 = new MenuItem("添加同级节点");
            menuItem2.setOnAction(e -> {
                if (curRow != null && !curRow.isEmpty()) {
                    curRow.getTreeItem().getParent().getChildren().add(new TreeItem<>(new FieldNode("Unknown")));
                }
            });
            this.getItems().addAll(menuItem1, menuItem2);
        }
    }

    private void enableDrag(TreeTableRow<FieldNode> row) {
        // 拖拽-检测
        row.setOnDragDetected(event -> {
            TreeTableRow<?> draggedRow = (TreeTableRow<?>) event.getSource();
            if (!draggedRow.isEmpty()) {
                // 开始拖拽
                Dragboard db = draggedRow.startDragAndDrop(TransferMode.MOVE);
                db.setDragView(draggedRow.snapshot(null, null));
                ClipboardContent cc = new ClipboardContent();
                // 记录拖拽开始时的位置 区分展开
                cc.put(SERIALIZED_MIME_TYPE, draggedRow.getIndex());

                db.setContent(cc);
                event.consume();
            }
        });
        // 释放-验证 当你拖动到目标上方的时候，会不停的执行
        row.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            @SuppressWarnings("unchecked")
            TreeTableRow<FieldNode> sourceRow = (TreeTableRow<FieldNode>) event.getGestureSource();
            @SuppressWarnings("unchecked")
            TreeTableRow<FieldNode> toRow = (TreeTableRow<FieldNode>) event.getSource();
            if (sourceRow.getTreeTableView() != toRow.getTreeTableView()) {
                // 检查是否是同一个表，不能跨表拖拽
                return;
            }
            if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                // 拖拽到的行不是拖拽起始行
                if (toRow.getIndex() != (Integer) db.getContent(SERIALIZED_MIME_TYPE)) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    markDroppedTargetRow(toRow);
                    event.consume();
                }
            }
        });
        /**
         * 拖动到目标并松开鼠标的时候，执行这个DragDropped事件。
         * DRAG_DROPPED只能在setOnDragDropped方法里调用
         */
        row.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                // 开始拖拽时的行
                @SuppressWarnings("unchecked")
                TreeTableRow<FieldNode> fromRow = (TreeTableRow<FieldNode>) event.getGestureSource();
                // 拖拽开始的索引位置
                // int draggedIndex = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
                // 拖拽到的行
                @SuppressWarnings("unchecked")
                TreeTableRow<FieldNode> toRow = (TreeTableRow<FieldNode>) event.getSource();

                final TreeTableView<FieldNode> tableView = fromRow.getTreeTableView();

                if (toRow == null || toRow.isEmpty()) {
                    // 添加到最外层(根节点)
                    TreeItem<FieldNode> parentNode = fromRow.getTreeItem().getParent();
                    TreeItem<FieldNode> removedItem = parentNode.getChildren().remove(calculateItemIndex(fromRow.getTreeItem()));
                    if (parentNode != rootNode) {
                        // 如果父节点不是根节点，移动到空白区域时视为移到最外层
                        rootNode.getChildren().add(removedItem);
                    } else {
                        // 移到最后
                        parentNode.getChildren().add(removedItem);
                    }
                    return;
                }
                // 位置不变 目标位置为原位置的父级节点
                if (toRow.getTreeItem() == fromRow.getTreeItem().getParent()) {
                    return;
                }

                // 拖拽位置没发生变化不会触发  不会有 fromRow == toRow 的情况
                if (fromRow.getTreeItem().getParent() == toRow.getTreeItem().getParent()) {
                    // 父级相同
                } else {

                    int fromLevel = tableView.getTreeItemLevel(fromRow.getTreeItem());
                    int toLevel = tableView.getTreeItemLevel(toRow.getTreeItem());
                    // System.out.println("层级" + fromLevel + " -> " + toLevel);
                    if (toLevel > fromLevel) {
                        // 判断是否父节点拖到其子孙节点
                        TreeItem<FieldNode> parent = toRow.getTreeItem().getParent();
                        for (int i = toLevel; i > fromLevel; i--) {
                            if (parent == fromRow.getTreeItem()) {
                                // System.out.println("父节点拖到其子孙节点");
                                return;
                            }
                            parent = parent.getParent();
                        }
                    }


                }

                // System.out.println("从" + fromRow.getIndex() + "拖到" + toRow.getIndex());

                // 连同所有子节点一起移动
                TreeItem<FieldNode> removedItem = remove(fromRow.getTreeItem());
                if (removedItem != null) {
                    toRow.getTreeItem().getChildren().add(removedItem);
                }

                // 添加到子节点

                // 表格如果展开，则索引为所有行的索引位置

                // 移除拖拽节点

                toRow.getTreeItem().setExpanded(true);

                event.consume();
            }
        });

        row.setOnDragDone(event -> {

        });
    }

    public final void expandAll() {
        FXUtils.expandAll(rootNode);
    }

    private void markDroppedTargetRow(TreeTableRow<?> row) {

    }

    public void addFields(Collection<FieldNode> nodeList) {
        for (FieldNode fieldNode : nodeList) {
            TreeItem<FieldNode> item = new TreeItem<>(fieldNode);
            rootNode.getChildren().add(item);
            add(item, fieldNode);
        }
    }

    public void clearAll() {
        rootNode.getChildren().clear();
    }

    private void add(TreeItem<FieldNode> parent, FieldNode parentItem) {
        if (parentItem.hasChildren()) {
            for (FieldNode child : parentItem.getChildren()) {
                TreeItem<FieldNode> cur = new TreeItem<>(child);
                parent.getChildren().add(cur);
                add(cur, child);
            }
        }
    }

    /**
     * 删除子节点
     *
     * @param item 子节点
     * @return 被删除的子节点，如果为null表示未删除
     */
    private <T> TreeItem<T> remove(TreeItem<T> item) {
        int index = item.getParent().getChildren().indexOf(item);
        if (index >= 0) {
            return item.getParent().getChildren().remove(index);
        }
        return null;
    }

    /**
     * 计算在父节点中的位置
     *
     * @param item 节点
     * @return 在父节点中的位置
     */
    private int calculateItemIndex(TreeItem<?> item) {
        return item.getParent().getChildren().indexOf(item);
    }

    public final List<FieldNode> getFields() {
        // 根节点不存放数据
        List<FieldNode> nodes = new ArrayList<>();
        ObservableList<TreeItem<FieldNode>> children = rootNode.getChildren();
        for (TreeItem<FieldNode> child : children) {
            nodes.add(child.getValue());
            getChildrenField(child.getValue(), child);
        }
        return nodes;
    }

    private void getChildrenField(FieldNode parent, TreeItem<FieldNode> parentItem) {
        List<FieldNode> fields = new ArrayList<>();
        for (TreeItem<FieldNode> child : parentItem.getChildren()) {
            getChildrenField(child.getValue(), child);
            fields.add(child.getValue());
        }
        parent.setChildren(fields);
    }
}
