package org.example.workassistant.fxui.controller.dbconn;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.example.workassistant.sdk.util.StringUtils;
import org.example.workassistant.fxui.model.DatabaseInfo;
import io.fxtras.Alerts;
import io.fxtras.scene.mvvm.FxmlView;
import io.fxtras.scene.mvvm.FxmlBinder;
import org.example.workassistant.fxui.utils.JSchUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.*;

@FxmlBinder(location = "layout/sshBasedConnection.fxml")
public class OverSshView extends FxmlView {

    @FXML
    public HBox pubkeyBox;
    @FXML
    public Label lPortLabel;
    @FXML
    public TextField sshUserField;
    @FXML
    public ChoiceBox<String> authTypeChoice;
    @FXML
    public Label sshPasswordLabel;
    @FXML
    public PasswordField sshPasswordField;
    @FXML
    private TextField lportField;
    @FXML
    private TextField rportField;
    @FXML
    private Label pubkeyBoxLabel;
    @FXML
    private TextField sshPubKeyField;
    @FXML
    public PasswordField sshPubkeyPasswordField;
    @FXML
    public Label sshPubkeyPasswordLabel;
    @FXML
    public Label sshPubkeyPasswordNote;

    private final FileChooser fileChooser = new FileChooser();

    private File privateKey;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileChooser.setTitle("选择SSH秘钥文件");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        authTypeChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if ("PubKey".equals(newValue)) {
                // 公钥认证
                sshPasswordField.setVisible(false);
                sshPasswordLabel.setVisible(false);
                pubkeyBox.setVisible(true);
                pubkeyBoxLabel.setVisible(true);
                sshPubkeyPasswordField.setVisible(true);
                sshPubkeyPasswordLabel.setVisible(true);
                sshPubkeyPasswordNote.setVisible(true);
            } else {
                // 密码认证
                pubkeyBox.setVisible(false);
                pubkeyBoxLabel.setVisible(false);
                sshPubkeyPasswordField.setVisible(false);
                sshPubkeyPasswordLabel.setVisible(false);
                sshPubkeyPasswordNote.setVisible(false);
                sshPasswordLabel.setVisible(true);
                sshPasswordField.setVisible(true);
            }
        });
    }

    public void setDbConnectionConfig(DatabaseInfo databaseConfig) {
        if (databaseConfig == null) {
            return;
        }
        // this.sshdPortField.setText(databaseConfig.getSshPort());
        // this.sshHostField.setText(databaseConfig.getSshHost());
        this.lportField.setText(databaseConfig.getLport());
        this.rportField.setText(databaseConfig.getRport());
        this.sshUserField.setText(databaseConfig.getSshUser());
        this.sshPasswordField.setText(databaseConfig.getSshPassword());
        // 例如：默认从本机的 3306 -> 转发到 3306
        if (!StringUtils.hasText(this.lportField.getText())) {
            this.lportField.setText(databaseConfig.getPort());
        }
        if (!StringUtils.hasText(this.rportField.getText())) {
            this.rportField.setText(databaseConfig.getPort());
        }
        if (!StringUtils.hasText(databaseConfig.getPrivateKey())) {
            this.sshPubKeyField.setText(databaseConfig.getPrivateKey());
            this.sshPubkeyPasswordField.setText(databaseConfig.getPrivateKeyPassword());
            authTypeChoice.getSelectionModel().select("PubKey");
        }
        checkInput();
    }

    @FXML
    public void checkInput() {
//        DatabaseInfo databaseConfig = extractConfigFromUi();
//        if (authTypeChoice.getValue()
//                .equals("Password") && (!StringUtils.hasText(databaseConfig.getSshHost()) || !StringUtils.hasText(databaseConfig.getSshPort()) || Utils.isBlank(databaseConfig.getSshUser()) || !StringUtils.hasText(databaseConfig.getSshPassword())) || authTypeChoice
//                .getValue()
//                .equals("PubKey") && (!StringUtils.hasText(databaseConfig.getSshHost()) || !StringUtils.hasText(databaseConfig.getSshPort()) || Utils.isBlank(databaseConfig.getSshUser()) || !StringUtils.hasText(databaseConfig.getPrivateKey()))) {
//            // note.setText("当前SSH配置输入不完整，OVER SSH不生效");
//            // note.setTextFill(Paint.valueOf("#ff666f"));
//        } else {
//            // note.setText("当前SSH配置生效");
//            // note.setTextFill(Paint.valueOf("#5da355"));
//        }
    }

    public void setLPortLabelText(String text) {
        lPortLabel.setText(text);
    }

    public void recoverNotice() {
        this.lPortLabel.setText("注意不要填写被其他程序占用的端口");
    }

    public DatabaseInfo extractConfigFromUi() {
        String authType = authTypeChoice.getValue();
        DatabaseInfo config = new DatabaseInfo();
        // config.setSshHost(this.sshHostField.getText());
        // config.setSshPort(this.sshdPortField.getText());
        config.setLport(this.lportField.getText());
        config.setRport(this.rportField.getText());
        config.setSshUser(this.sshUserField.getText());
        config.setSshPassword(this.sshPasswordField.getText());
        if ("PubKey".equals(authType)) {
            if (this.privateKey != null) {
                config.setPrivateKey(this.privateKey.getAbsolutePath());
                config.setPrivateKeyPassword(this.sshPubkeyPasswordField.getText());
            }
        }
        return config;
    }

    public void saveConfig(ActionEvent event) {
        DatabaseInfo databaseConfig = extractConfigFromUi();
        if (StringUtils.hasText(databaseConfig.getName(), databaseConfig.getHost(), databaseConfig.getPort(), databaseConfig.getUsername(), databaseConfig.getEncoding(), databaseConfig.getDbType(), databaseConfig.getSchema())) {
            Alerts.warn("密码以外其他字段必填").showAndWait();
            return;
        }
        try {
            getStage(event).close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Alerts.error(e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void testSSH() {
        Session session = JSchUtils.getSSHSession(extractConfigFromUi());
        if (session == null) {
            Alerts.error("请检查主机，端口，用户名，以及密码/秘钥是否填写正确").show();
            return;
        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<?> result = executorService.submit(() -> {
            try {
                session.connect();
            } catch (JSchException e) {
                log.error("Connect Over SSH failed", e);
                throw new RuntimeException(e.getMessage());
            }
        });
        executorService.shutdown();
        try {
            boolean b = executorService.awaitTermination(5, TimeUnit.SECONDS);
            if (!b) {
                throw new TimeoutException("连接超时");
            }
            result.get();
            Alerts.info("连接SSH服务器成功，恭喜你可以使用OverSSH功能").show();
            recoverNotice();
        } catch (Exception e) {
            Alerts.error("请检查主机，端口，用户名，以及密码/秘钥是否填写正确: " + e.getMessage()).showAndWait();
        } finally {
            JSchUtils.shutdownPortForwarding(session);
        }
    }

    @FXML
    public void reset(ActionEvent actionEvent) {
        this.sshUserField.clear();
        this.sshPasswordField.clear();
        // this.sshdPortField.clear();
        // this.sshHostField.clear();
        this.lportField.clear();
        this.rportField.clear();
        this.sshPubKeyField.clear();
        recoverNotice();
    }

    /**
     * 选择公钥文件
     * @param actionEvent 点击事件
     */
    @FXML
    public void choosePubKey(ActionEvent actionEvent) {
        this.privateKey = fileChooser.showOpenDialog(getStage(actionEvent));
        if (this.privateKey == null) {
            return;
        }
        sshPubKeyField.setText(this.privateKey.getAbsolutePath());
    }
}
