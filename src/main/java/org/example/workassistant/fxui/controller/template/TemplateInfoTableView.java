package org.example.workassistant.fxui.controller.template;

import cn.hutool.core.io.FileUtil;
import org.example.workassistant.fxui.editor.CodeMirrorEditor;
import org.example.workassistant.fxui.editor.LanguageMode;
import org.example.workassistant.fxui.fxtras.utils.EventUtils;
import org.example.workassistant.fxui.model.TemplateInfo;
import org.example.workassistant.sdk.util.StringUtils;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;

import java.io.File;
import java.util.Optional;

/**
 * 模板信息表
 */
public class TemplateInfoTableView extends TableView<TemplateInfo> {

    private final CodeMirrorEditor editor = CodeMirrorEditor.newInstance(LanguageMode.VELOCITY);

    private Dialog<ButtonType> dialog;

    public TemplateInfoTableView() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<TemplateInfo, String> templateNameColumn = new TableColumn<>("模板名称");
        templateNameColumn.setCellValueFactory(new PropertyValueFactory<>("templateName"));
        templateNameColumn.setMaxWidth(150.0);
        templateNameColumn.setMinWidth(150.0);
        TableColumn<TemplateInfo, String> templatePathColumn = new TableColumn<>("模板路径");
        templatePathColumn.setCellValueFactory(new PropertyValueFactory<>("templatePath"));
        TableColumn<TemplateInfo, Boolean> builtinColumn = new TableColumn<>("是否内置");
        builtinColumn.setCellValueFactory(new PropertyValueFactory<>("builtin"));
        builtinColumn.setMaxWidth(60.0);
        builtinColumn.setMinWidth(60.0);
        TableColumn<TemplateInfo, String> remarkColumn = new TableColumn<>("备注信息");
        remarkColumn.setCellValueFactory(new PropertyValueFactory<>("remark"));

        getColumns().add(templateNameColumn);
        getColumns().add(templatePathColumn);
        getColumns().add(builtinColumn);
        getColumns().add(remarkColumn);

        Platform.runLater(() -> dialog = createDialog());

        setRowFactory(param -> {
            TableRow<TemplateInfo> row = new TableRow<>();
            row.setTextAlignment(TextAlignment.CENTER);
            row.setOnMouseClicked(event -> {
                if (!EventUtils.isPrimaryButtonDoubleClicked(event)) {
                    return;
                }
                TemplateInfo item = row.getItem();
                if (item == null) {
                    return;
                }
                File file = new File(item.getTemplatePath());
                if (file.exists()) {
                    String content = FileUtil.readUtf8String(file);
                    if (StringUtils.hasText(content)) {
                        editor.setText(content, true);
                        dialog.setTitle(item.getTemplatePath());
                        Optional<ButtonType> buttonTypeOptional = dialog.showAndWait();
                        if (buttonTypeOptional.isPresent()) {
                            ButtonType btnType = buttonTypeOptional.get();
                            if (btnType == ButtonType.OK) {
                                System.out.println(btnType);
                            }
                        }
                    }
                }
            });
            return row;
        });
    }

    public Dialog<ButtonType> createDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL, ButtonType.FINISH);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.getDialogPane().setContent(editor.getView());
        dialog.setOnCloseRequest(event -> {
            editor.setText("", true);
            // TODO 添加模板编辑保存功能
        });
        return dialog;
    }

}
