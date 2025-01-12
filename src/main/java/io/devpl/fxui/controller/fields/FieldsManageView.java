package io.devpl.fxui.controller.fields;

import io.devpl.fxui.fxtras.mvvm.FxmlBinder;
import io.devpl.fxui.fxtras.mvvm.FxmlView;
import io.devpl.fxui.fxtras.mvvm.View;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * 字段元信息管理
 * 没有数据类型，只有字段名和字段值
 */
@FxmlBinder(location = "layout/fields/FieldsManageView.fxml", label = "字段管理")
public class FieldsManageView extends FxmlView {

    @FXML
    public TabPane tbpImportContent;
    @FXML
    public TextField txfSearchField;
    @FXML
    public Button btnSearchSubmit;
    @FXML
    public CheckBox chbAllowDuplicateFieldName;
    @FXML
    public VBox vbox;

    FieldTreeTable fieldTreeTable = new FieldTreeTable();

    @FXML
    public void addNewField(ActionEvent actionEvent) {

    }

    @FXML
    public void parse(ActionEvent actionEvent) {

    }

    /**
     * 导入类型
     */
    enum ImportType {
        JSON,
        SQL,
        TEXT,
        JAVA
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化所有Tab
        for (ImportType importType : ImportType.values()) {
            Tab tab = new Tab(importType.name());
            Node node = newTabContentByImportType(importType);
            if (node == null) {
                continue;
            }
            tab.setContent(node);
            tab.setClosable(false);
            tbpImportContent.getTabs().add(tab);
        }

        vbox.getChildren().add(fieldTreeTable);
    }

    /**
     * 导入类型决定节点
     *
     * @param importType 导入类型
     * @return
     */
    private Node newTabContentByImportType(ImportType importType) {
        Node node = null;
        if (importType == ImportType.SQL) {
            node = View.load(SQLImportView.class);
        } else if (importType == ImportType.JSON) {
            node = View.load(JsonImportView.class);
        } else if (importType == ImportType.JAVA) {
            node = View.load(JavaImportView.class);
        }
        return node;
    }
}
