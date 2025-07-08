package org.workassistant.ui.controller.fields;

import cn.hutool.core.io.FileUtil;
import org.workassistant.ui.editor.CodeEditor;
import org.workassistant.ui.editor.LanguageMode;
import org.workassistant.ui.model.FieldSpec;
import io.fxtras.scene.FileChooserDialog;
import io.fxtras.scene.mvvm.FxmlBinder;
import io.fxtras.scene.mvvm.FxmlView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@FxmlBinder(location = "layout/fields/FieldsImportJavaView.fxml")
public class JavaImportView extends FxmlView {

    @FXML
    public BorderPane bopRoot;

    CodeEditor editor = CodeEditor.newInstance(LanguageMode.JAVA);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bopRoot.setCenter(editor.getView());
    }

    @FXML
    public void chooseFile(ActionEvent actionEvent) {
        FileChooserDialog.showFileOpenDialog(getStage(actionEvent)).ifPresent(file -> {
            editor.setText(FileUtil.readUtf8String(file), true);
        });
    }

    public void parse(FieldImportEvent event) {

    }

    public List<FieldSpec> convert(List<MetaField> fieldMetaDataList) {
        List<FieldSpec> list = new ArrayList<>(fieldMetaDataList.size());
        for (MetaField fieldMetaData : fieldMetaDataList) {
            FieldSpec fieldSpec = new FieldSpec();
            fieldSpec.setFieldName(fieldMetaData.getName());
            fieldSpec.setFieldDescription(fieldMetaData.getDescription());
            list.add(fieldSpec);
        }
        return list;
    }
}
