package org.example.workassistant.fxui.controller.dbconn;

import org.example.workassistant.fxui.common.Constants;
import org.example.workassistant.fxui.controller.BuiltinDriverType;
import org.example.workassistant.fxui.event.FillDefaultValueEvent;
import org.example.workassistant.fxui.model.ConnectionConfig;
import org.example.workassistant.fxui.fxtras.mvvm.FxmlBinder;
import org.example.workassistant.fxui.fxtras.mvvm.FxmlView;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * 数据库连接控制器
 * 负责从界面的配置连接数据库
 */
@FxmlBinder(location = "layout/newConnection.fxml", label = "数据库连接配置")
public class DbConnView extends FxmlView {

    @FXML
    public CheckBox savePwdCheckBox;
    @FXML
    protected TextField nameField; // 数据库名称
    @FXML
    protected TextField hostField; // 主机地址
    @FXML
    protected TextField portField; // 端口
    @FXML
    protected TextField userNameField; // 用户名
    @FXML
    protected TextField passwordField; // 密码
    @FXML
    protected ComboBox<String> schemaField; // 数据库schema，MySQL中就是数据库名
    @FXML
    protected ChoiceBox<String> encodingChoice; // 编码
    @FXML
    protected ChoiceBox<String> dbTypeChoice;  // 数据库类型选择

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbTypeChoice.setItems(FXCollections.observableArrayList(BuiltinDriverType.supportedDriverNames()));
        dbTypeChoice.setValue(BuiltinDriverType.MYSQL5.name());
        encodingChoice.setItems(FXCollections.observableArrayList(Constants.SUPPORTED_ENCODING));
        encodingChoice.setValue(Constants.DEFAULT_ENCODING);
        hostField.setText(Constants.LOCALHOST);
        userNameField.setText(Constants.MYSQL_ROOT_USERNAME);
        portField.setText(String.valueOf(Constants.DEFAULT_MYSQL_SERVER_PORT));
    }

    /**
     * 初始化连接配置数据绑定
     * @param configuration 事件
     * @see ConnectionConfig
     */
    public void initBinder(ConnectionConfig configuration) {
        configuration.setPort(portField.getText());
        configuration.setUsername(userNameField.getText());
        configuration.setPassword(passwordField.getText());
        configuration.setDbType(dbTypeChoice.getValue());
        configuration.setHost(hostField.getText());
        configuration.setDbName(schemaField.getValue());
        configuration.setEncoding(encodingChoice.getValue());
        nameField.textProperty()
                .addListener((observable, oldValue, newValue) -> configuration.setConnectionName(newValue));
        // 数据监听
        dbTypeChoice.valueProperty().addListener((observable, oldValue, newValue) -> configuration.setDbType(newValue));
        encodingChoice.valueProperty()
                .addListener((observable, oldValue, newValue) -> configuration.setEncoding(newValue));
        hostField.textProperty().addListener((observable, oldValue, newValue) -> configuration.setHost(newValue));
        userNameField.textProperty()
                .addListener((observable, oldValue, newValue) -> configuration.setUsername(newValue));
        passwordField.textProperty()
                .addListener((observable, oldValue, newValue) -> configuration.setPassword(newValue));
        portField.textProperty().addListener((observable, oldValue, newValue) -> configuration.setPort(newValue));
        schemaField.valueProperty().addListener((observable, oldValue, newValue) -> configuration.setSchema(newValue));
    }

    /**
     * 填充默认值
     * @param event 填充默认值
     */
    public void fillDefaultValue(FillDefaultValueEvent event) {
        userNameField.setText("root");
        portField.setText("3306");
        passwordField.setText("123456");
        dbTypeChoice.setValue(BuiltinDriverType.MYSQL5.name());
        hostField.setText("127.0.0.1");
    }
}
