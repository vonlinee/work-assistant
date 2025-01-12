package io.devpl.fxui.app;

import io.devpl.fxui.controls.TextFieldTableCell;
import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TestComponentApplication extends Application {
    protected double initialWidth = 600.0;
    protected double initialHeight = 600.0;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(createRoot(), initialWidth, initialHeight);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(TestApp.class, args);
    }

    TableView<Data> tableView1 = new TableView<>();

    ObservableList<Data> items1 = FXCollections.observableArrayList();

    public Parent createRoot() {

        tableView1.setEditable(true);

        TableColumn<Data, Number> idColumn1 = new TableColumn<>("ID");
        TableColumn<Data, String> nameColumn1 = new TableColumn<>("姓名");

        nameColumn1.setOnEditCommit(event -> {
            System.out.println("列提交监听  " + event.getOldValue() + " " + event.getNewValue());
        });

        idColumn1.setCellValueFactory(param -> new SimpleIntegerProperty(param.getValue().getId()));
        nameColumn1.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        nameColumn1.setEditable(true);
        nameColumn1.setCellFactory(param -> {
            TextFieldTableCell<Data, String> cell = new TextFieldTableCell<>();

            return cell;
        });

        tableView1.getColumns().addAll(idColumn1, nameColumn1);

        tableView1.setEditable(true);

        tableView1.setItems(items1);

        items1.add(new Data(0, "A"));
        items1.add(new Data(1, "B"));
        items1.add(new Data(2, "C"));

        Button btn1 = new Button("Button1");
        Button btn2 = new Button("Refresh");

        btn2.setOnAction(event -> {
            tableView1.refresh();
        });

        HBox hBox = new HBox(btn1, btn2);

        Label label1 = new Label("Data");
        Label label2 = new Label("PropertyData");

        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setPrefHeight(200.0);


        VBox root = new VBox(label1, tableView1, label2, hBox, textArea);
        root.setSpacing(5);
        root.setAlignment(Pos.CENTER);
        return root;
    }
}

class Data {

    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Data [id=" + id + ", name=" + name + "]";
    }

    public Data(int id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

}
