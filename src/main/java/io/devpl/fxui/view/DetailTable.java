package io.devpl.fxui.view;

import io.devpl.fxui.utils.FXUtils;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class DetailTable extends BorderPane {

    private final TableView<Option> tableView;
    private final ChoiceBox<String> choiceBox;

    public DetailTable() {

        tableView = FXUtils.createTableView(Option.class);

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);
        top.setSpacing(5);

        choiceBox = new ChoiceBox<>();

        choiceBox.setPrefWidth(150.0);

        Label label = new Label("名称");
        Button resetBtn = new Button("重置");

        top.getChildren().addAll(choiceBox, label, resetBtn);

        setTop(top);
        setCenter(tableView);
    }

    public void setGeneratorName(GeneratorItem item) {
        choiceBox.setValue(item.getName());
        this.tableView.getItems().addAll(item.getOptions());
    }
}
