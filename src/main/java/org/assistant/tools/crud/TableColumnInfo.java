package org.assistant.tools.crud;

/**
 * Metadata for a single database column, used during CRUD code generation.
 */
public class TableColumnInfo {

    private final String columnName; // e.g. "user_name"
    private final String javaFieldName; // e.g. "userName" (camelCase)
    private final String jdbcType; // e.g. "VARCHAR", "INT"
    private final String javaType; // e.g. "String", "Integer"
    private final boolean primaryKey;
    private final boolean nullable;
    private final int columnSize;
    private final String comment; // column remark / comment

    public TableColumnInfo(String columnName, String jdbcType, String javaType,
            boolean primaryKey, boolean nullable, int columnSize, String comment) {
        this.columnName = columnName;
        this.javaFieldName = toCamelCase(columnName);
        this.jdbcType = jdbcType;
        this.javaType = javaType;
        this.primaryKey = primaryKey;
        this.nullable = nullable;
        this.columnSize = columnSize;
        this.comment = comment == null ? "" : comment;
    }

    // ── Derived helpers ───────────────────────────────────────────────────────

    /**
     * Returns the field name capitalised for setter/getter generation.
     * e.g. "userName" → "UserName"
     */
    public String getCapitalizedFieldName() {
        if (javaFieldName == null || javaFieldName.isEmpty())
            return javaFieldName;
        return Character.toUpperCase(javaFieldName.charAt(0)) + javaFieldName.substring(1);
    }

    // ── Static helpers ────────────────────────────────────────────────────────

    /**
     * Converts snake_case or UPPER_CASE to camelCase.
     * e.g. "user_name" → "userName", "CREATE_TIME" → "createTime"
     */
    public static String toCamelCase(String name) {
        if (name == null || name.isEmpty())
            return name;
        String lower = name.toLowerCase();
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (char c : lower.toCharArray()) {
            if (c == '_' || c == '-') {
                nextUpper = true;
            } else {
                sb.append(nextUpper ? Character.toUpperCase(c) : c);
                nextUpper = false;
            }
        }
        return sb.toString();
    }

    /**
     * Maps JDBC type name to the corresponding Java type name.
     */
    public static String jdbcTypeToJava(String jdbcType) {
        if (jdbcType == null)
            return "Object";
        return switch (jdbcType.toUpperCase()) {
            case "CHAR", "VARCHAR", "LONGVARCHAR", "NCHAR", "NVARCHAR", "TEXT",
                    "TINYTEXT", "MEDIUMTEXT", "LONGTEXT", "CLOB" ->
                "String";
            case "TINYINT" -> "Integer";
            case "SMALLINT" -> "Integer";
            case "INTEGER", "INT", "INT4", "INT2" -> "Integer";
            case "BIGINT", "INT8" -> "Long";
            case "FLOAT", "REAL" -> "Float";
            case "DOUBLE", "DOUBLE PRECISION" -> "Double";
            case "NUMERIC", "DECIMAL" -> "java.math.BigDecimal";
            case "BIT", "BOOLEAN", "BOOL" -> "Boolean";
            case "DATE" -> "java.time.LocalDate";
            case "TIME" -> "java.time.LocalTime";
            case "TIMESTAMP", "DATETIME" -> "java.time.LocalDateTime";
            case "BINARY", "VARBINARY", "BLOB", "LONGBLOB" -> "byte[]";
            default -> "Object";
        };
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getColumnName() {
        return columnName;
    }

    public String getJavaFieldName() {
        return javaFieldName;
    }

    public String getJdbcType() {
        return jdbcType;
    }

    public String getJavaType() {
        return javaType;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public boolean isNullable() {
        return nullable;
    }

    public int getColumnSize() {
        return columnSize;
    }

    public String getComment() {
        return comment;
    }
}
