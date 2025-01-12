package io.devpl.fxui.components.table;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Collection;

public abstract class TableVIewBase<R> extends TableView<R> {

    @SafeVarargs
    public final void addColumns(TableColumn<R, ?>... columns) {
        super.getColumns().addAll(columns);
    }

    public final void addColumns(Collection<TableColumn<R, ?>> columns) {
        super.getColumns().addAll(columns);
    }

    public final void setItems(Collection<R> items) {
        getItems().clear();
        getItems().addAll(items);
    }

    public final void clearItems() {
        super.getItems().clear();
    }

    public final void addItem(R item) {
        super.getItems().add(item);
    }
}
