package io.fxtras.scene.control.table;

import javafx.scene.control.TableColumn;

public class SimpleBeanTableColumn<S, T> extends TableColumn<S, T> {

    public SimpleBeanTableColumn(String text) {
        super(text);
    }

    public void setCellValueFactory(BeanCellValueFactory<S, T> factory) {
        super.setCellValueFactory(factory);
    }
}
