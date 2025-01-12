package io.devpl.fxui.components.table;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;

/**
 * 操作列单元格
 *
 * @param <S>
 */
public abstract class OperationColumnTableCell<S, T> extends TableCell<S, T> {

    private final HBox box;

    public OperationColumnTableCell(TableColumn<S, ?> column) {
        box = new HBox();

        init(box);
        getChildren().add(box);
    }

    protected abstract void init(HBox container);

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setGraphic(box);
        }
    }
}
