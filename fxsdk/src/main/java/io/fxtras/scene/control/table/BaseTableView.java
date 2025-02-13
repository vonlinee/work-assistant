package io.fxtras.scene.control.table;

import javafx.scene.control.TableView;

import java.util.Collection;

public abstract class BaseTableView<S> extends TableView<S> {

    public void addItems(Collection<S> items) {
        getItems().addAll(items);
    }

    public void setItems(Collection<S> items) {
        clear();
        addItems(items);
    }

    public void clear() {
        getItems().clear();
    }
}
