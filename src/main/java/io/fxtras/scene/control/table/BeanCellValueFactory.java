package io.fxtras.scene.control.table;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public abstract class BeanCellValueFactory<S, T> implements Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<T>> {
    @Override
    @SuppressWarnings("unchecked")
    public ObservableValue<T> call(TableColumn.CellDataFeatures<S, T> stCellDataFeatures) {
        S row = stCellDataFeatures.getValue();
        if (row == null) {
            return new SimpleObjectProperty<>();
        }
        T cellValue = getCellValue(row);
        if (cellValue instanceof String) {
            return (ObservableValue<T>) new SimpleStringProperty((String) cellValue);
        } else if (cellValue instanceof Integer) {
            return (ObservableValue<T>) new SimpleIntegerProperty((Integer) cellValue);
        }
        return new SimpleObjectProperty<>(cellValue);
    }

    protected abstract T getCellValue(S row);
}
