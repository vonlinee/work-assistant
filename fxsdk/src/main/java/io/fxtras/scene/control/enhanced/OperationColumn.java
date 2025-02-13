package io.fxtras.scene.control.enhanced;

import io.fxtras.scene.control.table.CustomTableCell;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 操作列
 *
 * @param <S>
 */
public class OperationColumn<S> extends TableColumn<S, Object> {

    private final List<Action<S>> actions = new ArrayList<>();

    public OperationColumn() {
        super("操作");
        setCellFactory(param -> new OperationTableCell(actions));
    }

    private ActionButton<S> newActionButton(Action<S> action, TableCell<S, Object> cell) {
        ActionButton<S> button = new ActionButton<>(action.getLabel(), cell);
        button.setOnAction(event -> {
            @SuppressWarnings("unchecked")
            ActionButton<S> source = (ActionButton<S>) event.getSource();
            int index = source.cell.getIndex();
            TableView<S> tableView = this.getTableView();
            S row = tableView.getItems().get(index);
            action.onAction(tableView, this, row);
        });
        return button;
    }

    public void addButton(Action<S> action) {
        actions.add(action);
    }

    private class OperationTableCell extends CustomTableCell<S, Object> {
        OperationColumnCellNode buttonBar;

        public OperationTableCell(List<Action<S>> actions) {
            List<ActionButton<S>> buttons = new ArrayList<>();
            for (Action<S> action : actions) {
                buttons.add(newActionButton(action, this));
            }
            buttonBar = new OperationColumnCellNode(this);
            buttonBar.getButtons().addAll(buttons);
        }

        @Override
        public Node getShowingNode() {
            return buttonBar;
        }
    }


    private class OperationColumnCellNode extends ButtonBar {

        TableCell<S, Object> cell;

        public OperationColumnCellNode(TableCell<S, Object> cell) {
            this.cell = cell;
        }
    }

    private class ActionButton<S> extends Button {
        TableCell<S, Object> cell;

        public ActionButton(String text, TableCell<S, Object> cell) {
            super(text);
            this.cell = cell;
        }
    }
}
