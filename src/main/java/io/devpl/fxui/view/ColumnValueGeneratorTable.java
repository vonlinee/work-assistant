package io.devpl.fxui.view;

import io.devpl.fxui.components.pane.RouterPane;
import io.devpl.fxui.components.TreeTableColumnEditEvent;
import io.devpl.fxui.utils.CellUtils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.cell.ChoiceBoxTreeTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Objects;

public class ColumnValueGeneratorTable extends TreeTableView<Row> {

    public ColumnValueGeneratorTable(RouterPane pane, List<String> list) {
        TreeItem<Row> root = new TreeItem<>();
        setRoot(root);
        setShowRoot(false);
        setEditable(true);
        setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);

        TreeTableColumn<Row, String> col1 = new TreeTableColumn<>("Attribute");
        col1.setCellValueFactory(new TreeItemPropertyValueFactory<>("fieldName"));
        col1.setEditable(false);
        col1.setCellFactory(param -> {
            TextFieldTreeTableCell<Row, String> cell = new TextFieldTreeTableCell<>();
            cell.setOnMouseClicked(event -> {
                TreeItem<Row> treeItem = cell.getTableRow().getTreeItem();
                if (treeItem != null) {
                    Row value = treeItem.getValue();
                    if (value != null && value.getGeneratorName() != null) {
                        pane.setCurrentRoute(value.getGeneratorName());
                    }
                }
            });
            return cell;
        });

        TreeTableColumn<Row, String> col2 = new TreeTableColumn<>("Generator");
        col2.setCellValueFactory(new TreeItemPropertyValueFactory<>("generatorName"));
        col2.setCellFactory(param -> {
            GeneratorTableCell cell = new GeneratorTableCell(list);
            cell.setEditable(true);
            cell.setOnCommitEdit(event -> pane.setCurrentRoute(event.getNewValue()));
            return cell;
        });

        getColumns().add(col1);
        getColumns().add(col2);
    }

    static class GeneratorTableCell extends TreeTableCell<Row, String> {

        /**
         * 所有列维护一个 ChoiceBox 实例
         */
        ChoiceBox<String> choiceBox;

        ObservableList<String> items = FXCollections.observableArrayList();

        StringConverter<String> converter = new StringConverter<>() {
            @Override
            public String toString(String object) {
                return object;
            }

            @Override
            public String fromString(String string) {
                return string;
            }
        };

        public GeneratorTableCell(List<String> list) {
            this.items.addAll(list);
        }

        EventHandler<? super TreeTableColumnEditEvent<Row, String>> onComitEventHandler;

        public void setOnCommitEdit(EventHandler<? super TreeTableColumnEditEvent<Row, String>> value) {
            this.onComitEventHandler = value;
        }

        @Override
        public void startEdit() {
            if (!isEditable() || !getTreeTableView().isEditable() || !getTableColumn().isEditable()) {
                return;
            }

            if (choiceBox == null) {
                choiceBox = CellUtils.createChoiceBox(this, items, new SimpleObjectProperty<>(converter));
            }

            choiceBox.getSelectionModel().select(getItem());
            super.startEdit();
            setText(null);
            setGraphic(choiceBox);
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(converter.toString(getItem()));
            setGraphic(null);
        }

        /**
         * @see ChoiceBoxTreeTableCell#updateItem(Object, boolean)
         */
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            CellUtils.updateItem(this, converter, null, null, choiceBox);
        }

        @Override
        public void commitEdit(String newValue) {
            if (Objects.equals(getItem(), newValue)) {
                return;
            }

            /**
             * 绑定数据失效，此处手动更新
             */
            TreeItem<Row> treeItem = getTableRow().getTreeItem();
            if (treeItem != null) {
                Row value = treeItem.getValue();
                if (value != null) {
                    value.setGeneratorName(newValue);
                }
            }
            super.commitEdit(newValue);

            if (onComitEventHandler != null) {
                onComitEventHandler.handle(new TreeTableColumnEditEvent<>(getTreeTableView(), getItem(), newValue));
            }
        }
    }

    public void addColumns(String databaseName, String... columns) {
        TreeItem<Row> dbItem = new TreeItem<>(new Row(databaseName, null));
        getRoot().getChildren().add(dbItem);
        for (String column : columns) {
            dbItem.getChildren().add(new TreeItem<>(new Row(column, null)));
        }
    }

    public void expandAll() {
        for (TreeItem<Row> dbItem : getRoot().getChildren()) {
            dbItem.setExpanded(true);
        }
    }
}
