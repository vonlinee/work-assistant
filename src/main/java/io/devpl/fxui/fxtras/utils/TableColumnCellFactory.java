package io.devpl.fxui.fxtras.utils;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public interface TableColumnCellFactory<S, T> extends Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<T>> {
    @Override
    default ObservableValue<T> call(TableColumn.CellDataFeatures<S, T> param) {
        return getCellValue(param.getValue());
    }

    ObservableValue<T> getCellValue(S rowData);
}
