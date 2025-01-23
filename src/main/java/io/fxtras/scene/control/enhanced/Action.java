package io.fxtras.scene.control.enhanced;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public interface Action<S> {

    String getLabel();

    void onAction(TableView<S> table, TableColumn<S, Object> column, S row);
}