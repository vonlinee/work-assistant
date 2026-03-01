package org.assistant.tools.doc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Utility class to generate mock JSON representations for API requests and
 * responses.
 */
public class MockDataGenerator {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Generate mock JSON for an API request based on its parameters.
     * Combines body and query/form parameters into a single mock object.
     *
     * @param params list of API parameters
     * @return formatted JSON string representing the request body or parameters
     */
    public static String generateMockRequest(List<ApiParam> params) {
        if (params == null || params.isEmpty()) {
            return "{}";
        }

        Map<String, Object> mockMap = new LinkedHashMap<>();

        ApiParam bodyParam = null;
        for (ApiParam param : params) {
            if (param.getIn() == ParamLocation.BODY) {
                bodyParam = param;
                break;
            }
            if (param.getIn() == ParamLocation.QUERY || param.getIn() == ParamLocation.FORM) {
                if (param.hasFields()) {
                    mockMap.put(param.getName(), generateMockFromFields(param.getFields()));
                } else {
                    mockMap.put(param.getName(),
                            getDummyValueForType(param.getDataType(), param.getExample(), param.getDefaultValue()));
                }
            }
        }

        // If there is a body parameter, its content usually represents the entire
        // request payload
        if (bodyParam != null) {
            if (bodyParam.hasFields()) {
                mockMap.putAll(generateMockFromFields(bodyParam.getFields()));
            } else {
                // simple body
                return GSON.toJson(getDummyValueForType(bodyParam.getDataType(), bodyParam.getExample(),
                        bodyParam.getDefaultValue()));
            }
        }

        return GSON.toJson(mockMap);
    }

    /**
     * Generate mocked URL with path parameters replaced and query parameters
     * appended.
     */
    public static String generateMockUrl(WebApiInfo api) {
        String url = api.getPath() != null ? api.getPath() : "";
        List<ApiParam> params = api.getParams();
        if (params == null)
            return url;

        // Replace path params
        for (ApiParam p : params) {
            if (p.getIn() == ParamLocation.PATH) {
                Object val = getDummyValueForType(p.getDataType(), p.getExample(), p.getDefaultValue());
                url = url.replace("{" + p.getName() + "}", String.valueOf(val));
            }
        }

        // Append query params
        List<String> queryParts = new ArrayList<>();
        for (ApiParam p : params) {
            if (p.getIn() == ParamLocation.QUERY) {
                Object val = getDummyValueForType(p.getDataType(), p.getExample(), p.getDefaultValue());
                try {
                    queryParts.add(URLEncoder.encode(p.getName(), StandardCharsets.UTF_8.name()) + "=" +
                            URLEncoder.encode(String.valueOf(val), StandardCharsets.UTF_8.name()));
                } catch (Exception e) {
                    queryParts.add(p.getName() + "=" + val);
                }
            }
        }

        if (!queryParts.isEmpty()) {
            url += "?" + String.join("&", queryParts);
        }
        return url;
    }

    /**
     * Generate mock headers as a string.
     */
    public static String generateMockHeaders(List<ApiParam> params) {
        if (params == null)
            return "";
        StringBuilder sb = new StringBuilder();
        for (ApiParam p : params) {
            if (p.getIn() == ParamLocation.HEADER) {
                Object val = getDummyValueForType(p.getDataType(), p.getExample(), p.getDefaultValue());
                sb.append(p.getName()).append(": ").append(val).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Generate mock JSON for an API response based on its return type fields.
     *
     * @param fields     list of fields in the return type
     * @param returnType the simple return type name
     * @return formatted JSON string representing the response body
     */
    public static String generateMockResponse(List<FieldInfo> fields, String returnType) {
        if (fields == null || fields.isEmpty()) {
            // Check if return type is simple type or array
            Object dummy = getDummyValueForType(returnType, null, null);
            if (dummy != null) {
                return GSON.toJson(dummy);
            }
            return "{}"; // Default for void or unknown object
        }

        Map<String, Object> mockMap = generateMockFromFields(fields);

        // If it's a list response
        if (returnType != null && isCollectionType(returnType)) {
            List<Object> list = new ArrayList<>();
            list.add(mockMap);
            return GSON.toJson(list);
        }

        return GSON.toJson(mockMap);
    }

    private static Map<String, Object> generateMockFromFields(List<FieldInfo> fields) {
        Map<String, Object> mockMap = new LinkedHashMap<>();
        for (FieldInfo field : fields) {
            if (field.hasChildren()) {
                if (isCollectionType(field.getType())) {
                    List<Object> list = new ArrayList<>();
                    list.add(generateMockFromFields(field.getChildren()));
                    mockMap.put(field.getName(), list);
                } else {
                    mockMap.put(field.getName(), generateMockFromFields(field.getChildren()));
                }
            } else {
                Object value = getDummyValueForType(field.getType(), field.getExample(), field.getDefaultValue());
                if (isCollectionType(field.getType())) {
                    List<Object> list = new ArrayList<>();
                    if (value != null) {
                        list.add(value);
                    }
                    mockMap.put(field.getName(), list);
                } else {
                    mockMap.put(field.getName(), value);
                }
            }
        }
        return mockMap;
    }

    private static Object getDummyValueForType(String type, String example, String defaultValue) {
        if (example != null && !example.isEmpty()) {
            return parseExampleValue(example, type);
        }
        if (defaultValue != null && !defaultValue.isEmpty()) {
            return parseExampleValue(defaultValue, type);
        }

        if (type == null) {
            return "string";
        }

        String lowerType = cleanJavaType(type).toLowerCase();

        return switch (lowerType) {
            case "string", "char", "character" -> "string";
            case "int", "integer", "short", "byte", "long" -> 0;
            case "float", "double", "bigdecimal" -> 0.0;
            case "boolean" -> false;
            case "date", "localdatetime", "timestamp" -> "2024-01-01T12:00:00Z";
            case "localdate" -> "2024-01-01";
            case "localtime" -> "12:00:00";
            default -> "object";
        };
    }

    /**
     * Public access to getting a dummy value for use in tables.
     */
    public static Object getDummyValue(String type, String example, String defaultValue) {
        return getDummyValueForType(type, example, defaultValue);
    }

    private static Object parseExampleValue(String value, String type) {
        if (type == null)
            return value;
        String lowerType = cleanJavaType(type).toLowerCase();
        try {
            switch (lowerType) {
                case "int":
                case "integer":
                case "short":
                case "byte":
                case "long":
                    return Long.parseLong(value);
                case "float":
                case "double":
                case "bigdecimal":
                    return Double.parseDouble(value);
                case "boolean":
                    return Boolean.parseBoolean(value);
                default:
                    return value;
            }
        } catch (Exception e) {
            return value;
        }
    }

    private static String cleanJavaType(String type) {
        if (type.contains("<")) {
            // List<String> -> String
            int start = type.indexOf('<');
            int end = type.lastIndexOf('>');
            if (start != -1 && end != -1 && end > start) {
                return type.substring(start + 1, end).trim();
            }
        }
        if (type.endsWith("[]")) {
            return type.substring(0, type.length() - 2);
        }
        return type;
    }

    private static boolean isCollectionType(String type) {
        if (type == null)
            return false;
        String lowerType = type.toLowerCase();
        return lowerType.startsWith("list") || lowerType.startsWith("set") ||
                lowerType.startsWith("collection") || type.endsWith("[]");
    }
}
