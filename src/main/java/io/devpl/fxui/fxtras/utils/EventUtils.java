package io.devpl.fxui.fxtras.utils;

import javafx.event.Event;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public final class EventUtils {

    private EventUtils() {
    }

    /**
     * 是否鼠标左键双击
     * @param event 鼠标事 件
     * @return 是否鼠标左键双击
     */
    public static boolean isPrimaryButtonDoubleClicked(MouseEvent event) {
        return event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY;
    }

    /**
     * 获取事件源
     * @param event      事件
     * @param sourceType 事件源对象类型
     * @param <T>        事件源对象类型
     * @return 事件源对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T getEventSource(Event event, Class<T> sourceType) {
        return (T) event.getSource();
    }
}
