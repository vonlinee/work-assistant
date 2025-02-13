package io.fxtras.utils;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;

import java.util.function.Function;

public class LambdaValueFactory<S, T> implements TableColumnCellFactory<S, T> {

    private final Function<S, T> getter;

    public LambdaValueFactory(Function<S, T> getter) {
        this.getter = getter;
    }

    @Override
    public ObservableValue<T> getCellValue(S rowData) {
        if (getter == null || rowData == null) {
            return null;
        }
        return new ReadOnlyObjectWrapper<T>(this.getter.apply(rowData));
    }
}
