package io.fxtras.scene.control.table;

import javafx.scene.Node;
import javafx.scene.control.TableCell;

public abstract class CustomTableCell<S, T> extends TableCell<S, T> {

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            // 如果此列为空默认不添加元素
            setText(getEmptyText());
            setGraphic(getEmptyGraphic());
        } else {
            this.setGraphic(getShowingNode());
        }
    }

    public String getEmptyText() {
        return null;
    }

    public Node getEmptyGraphic() {
        return null;
    }

    public Node getShowingNode() {
        return null;
    }
}
