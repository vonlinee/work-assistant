package org.example.workassistant.fxui.controller.fields;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.example.workassistant.fxui.editor.CodeEditor;
import org.example.workassistant.fxui.editor.LanguageMode;
import org.example.workassistant.fxui.model.FieldSpec;
import org.example.workassistant.fxui.tools.json.JSONTreeView;
import org.example.workassistant.sdk.util.FileUtils;
import org.example.workassistant.sdk.util.StringUtils;
import org.example.workassistant.fxui.fxtras.Alerts;
import org.example.workassistant.fxui.fxtras.mvvm.FxmlBinder;
import org.example.workassistant.fxui.fxtras.mvvm.FxmlView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * 导入JSON格式数据
 */
@FxmlBinder(location = "layout/fields/ImportFieldsJSONView.fxml")
public class JsonImportView extends FxmlView {

    @FXML
    public ChoiceBox<String> chbJsonSpec;
    @FXML
    public BorderPane bopRoot;

    CodeEditor codeEditor;

    Gson gson = new Gson();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chbJsonSpec.getItems().addAll("JSON", "JSON5", "HJSON");
        chbJsonSpec.getSelectionModel().select(0);

        if (codeEditor == null) {
            codeEditor = CodeEditor.newInstance(LanguageMode.JSON);
            bopRoot.setCenter(codeEditor.getView());
        }
    }

    /**
     * 解析字段
     *
     * @param event
     */
    public void parseFieldsFromInput(FieldImportEvent event) {
        try {
            List<FieldSpec> list = extractFieldsFromJson(codeEditor.getText());
            publish("AddFields", list);
        } catch (Exception exception) {
            Alerts.exception("解析异常", exception).showAndWait();
            log.error("解析异常", exception);
        }
    }

    /**
     * 解析JSON
     *
     * @param input json文本
     * @return 字段列表
     */
    private List<FieldSpec> extractFieldsFromJson(String input) {
        List<FieldSpec> list = new ArrayList<>();
        JsonElement jsonElement = gson.fromJson(input, JsonElement.class);
        fill(list, jsonElement);
        return list;
    }

    /**
     * 提取所有的Key，不提取值
     *
     * @param fieldList
     * @param jsonElement
     */
    private void fill(List<FieldSpec> fieldList, JsonElement jsonElement) {
        if (jsonElement == null) {
            return;
        }
        if (jsonElement.isJsonObject()) {
            JsonObject jobj = jsonElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jobj.entrySet()) {
                final JsonElement value = entry.getValue();
                if (value.isJsonNull() || value.isJsonPrimitive()) {
                    final FieldSpec metaField = new FieldSpec();
                    metaField.setFieldName(entry.getKey());
                    fieldList.add(metaField);
                } else {
                    fill(fieldList, value);
                }
            }
        } else if (jsonElement.isJsonArray()) {
            final JsonArray jsonArray = jsonElement.getAsJsonArray();

            for (JsonElement element : jsonArray) {
                fill(fieldList, element);
            }
        }
    }

    JSONTreeView jsonTreeView = new JSONTreeView();

    @FXML
    public void showJsonTree(ActionEvent actionEvent) {
        String text = codeEditor.getText();
        if (!StringUtils.hasText(text)) {
            return;
        }
        JsonElement jsonElement;
        try {
            jsonElement = gson.fromJson(text, JsonElement.class);
        } catch (Exception exception) {
            Alerts.exception("JSON解析异常", exception).show();
            return;
        }
        jsonTreeView.addRootJson(jsonElement);

        Scene scene;
        if ((scene = jsonTreeView.getScene()) == null) {
            scene = new Scene(jsonTreeView, 600, 600);
        }
        Stage stage;
        if (Objects.isNull(stage = (Stage) scene.getWindow())) {
            stage = new Stage();
            stage.setScene(scene);
        }
        stage.show();
    }

    FileChooser fileChooser = new FileChooser();

    /**
     * 选择JSON文件
     *
     * @param actionEvent 事件
     */
    @FXML
    public void chooseJsonFile(ActionEvent actionEvent) {
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + File.separator + "Desktop"));
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("json", ".json"));
        File file = fileChooser.showOpenDialog(getStage(actionEvent));
        if (file != null) {
            codeEditor.setText("", true);
            codeEditor.setText(FileUtils.readUtf8String(file), false);
        }
    }
}
