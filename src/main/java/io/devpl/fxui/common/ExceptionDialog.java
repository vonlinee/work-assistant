package io.devpl.fxui.common;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Supplier;

/**
 * 单例异常弹窗，阻塞用户行为
 */
public final class ExceptionDialog extends Stage {

    private static final ExceptionDialog dialog = new ExceptionDialog();

    private final TextArea contentArea = new TextArea();

    private transient Throwable throwable;

    private ExceptionDialog() {
        initModality(Modality.APPLICATION_MODAL);
        VBox container = new VBox();

        container.getChildren().add(contentArea);
        ButtonBar buttonBar = new ButtonBar();

        Button btnOk = new Button("OK");
        btnOk.setOnAction(event -> this.close());

        TextArea txaStackTrace = new TextArea();

        Button btnDetail = new Button("Details");
        btnDetail.setOnAction(event -> {
            if (container.getChildren().size() == 3) {
                container.getChildren().remove(2);
            } else {
                txaStackTrace.clear();
                if (txaStackTrace.getText().isEmpty() && throwable != null) {
                    txaStackTrace.appendText(toString(throwable).toString());
                }
                container.getChildren().add(txaStackTrace);
            }
        });
        buttonBar.getButtons().addAll(btnOk, btnDetail);
        HBox hBox = new HBox(buttonBar);
        container.getChildren().add(hBox);

        Scene scene = new Scene(container, 400.0, 400.0);
        setScene(scene);
    }

    private StringWriter toString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter;
    }

    public void setText(Throwable throwable) {
        this.throwable = throwable;
        contentArea.appendText(throwable.getMessage());
    }

    /**
     * 展示异常弹窗
     *
     * @param throwable 异常信息
     */
    public static void show(Throwable throwable) {
        dialog.setText(throwable);
        if (!dialog.isShowing()) {
            dialog.centerOnScreen();
            dialog.show();
        }
    }

    public static void wrap(Supplier<?> supplier) {
        try {
            supplier.get();
        } catch (Throwable throwable) {
            show(throwable);
        }
    }
}
