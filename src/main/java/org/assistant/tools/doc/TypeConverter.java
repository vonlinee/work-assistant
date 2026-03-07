package org.assistant.tools.doc;

/**
 * Utility class for converting Java backend types to frontend representations.
 */
public class TypeConverter {

    /**
     * Converts a Java data type string into a frontend standard type.
     * Mappings:
     * - int, long, Integer, Long, double, Double, float, Float, BigDecimal, short,
     * Short, byte, Byte -> number
     * - boolean, Boolean -> boolean
     * - String, char, Character, Date, LocalDate, LocalDateTime, LocalTime,
     * Timestamp, Instant -> string
     * - List, ArrayList, LinkedList, Set, HashSet, TreeSet, Collection, Array ->
     * array
     * - void, Void -> object
     * - default (complex types, Object, etc) -> object
     *
     * @param javaType The java data type string, potentially containing generics
     *                 e.g. "List<UserDto>"
     * @return A frontend type string: "string", "number", "boolean", "array", or
     *         "object"
     */
    public static String toFrontendType(String javaType) {
        if (javaType == null || javaType.isEmpty()) {
            return "object";
        }

        // Strip out generics if present to get the base type
        String baseType = javaType;
        int genericStart = baseType.indexOf('<');
        if (genericStart > -1) {
            baseType = baseType.substring(0, genericStart).trim();
        }

        // Handle array syntax (e.g. String[])
        if (baseType.endsWith("[]")) {
            return "array";
        }

        String lowerBase = baseType.toLowerCase();

        switch (lowerBase) {
            case "string":
            case "char":
            case "character":
            case "date":
            case "localdate":
            case "localdatetime":
            case "localtime":
            case "timestamp":
            case "instant":
                return "string";

            case "int":
            case "integer":
            case "long":
            case "double":
            case "float":
            case "short":
            case "byte":
            case "bigdecimal":
            case "biginteger":
                return "number";

            case "boolean":
                return "boolean";

            case "list":
            case "set":
            case "collection":
            case "arraylist":
            case "linkedlist":
            case "hashset":
            case "treeset":
                return "array";

            default:
                return "object";
        }
    }
}
