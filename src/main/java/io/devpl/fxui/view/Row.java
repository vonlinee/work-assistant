package io.devpl.fxui.view;

public class Row {
    private String fieldName;
    private String generatorName;

    public Row(String fieldName, String generatorName) {
        this.fieldName = fieldName;
        this.generatorName = generatorName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getGeneratorName() {
        return generatorName;
    }

    public void setGeneratorName(String generatorName) {
        this.generatorName = generatorName;
    }
}
