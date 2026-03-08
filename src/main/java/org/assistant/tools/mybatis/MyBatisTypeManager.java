package org.assistant.tools.mybatis;

import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class MyBatisTypeManager {

    private static final Preferences PREFS = Preferences.userNodeForPackage(MyBatisTypeManager.class);
    private static final String PREF_JDBC_TYPES = "mybatis_jdbc_types";
    private static final String PREF_DATA_TYPES = "mybatis_data_types";

    // Fallback default arrays
    private static final String[] DEFAULT_JDBC_TYPES = {
            "VARCHAR", "INTEGER", "BIGINT", "DATE", "TIMESTAMP", "BOOLEAN",
            "NUMERIC", "REAL", "BLOB", "CLOB", "DECIMAL", "DOUBLE", "FLOAT",
            "CHAR", "NVARCHAR", "TIME", "NULL"
    };

    private static final String[] DEFAULT_DATA_TYPES = ParamDataType.names();

    /**
     * Retrieves the active configured array of valid JDBC Types.
     */
    public static String[] getJdbcTypes() {
        String savedValue = PREFS.get(PREF_JDBC_TYPES, null);
        if (savedValue != null && !savedValue.trim().isEmpty()) {
            return savedValue.split(",");
        }
        return DEFAULT_JDBC_TYPES.clone();
    }

    /**
     * Saves a customized list of JDBC Types to persistent system preferences.
     */
    public static void saveJdbcTypes(List<String> types) {
        if (types == null)
            return;
        String serialized = types.stream()
                .filter(t -> t != null && !t.trim().isEmpty())
                .collect(Collectors.joining(","));
        PREFS.put(PREF_JDBC_TYPES, serialized);
    }

    /**
     * Retrieves the active configured array of valid Java Data Types.
     */
    public static String[] getDataTypes() {
        String savedValue = PREFS.get(PREF_DATA_TYPES, null);
        if (savedValue != null && !savedValue.trim().isEmpty()) {
            return savedValue.split(",");
        }
        return DEFAULT_DATA_TYPES.clone();
    }

    /**
     * Saves a customized list of generic Data Types to persistent system
     * preferences.
     */
    public static void saveDataTypes(List<String> types) {
        if (types == null)
            return;
        String serialized = types.stream()
                .filter(t -> t != null && !t.trim().isEmpty())
                .collect(Collectors.joining(","));
        PREFS.put(PREF_DATA_TYPES, serialized);
    }

    /**
     * Clears user definitions forcing the UI to cascade back to static defaults
     * natively.
     */
    public static void resetToDefaults() {
        PREFS.remove(PREF_JDBC_TYPES);
        PREFS.remove(PREF_DATA_TYPES);
    }
}
