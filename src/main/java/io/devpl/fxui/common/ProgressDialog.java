package io.devpl.fxui.common;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import org.mybatis.generator.api.ProgressCallback;

import java.time.LocalDateTime;

/**
 * 进度弹窗
 * TODO 改成追加模式
 */
public class ProgressDialog extends Alert implements ProgressCallback {

    private final TextArea textArea = new TextArea();

    public ProgressDialog() {
        super(AlertType.INFORMATION);
        setResizable(true);
        textArea.setPrefSize(800.0, 500.0);
        getDialogPane().setContent(textArea);
        showingProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                setContentText(""); // 清空文本
                textArea.clear();
            }
        });
    }

    @Override
    public void introspectionStarted(int totalTasks) {
        textArea.appendText(LocalDateTime.now().toString());
        textArea.appendText(" 开始获取表信息: totalTasks=" + totalTasks + "\n");
    }

    @Override
    public void generationStarted(int totalTasks) {
        textArea.appendText(LocalDateTime.now().toString());
        textArea.appendText(" 开始代码生成: totalTasks=" + totalTasks + "\n");
    }

    @Override
    public void saveStarted(int totalTasks) {
        textArea.appendText(LocalDateTime.now().toString());
        textArea.appendText(" 开始保存生成的文件: totalTasks=" + totalTasks + "\n");
    }

    @Override
    public void startTask(String taskName) {
        textArea.appendText(LocalDateTime.now().toString());
        textArea.appendText(" 开始代码生成任务: taskName=" + taskName + "\n");
    }

    @Override
    public void done() {
        textArea.appendText(LocalDateTime.now().toString());
        textArea.appendText(" 完成");
    }

    @Override
    public void checkCancel() {
        textArea.appendText(LocalDateTime.now().toString());
        textArea.appendText(" 取消\n");
    }
}
