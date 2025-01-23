package org.example.workassistant.fxui.controller.fields;

import cn.hutool.core.io.FileUtil;
import org.example.workassistant.fxui.model.FieldNode;
import io.fxtras.FXUtils;
import org.example.workassistant.sdk.util.StringUtils;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Collection;

/**
 * 字段模板渲染
 */
public class FieldRenderView extends SplitPane {

    TextArea inputTextArea;
    TextArea outputTextArea;
    FieldTreeTable fieldTreeTable;

    FileChooser fileChooser;

    TemplateEngine templateEngine = new VelocityTemplateEngine();

    public FieldRenderView() {
        this.inputTextArea = new TextArea();
        inputTextArea.setFont(new Font(15));

        this.outputTextArea = new TextArea();
        outputTextArea.setFont(new Font(15));

        this.fieldTreeTable = new FieldTreeTable();
        VBox vBox = new VBox();

        ButtonBar buttonBar = new ButtonBar();

        Button loadBtn = new Button("加载模板");
        Button renderBtn = new Button("渲染");

        renderBtn.setOnAction(event -> {
            String tempate = inputTextArea.getText();
            if (StringUtils.hasText(tempate)) {
                TemplateArgumentsMap map = new TemplateArgumentsMap();

                map.add("columns", fieldTreeTable.getFields());
                outputTextArea.setText(templateEngine.evaluate(tempate, map));
            }
        });

        loadBtn.setOnAction(event -> {
            if (fileChooser == null) {
                fileChooser = new FileChooser();
                fileChooser.setTitle("选择模板");
                fileChooser.setInitialDirectory(new File("D:/Temp"));
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Velocity Template Files", "*.vm"), new FileChooser.ExtensionFilter("所有文件", "*"));
            }
            Stage stage = FXUtils.getStage(event);
            fileChooser.setInitialDirectory(new File("").getAbsoluteFile());
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                this.inputTextArea.setText(FileUtil.readUtf8String(selectedFile));
            }
        });

        buttonBar.getButtons().addAll(renderBtn, loadBtn);

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(inputTextArea, outputTextArea);
        splitPane.prefHeightProperty().bind(vBox.heightProperty().subtract(buttonBar.heightProperty()));

        vBox.getChildren().addAll(buttonBar, splitPane);

        getItems().addAll(vBox, fieldTreeTable);
    }

    public void setFields(Collection<FieldNode> fields) {
        fieldTreeTable.clearAll();
        fieldTreeTable.addFields(fields);
    }
}
