package io.devpl.fxui.components;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.control.TreeTableView;

/**
 * @see javafx.scene.control.TreeView.EditEvent
 */
public class TreeTableColumnEditEvent<S, T> extends Event {

    private final TreeTableView<S> source;
    private T oldValue;
    private T newValue;

    public TreeTableColumnEditEvent(TreeTableView<S> treeTableView, T oldValue, T newValue) {
        super(ANY);
        this.source = treeTableView;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public T getOldValue() {
        return oldValue;
    }

    public void setOldValue(T oldValue) {
        this.oldValue = oldValue;
    }

    public T getNewValue() {
        return newValue;
    }

    public void setNewValue(T newValue) {
        this.newValue = newValue;
    }

    @Override
    public TreeTableView<S> getSource() {
        return source;
    }
}
