package org.example.workassistant.ui.tools.mybatis;

import org.example.workassistant.ui.model.CommonJavaType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 封装一个带名称和数据类型的值
 */
public class VarItem {

    /**
     * 名称
     */
    private final StringProperty name = new SimpleStringProperty();

    /**
     * 值，如果是字符串，可以判断是否进行数据类型推断
     */
    private final ObjectProperty<Object> value = new SimpleObjectProperty<>();

    /**
     * 值的数据类型
     */
    private final ObjectProperty<CommonJavaType> type = new SimpleObjectProperty<>();

    public VarItem(String name, Object value) {
        this.name.set(name);
        this.value.set(value);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public Object getValue() {
        return value.get();
    }

    public void setValue(Object value) {
        this.value.set(value);
    }

    public ObjectProperty<Object> valueProperty() {
        return value;
    }

    public CommonJavaType getType() {
        return type.get();
    }

    public void setType(CommonJavaType type) {
        this.type.set(type);
    }

    public ObjectProperty<CommonJavaType> typeProperty() {
        return type;
    }
}
