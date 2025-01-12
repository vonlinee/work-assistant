package io.devpl.fxui.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * 字典数据
 */
public class SysDictData {

    private final SimpleStringProperty name = new SimpleStringProperty();
    private final SimpleStringProperty code = new SimpleStringProperty();
    private final SimpleStringProperty value = new SimpleStringProperty();
    private final SimpleStringProperty description = new SimpleStringProperty();
    private final SimpleBooleanProperty isEnable = new SimpleBooleanProperty();

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getCode() {
        return code.get();
    }

    public SimpleStringProperty codeProperty() {
        return code;
    }

    public void setCode(String code) {
        this.code.set(code);
    }

    public String getValue() {
        return value.get();
    }

    public SimpleStringProperty valueProperty() {
        return value;
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    public String getDescription() {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public boolean isEnabled() {
        return isEnable.get();
    }

    public SimpleBooleanProperty isEnableProperty() {
        return isEnable;
    }

    public void setEnabled(boolean isEnable) {
        this.isEnable.set(isEnable);
    }
}
