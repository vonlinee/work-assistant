package org.assistant.tools.sql;

import com.alibaba.druid.DbType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class DataTypeManager {

    private static final Preferences PREFS = Preferences.userNodeForPackage(DataTypeManager.class);
    private static final String PREF_PREFIX = "dialect_types_";

    // Fallback static maps
    private static final Map<DbType, String[]> DEFAULT_DATATYPES = new HashMap<>();
    static {
        DEFAULT_DATATYPES.put(DbType.mysql, new String[] { "VARCHAR", "INT", "BIGINT", "TINYINT", "DATE", "DATETIME",
                "TIMESTAMP", "DECIMAL", "DOUBLE", "TEXT", "BLOB" });
        DEFAULT_DATATYPES.put(DbType.postgresql, new String[] { "varchar", "text", "integer", "bigint", "serial",
                "numeric", "real", "double precision", "date", "timestamp", "boolean", "uuid" });
        DEFAULT_DATATYPES.put(DbType.oracle,
                new String[] { "VARCHAR2", "NUMBER", "DATE", "TIMESTAMP", "CLOB", "BLOB", "CHAR", "RAW" });
        DEFAULT_DATATYPES.put(DbType.sqlserver, new String[] { "VARCHAR", "NVARCHAR", "INT", "BIGINT", "DATETIME",
                "DATETIME2", "DECIMAL", "FLOAT", "BIT", "UNIQUEIDENTIFIER" });
    }
    private static final String[] GENERIC_FALLBACK = new String[] { "VARCHAR", "INT", "DATE", "DATETIME", "TIMESTAMP",
            "DECIMAL", "TEXT" };

    /**
     * Gets customized datatypes from Preferences, or falls back to system defaults.
     */
    public static String[] getTypesForDialect(DbType dialect) {
        if (dialect == null)
            return GENERIC_FALLBACK;

        String key = PREF_PREFIX + dialect.name();
        String savedValue = PREFS.get(key, null);
        if (savedValue != null && !savedValue.trim().isEmpty()) {
            return savedValue.split(",");
        }

        return DEFAULT_DATATYPES.getOrDefault(dialect, GENERIC_FALLBACK);
    }

    /**
     * Serializes and saves a custom list of datatypes to OS native preferences.
     */
    public static void saveTypesForDialect(DbType dialect, List<String> types) {
        if (dialect == null || types == null)
            return;

        String key = PREF_PREFIX + dialect.name();
        String serialized = types.stream()
                .filter(t -> t != null && !t.trim().isEmpty())
                .collect(Collectors.joining(","));
        PREFS.put(key, serialized);
    }

    /**
     * Restores a dialect back to system defaults.
     */
    public static void resetToDefaults(DbType dialect) {
        if (dialect == null)
            return;
        PREFS.remove(PREF_PREFIX + dialect.name());
    }
}
