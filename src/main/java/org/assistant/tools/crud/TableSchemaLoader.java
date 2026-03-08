package org.assistant.tools.crud;

import org.assistant.tools.datasource.DataSourceConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads table names and column metadata from a live database connection,
 * using JDBC {@link DatabaseMetaData}.
 */
public class TableSchemaLoader {

    /**
     * Driver class names that need dynamic loading (loaded via the existing
     * DriverManager).
     */
    private final DataSourceConfig dsConfig;

    public TableSchemaLoader(DataSourceConfig dsConfig) {
        this.dsConfig = dsConfig;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns all table names in the connected schema (excludes views).
     */
    public List<String> loadTableNames() throws SQLException {
        List<String> names = new ArrayList<>();
        try (Connection conn = openConnection();
                ResultSet rs = conn.getMetaData().getTables(
                        getSchemaOrCatalog(conn), null, "%",
                        new String[] { "TABLE" })) {
            while (rs.next()) {
                names.add(rs.getString("TABLE_NAME"));
            }
        }
        names.sort(String::compareToIgnoreCase);
        return names;
    }

    /**
     * Returns column metadata for a given table name.
     */
    public List<TableColumnInfo> loadColumns(String tableName) throws SQLException {
        List<TableColumnInfo> columns = new ArrayList<>();
        try (Connection conn = openConnection()) {
            DatabaseMetaData meta = conn.getMetaData();

            // primary keys
            Set<String> pkColumns = new HashSet<>();
            try (ResultSet pks = meta.getPrimaryKeys(getSchemaOrCatalog(conn), null, tableName)) {
                while (pks.next()) {
                    pkColumns.add(pks.getString("COLUMN_NAME"));
                }
            }

            // columns
            try (ResultSet cols = meta.getColumns(getSchemaOrCatalog(conn), null, tableName, "%")) {
                while (cols.next()) {
                    String colName = cols.getString("COLUMN_NAME");
                    String jdbcType = cols.getString("TYPE_NAME");
                    int size = cols.getInt("COLUMN_SIZE");
                    int nullable = cols.getInt("NULLABLE");
                    String remark = cols.getString("REMARKS");
                    String javaType = TableColumnInfo.jdbcTypeToJava(jdbcType);
                    boolean isPk = pkColumns.contains(colName);
                    columns.add(new TableColumnInfo(colName, jdbcType, javaType,
                            isPk, nullable != DatabaseMetaData.columnNoNulls, size, remark));
                }
            }
        }
        return columns;
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private Connection openConnection() throws SQLException {
        String url = dsConfig.getEffectiveJdbcUrl();
        String user = dsConfig.getUsername();
        String pass = dsConfig.getPassword();
        return DriverManager.getConnection(url, user, pass);
    }

    /**
     * Some drivers use catalog (MySQL), others use schema (PostgreSQL).
     * Returns the database name for catalog-centric drivers, null otherwise.
     */
    private String getSchemaOrCatalog(Connection conn) {
        try {
            String product = conn.getMetaData().getDatabaseProductName().toLowerCase();
            if (product.contains("mysql") || product.contains("mariadb")) {
                return dsConfig.getDatabase();
            }
        } catch (SQLException ignored) {
        }
        return null;
    }
}
