package io.devpl.fxui.components.table;

import javafx.beans.NamedArg;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.control.TableColumn;

/**
 * 分页事件
 *
 * @see TableColumn#editCommitEvent()
 */
public class PageEvent extends Event {

    public static final EventType<PageEvent> ANY = new EventType<>(Event.ANY, "ANY");

    public static final EventType<PageEvent> CHANGE = new EventType<>(ANY, "CHANGE");

    private int pageNum;
    private int pageSize;

    public PageEvent(EventType<PageEvent> eventType) {
        super(eventType);
    }

    public PageEvent(final @NamedArg("source") Object source, final @NamedArg("target") EventTarget target, final @NamedArg("eventType") EventType<PageEvent> eventType) {
        super(source, target, eventType);
    }

    public int getPageNum() {
        return pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public static PageEvent pageChange(Integer pageNum, Integer pageSize) {
        PageEvent event = new PageEvent(PageEvent.CHANGE);
        event.pageNum = pageNum;
        event.pageSize = pageSize;
        return event;
    }
}
