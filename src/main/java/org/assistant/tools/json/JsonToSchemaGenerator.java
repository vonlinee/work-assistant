package org.assistant.tools.json;

import com.google.gson.*;

import java.util.Map;

/**
 * Generator class to infer JSON Schema from Gson elements.
 */
public class JsonToSchemaGenerator {

    private final String schemaTitle;
    private final String schemaId;
    private final boolean requireAllProperties;
    private final boolean allowAdditionalProperties;

    public JsonToSchemaGenerator(String schemaTitle, String schemaId, boolean requireAllProperties,
            boolean allowAdditionalProperties) {
        this.schemaTitle = schemaTitle == null || schemaTitle.isEmpty() ? "RootSchema" : schemaTitle;
        this.schemaId = schemaId == null ? "" : schemaId;
        this.requireAllProperties = requireAllProperties;
        this.allowAdditionalProperties = allowAdditionalProperties;
    }

    public String generate(JsonElement element) {
        JsonObject rootSchema = new JsonObject();
        rootSchema.addProperty("$schema", "http://json-schema.org/draft-07/schema#");
        if (!schemaId.isEmpty()) {
            rootSchema.addProperty("$id", schemaId);
        }
        rootSchema.addProperty("title", schemaTitle);
        rootSchema.addProperty("description", "Inferred JSON Schema");

        inferType(element, rootSchema, true);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(rootSchema);
    }

    private void inferType(JsonElement element, JsonObject targetNode, boolean isRoot) {
        if (element.isJsonNull()) {
            targetNode.addProperty("type", "null");
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive prim = element.getAsJsonPrimitive();
            if (prim.isBoolean()) {
                targetNode.addProperty("type", "boolean");
            } else if (prim.isNumber()) {
                String numStr = prim.getAsString();
                if (numStr.contains(".")) {
                    targetNode.addProperty("type", "number");
                } else {
                    targetNode.addProperty("type", "integer");
                }
            } else {
                targetNode.addProperty("type", "string");
            }
        } else if (element.isJsonObject()) {
            targetNode.addProperty("type", "object");
            JsonObject properties = new JsonObject();
            JsonArray requiredArray = new JsonArray();
            JsonObject obj = element.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                String key = entry.getKey();
                JsonObject propSchema = new JsonObject();
                // Avoid setting title/description recursively for all props unless needed,
                // keeping it clean
                inferType(entry.getValue(), propSchema, false);
                properties.add(key, propSchema);
                if (requireAllProperties) {
                    requiredArray.add(key);
                }
            }

            targetNode.add("properties", properties);
            if (requireAllProperties && requiredArray.size() > 0) {
                targetNode.add("required", requiredArray);
            }

            targetNode.addProperty("additionalProperties", allowAdditionalProperties);
        } else if (element.isJsonArray()) {
            targetNode.addProperty("type", "array");
            JsonObject itemsSchema = new JsonObject();
            JsonArray arr = element.getAsJsonArray();

            if (arr.size() > 0) {
                // Infer from first element
                inferType(arr.get(0), itemsSchema, false);
            } else {
                // Empty array -> arbitrary items
                // Draft-07 doesn't strictly need a type, passing no type implies any
            }

            targetNode.add("items", itemsSchema);
        } else {
            // Default fallback
            targetNode.addProperty("type", "object");
        }
    }
}
