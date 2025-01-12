package io.devpl.fxui.controller.domain;

import io.devpl.fxui.model.CommonJavaType;
import io.devpl.fxui.model.FieldInfo;
import io.devpl.fxui.utils.StringConverters;
import io.devpl.fxui.fxtras.mvvm.FxmlBinder;
import io.devpl.fxui.fxtras.mvvm.FxmlView;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;

import java.net.URL;
import java.util.ResourceBundle;

@FxmlBinder(location = "layout/class.fxml")
public class ClassView extends FxmlView {

    @FXML
    public TableView<FieldInfo> tbvFieldInfo;
    @FXML
    public Button btnAddOne;
    @FXML
    public TextArea txaDdlResult; // 存放DDL生成结果
    @FXML
    public TableColumn<FieldInfo, String> tblcFieldName;
    @FXML
    public TableColumn<FieldInfo, CommonJavaType> tblcFieldType;
    @FXML
    public TableColumn<FieldInfo, String> tblcFieldComment;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tbvFieldInfo.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblcFieldName.setCellValueFactory(param -> param.getValue().nameProperty());
        tblcFieldName.setCellFactory(param -> {
            TextFieldTableCell<FieldInfo, String> tableCell = new TextFieldTableCell<>(new DefaultStringConverter());
            tableCell.setAlignment(Pos.CENTER);
            return tableCell;
        });
        tblcFieldName.setEditable(true);
        tblcFieldType.setCellValueFactory(param -> param.getValue().dataTypeProperty());
        tblcFieldType.setEditable(true);
        tblcFieldType.setCellFactory(param -> {
            ComboBoxTableCell<FieldInfo, CommonJavaType> tableCell = new ComboBoxTableCell<>(StringConverters.forType(CommonJavaType.class, CommonJavaType::getQualifier, CommonJavaType::valueOfQulifiedName)) {
                @Override
                public void startEdit() {
                    super.startEdit();
                }
            };
            tableCell.getItems().addAll(CommonJavaType.values());
            tableCell.setEditable(true);
            return tableCell;
        });

        tblcFieldComment.setEditable(true);
        tblcFieldComment.setCellFactory(TextFieldTableCell.forTableColumn());
        tblcFieldComment.setCellValueFactory(param -> param.getValue().remarksProperty());
    }

    Stage stage = new Stage();

    {
        Scene scene = new Scene(new TypeMappingTable(), 600.0, 400.0);
        stage.setScene(scene);
        stage.setTitle("类型映射表");
    }

    /**
     * 类型映射配置
     *
     * @param actionEvent 事件
     */
    public void showTypeMappingTable(ActionEvent actionEvent) {
        if (stage.isShowing()) {
            if (!stage.isFocused()) {
                stage.requestFocus();
            }
            return;
        }
        stage.show();
    }

    /**
     * 生成DDL
     *
     * @param actionEvent 事件
     */
    @FXML
    public void generateDDL(ActionEvent actionEvent) {
        ObservableList<FieldInfo> items = tbvFieldInfo.getItems();


        txaDdlResult.setText("TODO");
    }

    @FXML
    public void addNewDefaultField(ActionEvent actionEvent) {
        FieldInfo fieldInfo = new FieldInfo();
        fieldInfo.setName("未知");
        fieldInfo.setDataType(CommonJavaType.STRING);
        tbvFieldInfo.getItems().add(fieldInfo);
    }
}
