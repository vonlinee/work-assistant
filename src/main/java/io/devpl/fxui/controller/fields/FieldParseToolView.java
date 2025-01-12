package io.devpl.fxui.controller.fields;

import io.devpl.fxui.components.Modal;
import io.devpl.fxui.model.FieldNode;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.List;

/**
 * 字段解析
 */
public class FieldParseToolView extends BorderPane {

    FieldTreeTable treeTable;
    TabPane tabPane;

    FieldRenderView fieldRenderView = new FieldRenderView();

    public FieldParseToolView() {
        SplitPane root = new SplitPane();
        tabPane = new TabPane();
        treeTable = new FieldTreeTable();

        addTab(new SqlParseView());
        addTab(new MyBatisParseView());

        root.getItems().addAll(tabPane, treeTable);

        Button btnGetSample = new Button("获取示例");
        Button btnParse = new Button("解析");
        Button btnGenFile = new Button("生成");

        btnGenFile.setOnAction(event -> {
            List<FieldNode> fields = treeTable.getFields();
            if (fields.isEmpty()) {
                return;
            }
            fieldRenderView.setFields(fields);
            Modal.show("字段渲染", fieldRenderView, 600, 500);
        });

        ChoiceBox<String> appendMode = new ChoiceBox<>(FXCollections.observableArrayList("智能合并", "全部覆盖", "覆盖同名字段", "追加"));
        appendMode.setValue("智能合并");

        HBox bottom = new HBox();
        bottom.getChildren().add(btnParse);

        btnGetSample.setOnAction(event -> {
            FieldParseView content = (FieldParseView) tabPane.getSelectionModel().getSelectedItem().getContent();
            content.fillSampleText();
        });

        btnParse.setOnAction(event -> {
            FieldParseView parseView = (FieldParseView) tabPane.getSelectionModel().getSelectedItem().getContent();
            List<FieldNode> fieldNodes = parseView.parse(parseView.getParseableText());
            treeTable.addFields(fieldNodes);
        });

        bottom.getChildren().add(btnGetSample);
        bottom.getChildren().add(appendMode);
        bottom.getChildren().add(btnGenFile);
        setCenter(root);
        setBottom(bottom);
    }

    void addTab(FieldParseView view) {
        Tab tab = new Tab(view.getName(), view);
        tab.setClosable(false);
        tabPane.getTabs().add(tab);
    }

    public final List<FieldNode> getFields() {
        return treeTable.getFields();
    }
}
