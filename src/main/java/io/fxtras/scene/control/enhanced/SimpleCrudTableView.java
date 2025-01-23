package io.fxtras.scene.control.enhanced;

import io.fxtras.scene.control.table.SimpleBeanTableView;
import javafx.scene.control.TableColumn;

import java.util.List;

public abstract class SimpleCrudTableView<S> extends SimpleBeanTableView<S> {

    OperationColumn<S> operationColumn = new OperationColumn<>();

    public SimpleCrudTableView() {
        getColumns().addAll(createColumns());
        getColumns().add(operationColumn);
    }

    public void addOperationAction(Action<S> action) {
        operationColumn.addButton(action);
    }

    protected abstract <C extends TableColumn<? extends S, ?>> List<C> createColumns();
}
