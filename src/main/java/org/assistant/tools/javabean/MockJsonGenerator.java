package org.assistant.tools.javabean;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.assistant.tools.javabean.JavaBeanParser.ClassInfo;
import org.assistant.tools.javabean.JavaBeanParser.FieldInfo;

import java.util.Map;

/**
 * Generates a mock JSON string from a parsed JavaBean class.
 * The JSON is structured so that it can be directly deserialized back
 * into an instance of the original Java class.
 */
public class MockJsonGenerator {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final JavaBeanConfig config;
    private final Map<String, ClassInfo> classMap;

    private int counter = 0;

    public MockJsonGenerator(JavaBeanConfig config, Map<String, ClassInfo> classMap) {
        this.config = config;
        this.classMap = classMap;
    }

    /**
     * Generate a pretty-printed JSON string for the given class.
     *
     * @param classInfo parsed class model
     * @return JSON string
     */
    public String generate(ClassInfo classInfo) {
        counter = 0;
        JsonObject root = buildObject(classInfo, 0);
        return GSON.toJson(root);
    }

    // ─────────────────────────────────────────────────────────────────────────

    private JsonObject buildObject(ClassInfo ci, int depth) {
        JsonObject obj = new JsonObject();
        for (FieldInfo fi : ci.getFields()) {
            obj.add(fi.getName(), buildValue(fi, depth));
        }
        return obj;
    }

    private com.google.gson.JsonElement buildValue(FieldInfo fi, int depth) {
        String base = fi.getBaseType();

        // ── primitive / wrapper / String ──────────────────────────────────────
        if (fi.isSimpleType()) {
            return primitiveElement(base, fi.getName());
        }

        // ── depth guard ───────────────────────────────────────────────────────
        if (depth >= config.getMaxDepth()) {
            return com.google.gson.JsonNull.INSTANCE;
        }

        // ── Collection ────────────────────────────────────────────────────────
        if (fi.isCollection()) {
            return buildArray(fi.getGenericArg(), depth);
        }

        // ── Map ───────────────────────────────────────────────────────────────
        if (fi.isMap()) {
            JsonObject mapObj = new JsonObject();
            mapObj.addProperty("key1", "value1");
            mapObj.addProperty("key2", "value2");
            return mapObj;
        }

        // ── array ─────────────────────────────────────────────────────────────
        if (base.endsWith("[]")) {
            String component = base.substring(0, base.length() - 2).trim();
            FieldInfo componentFi = new FieldInfo(fi.getName(), component);
            JsonArray arr = new JsonArray();
            for (int i = 0; i < config.getMockCollectionSize(); i++) {
                arr.add(buildValue(componentFi, depth + 1));
            }
            return arr;
        }

        // ── nested complex type ───────────────────────────────────────────────
        if (classMap.containsKey(base)) {
            return buildObject(classMap.get(base), depth + 1);
        }

        // ── unknown type: emit empty object ───────────────────────────────────
        return new JsonObject();
    }

    private JsonArray buildArray(String elementType, int depth) {
        JsonArray arr = new JsonArray();
        if (elementType == null || elementType.isEmpty()) {
            return arr;
        }
        for (int i = 0; i < config.getMockCollectionSize(); i++) {
            FieldInfo ef = new FieldInfo("item", elementType);
            arr.add(buildValue(ef, depth + 1));
        }
        return arr;
    }

    // ─────────────────────────────────────────────────────────────────────────

    private JsonPrimitive primitiveElement(String type, String fieldName) {
        int num = config.isRandomNumbers() ? (int) (Math.random() * 1000) : ++counter;
        String mockStr = buildMockString(fieldName, config.getMockStringLength());
        return switch (type) {
            case "String" -> new JsonPrimitive(mockStr);
            case "int", "Integer", "short", "Short", "byte", "Byte" -> new JsonPrimitive(num);
            case "long", "Long" -> new JsonPrimitive((long) num);
            case "double", "Double" -> new JsonPrimitive((double) num);
            case "float", "Float" -> new JsonPrimitive((float) num);
            case "boolean", "Boolean" -> new JsonPrimitive(true);
            case "char", "Character" -> new JsonPrimitive("A");
            case "BigDecimal", "BigInteger" -> new JsonPrimitive(num + ".00");
            case "Date" -> new JsonPrimitive("2024-01-01T00:00:00.000Z");
            case "LocalDate" -> new JsonPrimitive("2024-01-01");
            case "LocalDateTime" -> new JsonPrimitive("2024-01-01T00:00:00");
            case "LocalTime" -> new JsonPrimitive("00:00:00");
            case "Instant" -> new JsonPrimitive("2024-01-01T00:00:00Z");
            case "ZonedDateTime", "OffsetDateTime" -> new JsonPrimitive("2024-01-01T00:00:00+00:00");
            default -> new JsonPrimitive(mockStr);
        };
    }

    private String buildMockString(String fieldName, int length) {
        // Generate a readable mock string based on field name
        String base = fieldName.length() > length ? fieldName.substring(0, length) : fieldName;
        // Pad/suffix with ordinal counter
        return base + (++counter);
    }
}
