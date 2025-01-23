package org.example.workassistant.fxui.controller.mbg;

import org.example.workassistant.fxui.bridge.ProjectConfiguration;
import io.fxtras.utils.HBoxBuilder;
import io.fxtras.utils.VBoxBuilder;
import io.fxtras.utils.EventUtils;
import org.example.workassistant.fxui.utils.AppConfig;
import io.fxtras.mvvm.FxmlBinder;
import io.fxtras.mvvm.FxmlView;
import org.example.workassistant.fxui.utils.JSONUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * 项目配置表
 */
@FxmlBinder(location = "layout/mbg/ProjectConfigurationView.fxml", label = "项目配置")
public class ProjectConfigurationView extends FxmlView {

    @FXML
    public TableView<ProjectConfiguration> tbvConfig;
    @FXML
    public TableColumn<ProjectConfiguration, String> tblcName;
    @FXML
    public TableColumn<ProjectConfiguration, String> tblcValue;
    @FXML
    public Button btnApply;
    @FXML
    public BorderPane root;
    @FXML
    public Button btnRefresh;

    Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tblcName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        tblcValue.setCellValueFactory(param -> new SimpleStringProperty("..."));

        dialog.getDialogPane().setContent(VBoxBuilder.builder().spacing(5.0)
                .children(HBoxBuilder.builder().children(new Label("名称"), new TextField()).build(), new TextArea())
                .build());
        tbvConfig.setRowFactory(param -> {
            TableRow<ProjectConfiguration> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (EventUtils.isPrimaryButtonDoubleClicked(event)) {
                    VBox content = (VBox) dialog.getDialogPane().getContent();
                    TextArea textArea = (TextArea) content.getChildren().get(1);
                    ProjectConfiguration rowItem = row.getItem();
                    String text = JSONUtils.toString(rowItem, true);
                    textArea.setText(text);
                    Optional<ButtonType> buttonType = dialog.showAndWait();
                    if (buttonType.isPresent()) {
                        ButtonType btnType = buttonType.get();
                        if (btnType == ButtonType.OK) {
                            text = textArea.getText();
                            ProjectConfiguration newItem = JSONUtils.parseObject(text, ProjectConfiguration.class);
                            AppConfig.updateProjectConfiguration(rowItem.getName(), newItem.getName(), JSONUtils.toString(newItem));
                        }
                    }
                }
            });
            return row;
        });
        Event.fireEvent(btnRefresh, new ActionEvent());
    }

    @FXML
    public void applyConfig(ActionEvent actionEvent) {
        ProjectConfiguration selectedItem = tbvConfig.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        publish("LoadConfig", selectedItem);
        tbvConfig.getSelectionModel().clearSelection();
        getStage(tbvConfig).close();
    }

    @FXML
    public void refreshConfig(ActionEvent actionEvent) {
        tbvConfig.getItems().clear();
        AppConfig.listProjectConfigurations().forEach(item -> tbvConfig.getItems().add(item));
    }
}
