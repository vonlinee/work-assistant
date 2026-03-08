package org.assistant.tools.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.*;

/**
 * Generator class mapping Gson elements into structured Java POJOs.
 */
public class JsonToPojoGenerator {

    private final String rootClassName;
    private final String targetPackage;
    private final boolean useLombok;
    private final boolean useCamelCase;

    // Stores all generated class structures. The Root class is the first one.
    private final Map<String, ClassGenerator> classes = new LinkedHashMap<>();

    public JsonToPojoGenerator(String rootClassName, String targetPackage, boolean useLombok, boolean useCamelCase) {
        this.rootClassName = rootClassName == null || rootClassName.isEmpty() ? "RootResponse" : rootClassName;
        this.targetPackage = targetPackage == null ? "" : targetPackage;
        this.useLombok = useLombok;
        this.useCamelCase = useCamelCase;
    }

    /**
     * Traverses the element and constructs Java representations.
     */
    public String generate(JsonElement element) {
        if (!element.isJsonObject()) {
            return "// Error: Selected node is not a JSON Object. Cannot generate POJO.";
        }

        processObject(rootClassName, element.getAsJsonObject());

        StringBuilder sb = new StringBuilder();
        if (!targetPackage.isEmpty()) {
            sb.append("package ").append(targetPackage).append(";\n\n");
        }

        if (useLombok) {
            sb.append("import lombok.Data;\n");
            sb.append("import lombok.NoArgsConstructor;\n");
        }
        sb.append("import java.util.List;\n\n");

        for (ClassGenerator cg : classes.values()) {
            sb.append(cg.build(useLombok));
            sb.append("\n");
        }

        return sb.toString();
    }

    private void processObject(String className, JsonObject obj) {
        if (classes.containsKey(className))
            return; // Prevent infinite loops or duplication

        ClassGenerator currentClass = new ClassGenerator(className);
        classes.put(className, currentClass);

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            String originalKey = entry.getKey();
            String fieldName = useCamelCase ? toCamelCase(originalKey) : originalKey;
            String javaType = determineJavaType(originalKey, entry.getValue());

            currentClass.addField(javaType, fieldName, originalKey);
        }
    }

    /**
     * Determines proper Java types. If nested objects are detected, fires off
     * recursive class builds.
     */
    private String determineJavaType(String key, JsonElement element) {
        if (element.isJsonNull())
            return "Object";

        if (element.isJsonPrimitive()) {
            JsonPrimitive prim = element.getAsJsonPrimitive();
            if (prim.isBoolean())
                return "Boolean";
            if (prim.isNumber()) {
                String num = prim.getAsString();
                return num.contains(".") ? "Double" : "Integer";
            }
            return "String";
        }

        if (element.isJsonObject()) {
            String subClassName = capitalize(useCamelCase ? toCamelCase(key) : key);
            processObject(subClassName, element.getAsJsonObject());
            return subClassName;
        }

        if (element.isJsonArray()) {
            JsonArray arr = element.getAsJsonArray();
            if (arr.isEmpty()) {
                return "List<Object>";
            }
            // Sample first element to guess array type
            JsonElement first = arr.get(0);
            String innerType = determineJavaType(key + "Item", first);

            // Clean up suffix heuristic if it generated an object recursively
            if (first.isJsonObject()) {
                innerType = capitalize(useCamelCase ? toCamelCase(key) : key); // Use pluralized/base name for class
                                                                               // instead of "ItemsItem"
                if (innerType.endsWith("s")) {
                    innerType = innerType.substring(0, innerType.length() - 1); // rough singularization
                }
                processObject(innerType, first.getAsJsonObject());
            }

            return "List<" + innerType + ">";
        }

        return "Object";
    }

    private String toCamelCase(String s) {
        if (!s.contains("_") && !s.contains("-"))
            return s;
        String[] parts = s.split("[_-]");
        StringBuilder camelCaseString = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                camelCaseString.append(capitalize(parts[i]));
            }
        }
        return camelCaseString.toString();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty())
            return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private static class ClassGenerator {
        private final String className;
        private final List<Field> fields = new ArrayList<>();

        public ClassGenerator(String className) {
            this.className = className;
        }

        public void addField(String type, String name, String originalName) {
            fields.add(new Field(type, name, originalName));
        }

        public String build(boolean useLombok) {
            StringBuilder sb = new StringBuilder();
            if (useLombok) {
                sb.append("@Data\n@NoArgsConstructor\n");
            }
            sb.append("public class ").append(className).append(" {\n\n");

            for (Field f : fields) {
                if (!f.name.equals(f.originalName) && !useLombok) {
                    // if it differs and we're not using lombok (or maybe we should use jackson
                    // annotations, but skipping for simplicity)
                    // usually @JsonProperty(originalName) is used here. For now we just add a
                    // comment.
                    sb.append("    // original JSON key: ").append(f.originalName).append("\n");
                }
                sb.append("    private ").append(f.type).append(" ").append(f.name).append(";\n");
            }

            if (!useLombok) {
                sb.append("\n");
                for (Field f : fields) {
                    String capName = f.name.substring(0, 1).toUpperCase() + f.name.substring(1);
                    sb.append("    public ").append(f.type).append(" get").append(capName).append("() {\n");
                    sb.append("        return ").append(f.name).append(";\n    }\n\n");

                    sb.append("    public void set").append(capName).append("(").append(f.type).append(" ")
                            .append(f.name).append(") {\n");
                    sb.append("        this.").append(f.name).append(" = ").append(f.name).append(";\n    }\n\n");
                }
            }

            sb.append("}\n");
            return sb.toString();
        }

        private record Field(String type, String name, String originalName) {
        }
    }
}
