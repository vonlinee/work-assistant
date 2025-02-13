package org.example.workassistant.ui.controller.fields;

import org.example.workassistant.ui.model.CommonJavaType;
import org.example.workassistant.utils.Helper;
import org.example.workassistant.utils.util.StringUtils;
import org.example.workassistant.ui.editor.CodeMirrorEditor;
import org.example.workassistant.ui.editor.LanguageMode;
import org.example.workassistant.ui.model.ConnectionRegistry;
import org.example.workassistant.ui.model.FieldInfo;
import org.example.workassistant.utils.DBUtils;
import io.fxtras.Alerts;
import io.fxtras.scene.mvvm.FxmlBinder;
import io.fxtras.scene.mvvm.FxmlView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * SQL字段导入
 */
@FxmlBinder(location = "layout/fields/FieldsImportSQLView.fxml")
public class SQLImportView extends FxmlView {

    @FXML
    public ComboBox<String> chbJsonSpec;
    @FXML
    public ComboBox<String> cboxDbName;
    @FXML
    public BorderPane bopRoot;

    CodeMirrorEditor codeEditor;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (codeEditor == null) {
            codeEditor = CodeMirrorEditor.newInstance(LanguageMode.SQL);
            bopRoot.setCenter(codeEditor.getView());
        }
        chbJsonSpec.getItems().addAll(ConnectionRegistry.getRegisteredConnectionConfigMap().keySet());
    }

    @FXML
    public void parseColumns(ActionEvent actionEvent) {

        String text = codeEditor.getText();
        if (!StringUtils.hasText(text)) {
            Alerts.warn("待解析SQL为空!").showAndWait();
            return;
        }
        try {
            Map<String, Set<String>> map = new HashMap<>();
            String dbName = cboxDbName.getValue();
            if (!StringUtils.hasText(dbName)) {
                Alerts.warn("数据库名称为空!").show();
                return;
            }
            List<InfoSchemaColumn> metadata = new ArrayList<>();
            map.forEach((tableName, columnNames) -> {
                List<String> names = new ArrayList<>();
                for (String columnName : columnNames) {
                    names.add("'" + columnName + "'");
                }
                String sql = getQueryColumnMetaSql(dbName, tableName, names);
                metadata.addAll(query(sql));
            });
            List<FieldInfo> fieldInfos = new ArrayList<>();
            for (InfoSchemaColumn metadatum : metadata) {
                FieldInfo fieldInfo = new FieldInfo();
                fieldInfo.setModifier("private");
                fieldInfo.setDataType(CommonJavaType.STRING);
                fieldInfo.setName(Helper.underlineToCamel(metadatum.getColumnName()));
                fieldInfo.setRemarks(metadatum.getColumnComment());
                fieldInfos.add(fieldInfo);
            }
            publish("addFieldInfoList", fieldInfos);
            getStage(actionEvent).close();
        } catch (Exception exception) {
            Alerts.exception("解析失败", exception).showAndWait();
        }
    }

    public String getQueryColumnMetaSql(String databaseName, String tableName, Collection<String> columns) {
        String columnCondition = String.join(",", columns);
        return String.format("SELECT * FROM information_schema.`COLUMNS` " + "WHERE TABLE_SCHEMA = '%s' " + "AND TABLE_NAME = '%s' " + "AND COLUMN_NAME IN (%s)", databaseName, tableName, columnCondition);
    }

    public static List<InfoSchemaColumn> query(String sql) {
        String url = "jdbc:mysql://localhost:3306/ruoyi?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8";
        try (Connection connection = DBUtils.getConnection(url, "root", "123456")) {
            return DBUtils.queryBeanList(connection, sql, InfoSchemaColumn.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
