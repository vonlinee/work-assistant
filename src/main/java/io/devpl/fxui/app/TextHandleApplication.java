package io.devpl.fxui.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 文件合并工具
 */
public class TextHandleApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("文本处理");

        BorderPane root = new BorderPane();
        SplitPane center = new SplitPane();

        TextArea input = new TextArea();
        TextArea output = new TextArea();
        center.getItems().addAll(input, output);
        root.setCenter(center);

        FlowPane bottom = new FlowPane();

        Button btn1 = new Button("去除空行");
        btn1.setOnAction(event -> {
            String text = input.getText();
            String[] lines = text.split("\n");
            List<String> leftLines = Arrays.stream(lines).filter(l -> l != null && !l.isEmpty()).toList();
            output.setText(String.join("\n", leftLines));
        });

        Button btn2 = new Button("添加序号");
        btn2.setOnAction(event -> {
            String text = input.getText();
            String[] lines = text.split("\n");
            List<String> leftLines = new ArrayList<>(Arrays.stream(lines).filter(l -> l != null && !l.isEmpty()).toList());
            for (int i = 0; i < leftLines.size(); i++) {
                leftLines.set(i, (i + 1) + "." + leftLines.get(i));
            }
            output.setText(String.join("\n", leftLines));
        });

        bottom.getChildren().addAll(btn1, btn2);

        root.setBottom(bottom);
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }
}
