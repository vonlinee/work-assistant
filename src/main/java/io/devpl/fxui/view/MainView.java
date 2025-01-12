package io.devpl.fxui.view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;

public class MainView extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(createRoot(), 700.0, 700.0);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public Parent createRoot() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(5));
        fillRoot(root);
        return root;
    }

    public void fillRoot(BorderPane root) {
        CodeArea codeArea = new CodeArea();
        codeArea.setPrefWidth(300.0);
        root.setLeft(codeArea);

        CodeArea codeAreaRight = new CodeArea();
        codeAreaRight.setPrefWidth(300.0);
        root.setRight(codeAreaRight);

        VBox vBox = new VBox();

        Button btn = new Button("parse");

        btn.setOnAction(event -> {
            String text = codeArea.getText();
            if (text != null && !text.isBlank()) {
                codeAreaRight.clear();
            }
        });

        vBox.getChildren().add(btn);

        root.setCenter(vBox);
    }
}
