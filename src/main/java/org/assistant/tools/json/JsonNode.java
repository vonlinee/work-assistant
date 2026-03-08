package org.assistant.tools.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

import java.util.Map;

/**
 * Represents a Node in the JSON visualization tree.
 * Implements lazy-loading. Children are not instantiated until requested
 * initially to prevent memory
 * and rendering exhaustion on massive payloads.
 */
public class JsonNode extends DefaultMutableTreeTableNode {

    private final String key;
    private final JsonElement element;
    private boolean childrenLoaded = false;

    // Cached logical type specifically for UI rendering
    private final String displayType;
    private final String displayValue;

    public JsonNode(String key, JsonElement element) {
        this.key = key;
        this.element = element;

        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            this.displayType = "Object";
            this.displayValue = "{ " + obj.size() + " keys }";
        } else if (element.isJsonArray()) {
            JsonArray arr = element.getAsJsonArray();
            this.displayType = "Array";
            this.displayValue = "[ " + arr.size() + " items ]";
        } else if (element.isJsonNull()) {
            this.displayType = "Null";
            this.displayValue = "null";
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive prim = element.getAsJsonPrimitive();
            if (prim.isBoolean()) {
                this.displayType = "Boolean";
            } else if (prim.isNumber()) {
                this.displayType = "Number";
            } else if (prim.isString()) {
                this.displayType = "String";
            } else {
                this.displayType = "Unknown";
            }
            this.displayValue = prim.getAsString();
        } else {
            this.displayType = "Unknown";
            this.displayValue = element.toString();
        }
    }

    public String getKey() {
        return key;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public String getDisplayType() {
        return displayType;
    }

    public JsonElement getJsonElement() {
        return element;
    }

    @Override
    public String toString() {
        return key; // The Swing JXTreeTable heavily relies on toString for rendering the left-most
                    // column cell hierarchy cleanly
    }

    @Override
    public boolean isLeaf() {
        return element == null || element.isJsonPrimitive() || element.isJsonNull();
    }

    @Override
    public int getChildCount() {
        if (!childrenLoaded) {
            lazyLoadChildren();
        }
        return super.getChildCount();
    }

    /**
     * Spawns Java UI structures explicitly on-demand navigating through the native
     * Gson memory tree efficiently.
     */
    private void lazyLoadChildren() {
        if (childrenLoaded)
            return;

        childrenLoaded = true; // Flag immediately to prevent infinite recursion when add() triggers
                               // getChildCount()

        if (element != null && element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                add(new JsonNode(entry.getKey(), entry.getValue()));
            }
        } else if (element != null && element.isJsonArray()) {
            JsonArray arr = element.getAsJsonArray();
            for (int i = 0; i < arr.size(); i++) {
                add(new JsonNode("[" + i + "]", arr.get(i)));
            }
        }
    }
}
