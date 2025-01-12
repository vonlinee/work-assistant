package io.devpl.fxui.controller.dbconn;

import io.devpl.fxui.event.DeleteConnEvent;
import io.devpl.fxui.fxtras.Alerts;
import io.devpl.fxui.fxtras.mvvm.FxmlBinder;
import io.devpl.fxui.fxtras.mvvm.FxmlView;
import io.devpl.fxui.fxtras.utils.StageManager;
import io.devpl.fxui.model.ConnectionConfig;
import io.devpl.fxui.model.ConnectionRegistry;
import io.devpl.fxui.utils.AppConfig;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.TextAlignment;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * 数据库连接信息管理控制器
 */
@FxmlBinder(location = "layout/connection_manage.fxml", label = "数据库连接管理")
public class ConnManageView extends FxmlView {

    @FXML
    public TableColumn<ConnectionConfig, String> tblcDbType;
    @FXML
    public TableColumn<ConnectionConfig, String> tblcHostname;
    @FXML
    public TableColumn<ConnectionConfig, String> tblcPort;
    @FXML
    public TableColumn<ConnectionConfig, String> tblcDatabaseName;
    @FXML
    public TableView<ConnectionConfig> tblvConnectionList;
    @FXML
    public TableColumn<ConnectionConfig, String> tblcConnectionName;

    private final ConnectionConfigService connectionConfigService = new ConnectionConfigServiceImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tblvConnectionList.setRowFactory(param -> {
            final TableRow<ConnectionConfig> row = new TableRow<>();
            row.setAlignment(Pos.CENTER);
            row.setTextAlignment(TextAlignment.CENTER);
            return row;
        });
        tblvConnectionList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblcDbType.setCellValueFactory(new PropertyValueFactory<>("dbType"));
        tblcHostname.setCellValueFactory(new PropertyValueFactory<>("host"));
        tblcPort.setCellValueFactory(new PropertyValueFactory<>("port"));
        tblcDatabaseName.setCellValueFactory(new PropertyValueFactory<>("dbName"));
        tblcConnectionName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getConnectionName()));
        fillConnectionInfo();
    }

    @FXML
    public void btnNewConnection(ActionEvent actionEvent) {
        StageManager.show(NewConnView.class);
    }

    public void addNewConnectionInfo(ConnectionConfig connectionInfo) {
        if (connectionConfigService.save(connectionInfo)) {
            log.info("保存成功 {}", connectionInfo);
            Platform.runLater(() -> tblvConnectionList.getItems().add(connectionInfo));
        }
    }

    public void fillConnectionInfo() {
        tblvConnectionList.setItems(ConnectionRegistry.getConnectionConfigurations());
    }

    @FXML
    public void deleteConnection(ActionEvent actionEvent) {
        ObservableList<ConnectionConfig> selectedItems = tblvConnectionList.getSelectionModel().getSelectedItems();
        List<ConnectionConfig> list = new ArrayList<>(selectedItems);
        try {
            if (AppConfig.deleteConnectionById(list) == list.size()) {
                tblvConnectionList.getItems().removeAll(list);
            }
        } catch (Exception exception) {
            Alerts.exception("删除连接失败", exception).show();
            return;
        }
        DeleteConnEvent event = new DeleteConnEvent();
        event.setConnectionNames(list.stream().map(ConnectionConfig::getConnectionName).collect(Collectors.toList()));
        publish(event);
    }
}
