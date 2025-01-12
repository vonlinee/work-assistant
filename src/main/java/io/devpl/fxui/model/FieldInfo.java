package io.devpl.fxui.model;

import io.devpl.fxui.components.table.TableViewColumn;
import io.devpl.fxui.components.table.TableViewModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 字段信息
 */
@TableViewModel
public class FieldInfo {

    @TableViewColumn(title = "修饰符")
    private final StringProperty modifier = new SimpleStringProperty();

    @TableViewColumn(title = "数据类型")
    private final ObjectProperty<CommonJavaType> dataType = new SimpleObjectProperty<>();

    @TableViewColumn(title = "名称")
    private final StringProperty name = new SimpleStringProperty();

    @TableViewColumn(title = "备注")
    private final StringProperty remarks = new SimpleStringProperty();

    public String getModifier() {
        return modifier.get();
    }

    public StringProperty modifierProperty() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier.set(modifier);
    }

    public CommonJavaType getDataType() {
        return dataType.get();
    }

    public ObjectProperty<CommonJavaType> dataTypeProperty() {
        return dataType;
    }

    public void setDataType(CommonJavaType dataType) {
        this.dataType.set(dataType);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getRemarks() {
        return remarks.get();
    }

    public StringProperty remarksProperty() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks.set(remarks);
    }
}
