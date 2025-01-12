package io.devpl.fxui.tools.json;

import com.google.gson.JsonElement;

/**
 * TreeTableView的每行数据
 */
public class JSONNode {

    /**
     * key字段
     */
    private String key;

    /**
     * 界面上展示的值：Value列
     */
    private String value;

    /**
     * 对应的JSON元素
     */
    private JsonElement element;

    public JSONNode(String key, JsonElement element) {
        this.key = key;
        this.element = element;
        if (element == null) {
            this.value = null;
        } else if (element.isJsonNull()) {
            this.value = "NULL";
        } else if (element.isJsonPrimitive()) {
            this.value = element.getAsString();
        } else if (element.isJsonObject()) {
            this.value = element.getAsJsonObject().size() + " Fields";
        } else if (element.isJsonArray()) {
            this.value = element.getAsJsonArray().size() + " Items";
        }
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public JsonElement getElement() {
        return element;
    }

    public void setElement(JsonElement element) {
        this.element = element;
    }
}
