package io.devpl.fxui.view;

import javafx.beans.property.*;

/**
 * @see DataTypeItem
 */
public class DataTypeModel {

    private final ObjectProperty<String> typeGroup = new SimpleObjectProperty<>("");
    private final StringProperty typeKey = new SimpleStringProperty("");
    private final StringProperty typeName = new SimpleStringProperty("");
    private final StringProperty description = new SimpleStringProperty("");
    private final IntegerProperty minLength = new SimpleIntegerProperty(-1);
    private final IntegerProperty maxLength = new SimpleIntegerProperty(-1);
    private final StringProperty defaultValue = new SimpleStringProperty();
    public String getTypeKey() {
        return typeKey.get();
    }

    public StringProperty typeKeyProperty() {
        return typeKey;
    }

    public void setTypeKey(String typeKey) {
        if (typeKey == null) {
            typeKey = "";
        }
        this.typeKey.set(typeKey);
    }

    public String getTypeName() {
        return typeName.get();
    }

    public StringProperty typeNameProperty() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        if (typeName == null) {
            typeName = "";
        }
        this.typeName.set(typeName);
    }

    public String getTypeGroup() {
        return typeGroup.get();
    }

    public ObjectProperty<String> typeGroupProperty() {
        return typeGroup;
    }

    public void setTypeGroup(String typeGroup) {
        if (typeGroup == null) {
            typeGroup = "";
        }
        this.typeGroup.set(typeGroup);
    }

    public final String getDescription() {
        return description.get();
    }

    public final StringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null) {
            description = "";
        }
        this.description.set(description);
    }

    public int getMinLength() {
        return minLength.get();
    }

    public IntegerProperty minLengthProperty() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength.set(minLength);
    }

    public int getMaxLength() {
        return maxLength.get();
    }

    public IntegerProperty maxLengthProperty() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength.set(maxLength);
    }

    public String getDefaultValue() {
        return defaultValue.get();
    }

    public StringProperty defaultValueProperty() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue.set(defaultValue);
    }

    @Override
    public String toString() {
        return "DataTypeModel{" +
            "typeGroup=" + typeGroup.get() +
            ", typeKey=" + typeKey.get() +
            ", typeName=" + typeName.get() +
            '}';
    }
}
