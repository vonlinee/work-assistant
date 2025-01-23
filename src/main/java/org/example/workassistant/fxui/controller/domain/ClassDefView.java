package org.example.workassistant.fxui.controller.domain;

import org.example.workassistant.fxui.controller.fields.FieldParseToolView;
import org.example.workassistant.fxui.controls.Modal;
import org.example.workassistant.fxui.tools.filestructure.FieldItem;
import org.example.workassistant.fxui.tools.filestructure.JavaFileStrucutreTreeView;
import org.example.workassistant.fxui.tools.filestructure.MethodItem;
import org.example.workassistant.fxui.tools.filestructure.TopLevelClassItem;
import io.fxtras.scene.mvvm.FxmlBinder;
import io.fxtras.scene.mvvm.FxmlView;
import io.fxtras.scene.mvvm.View;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * 类编辑器
 */
@FxmlBinder(location = "layout/class_definition.fxml")
public class ClassDefView extends FxmlView {

    @FXML
    public BorderPane bopRoot;
    @FXML
    public SplitPane sppCenter;

    private JavaFileStrucutreTreeView jfsTreeView;

    FieldParseToolView fieldParseToolView = new FieldParseToolView();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        jfsTreeView = new JavaFileStrucutreTreeView();
        sppCenter.getItems().addAll(jfsTreeView, View.load(ClassView.class));
    }

    @FXML
    public void addDefaultClass(ActionEvent actionEvent) {
        TopLevelClassItem classItem = new TopLevelClassItem();
        classItem.setValue("Student");
        MethodItem methodItem = new MethodItem();
        methodItem.setValue("setName");
        classItem.addMethod(methodItem);
        FieldItem fieldItem = new FieldItem();
        fieldItem.setValue("name");
        classItem.addField(fieldItem);
        jfsTreeView.addClass(classItem);
    }

    @FXML
    public void showFieldImportModal(ActionEvent actionEvent) {
        Modal.show(actionEvent, "字段解析", fieldParseToolView, event -> System.out.println(fieldParseToolView.getFields()));
    }
}
