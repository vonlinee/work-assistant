package io.devpl.fxui.fxtras;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * FX弹窗警告
 */
public final class Alerts {

    private Alerts() {
    }

    private static final Alert INFO = new Alert(Alert.AlertType.INFORMATION);
    private static final Alert WARN = new Alert(Alert.AlertType.WARNING);
    private static final Alert ERROR = new Alert(Alert.AlertType.ERROR);
    private static final Alert NONE = new Alert(Alert.AlertType.NONE);
    private static final Alert CONFIRM = new Alert(Alert.AlertType.CONFIRMATION);
    private static final Alert EXCEPTION = new Alert(Alert.AlertType.ERROR);

    static {
        INFO.setResizable(true);
        WARN.setResizable(true);
        ERROR.setResizable(true);
        NONE.setResizable(true);
        CONFIRM.setResizable(true);

        TextArea textArea = new TextArea();
        EXCEPTION.getDialogPane().setContent(textArea);
        EXCEPTION.setResizable(true);
        EXCEPTION.contentTextProperty().bindBidirectional(textArea.textProperty());
    }

    public static Alert info(String message) {
        INFO.setContentText(message);
        return INFO;
    }

    public static Alert info(String title, Object data) {
        INFO.setTitle(title);
        INFO.setContentText(String.valueOf(data));
        return INFO;
    }

    public static Alert warn(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(message);
        return alert;
    }

    public static Alert error(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setWidth(400);
        alert.setHeight(400);
        alert.setResizable(true);
        alert.setContentText(message);
        return alert;
    }

    public static Alert error(String message, Object... args) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setWidth(400);
        alert.setHeight(400);
        alert.setResizable(true);
        alert.setContentText(message.formatted(args));
        return alert;
    }

    public static Alert exception(String header, Throwable throwable) {
        final StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw, true)) {
            throwable.printStackTrace(pw);
        }
        EXCEPTION.setHeaderText(header);
        EXCEPTION.setContentText(sw.toString());
        return EXCEPTION;
    }

    public static Alert confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText(message);
        return alert;
    }
}
