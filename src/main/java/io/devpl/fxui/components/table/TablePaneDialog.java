package io.devpl.fxui.components.table;

import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

/**
 * 用于TablePane的弹窗，新增或者修改
 * 注意：新增和修改用的是同一个表单
 *
 * @param <F> 表单数据类型，通常是一个JavaFX属性类，支持数据绑定
 * @param <R> 行数据类型，可以是POJO，也可以是JavaFX属性类
 */
class TablePaneDialog<R, F> extends Dialog<F> {

    R editingRow;
    Form form;
    int editingIndex;

    public TablePaneDialog(Form form,  EventHandler<ActionEvent> saveCallback, EventHandler<ActionEvent> updateCallback) {
        this.form = form;
        FormRenderer formRegion = new FormRenderer(form);

        // 可改变大小
        this.setResizable(true);
        // 添加按钮
        this.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        this.getDialogPane().getButtonTypes().add(ButtonType.OK);

        formRegion.setPrefSize(600.0, 400.0);
        // 事件回调
        Button okBtn = (Button) this.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setOnAction(event -> {
            if (!form.isValid()) {
                event.consume();
                return;
            }
            if ("新增".equals(this.getTitle())) {
                saveCallback.handle(event);
            } else if ("修改".equals(this.getTitle())) {
                updateCallback.handle(event);
            }
            event.consume();
        });

        this.getDialogPane().setContent(formRegion);
    }

    public final void edit(int index, R rowToBeEdite) {
        if (rowToBeEdite == null) {
            this.setTitle("新增");
        } else {
            this.editingIndex = index;
            this.editingRow = rowToBeEdite;
            this.setTitle("修改");
        }
        super.show();
    }

    public final void reset() {
        this.editingIndex = -1;
        this.editingRow = null;
    }

    public final void setPrefSize(double w, double h) {
        setWidth(w);
        setHeight(h);
    }

    /**
     * 获取编辑的某行
     *
     * @return 可能为null
     */
    public final R getEditingItem() {
        return editingRow;
    }

    public final boolean hasEditingItem() {
        return editingIndex != -1;
    }

    /**
     * @return -1 或者 0, 每页最大长度
     */
    public final int getEditingIndex() {
        return editingIndex;
    }
}
