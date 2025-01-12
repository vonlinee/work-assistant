package io.devpl.fxui.controls;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * 弹窗
 *
 * @see javafx.stage.PopupWindow
 */
public class Modal extends Stage {

    public Modal() {
        initModality(Modality.WINDOW_MODAL);
    }

    /**
     * 展示弹窗
     *
     * @param event   触发事件
     * @param root    弹窗内容的根节点
     * @param onClose 弹窗关闭的回调
     */
    public static void show(Event event, String title, Parent root, EventHandler<WindowEvent> onClose) {
        if (root.getParent() != null) {
            throw new RuntimeException("root node has been add scene graph");
        }
        Scene scene = root.getScene();
        if (scene == null) {
            scene = new Scene(root);
        }
        Modal modal = new Modal();
        modal.setTitle(title);
        modal.setScene(scene);
        if (onClose != null) {
            modal.setOnCloseRequest(onClose);
        }
        modal.show();
    }
}
