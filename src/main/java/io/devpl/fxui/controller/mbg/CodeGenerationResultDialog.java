package io.devpl.fxui.controller.mbg;

import io.devpl.fxui.utils.Helper;
import io.devpl.fxui.view.FileGenerationResultView;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.io.File;
import java.util.List;

/**
 * 代码生成结果面板
 */
public class CodeGenerationResultDialog extends Dialog<Void> {

    TableView<File> fileTableView = new TableView<>();

    FileGenerationResultView view = new FileGenerationResultView(null);

    public CodeGenerationResultDialog() {
        DialogPane dialogPane = new DialogPane();
        setResizable(true);
        setTitle("生成结果");
        TableColumn<File, String> column = new TableColumn<>("文件");
        column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getAbsolutePath()));
        fileTableView.getColumns().add(column);
        fileTableView.setPrefSize(800.0, 400.0);
        fileTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<File, File> columnOperation = new TableColumn<>("操作");
        columnOperation.setMaxWidth(200.0);
        columnOperation.setMinWidth(200.0);
        columnOperation.setCellFactory(new Callback<>() {
            @Override
            public TableCell<File, File> call(TableColumn<File, File> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(File item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            setText(null);
                            HBox hBox = new HBox();
                            hBox.setAlignment(Pos.CENTER);
                            Button btnShow = new Button("Show");
                            hBox.getChildren().addAll(btnShow);
                            btnShow.setOnAction(event -> {
                                TableView<File> tableView = param.getTableView();
                                int index = this.getIndex();
                                File file = tableView.getItems().get(index);
                                Helper.show(file.getParentFile());
                            });
                            Button btnOpen = new Button("Open");
                            btnOpen.setOnAction(event -> {
                                TableView<File> tableView = param.getTableView();
                                int index = this.getIndex();
                                File file = tableView.getItems().get(index);
                                Helper.edit(file);
                            });
                            hBox.getChildren().addAll(btnOpen);
                            setGraphic(hBox);
                        } else {
                            // 清除节点，否则还会出现
                            setGraphic(null);
                            setText(null);
                        }
                    }
                };
            }
        });
        fileTableView.getColumns().add(columnOperation);

//        ScrollPane scrollPane = new ScrollPane(fileTableView);

        dialogPane.setContent(view);
        this.setDialogPane(dialogPane);
        dialogPane.getButtonTypes().add(new ButtonType("确认", ButtonBar.ButtonData.OK_DONE));
        setOnCloseRequest(event -> fileTableView.getItems().clear());
    }

    public void addGeneratedFiles(List<File> files) {
        fileTableView.getItems().addAll(files);
    }

    public void showDirectory(File dir) {
        this.view.setRoot(dir);
        this.show();
    }
}
