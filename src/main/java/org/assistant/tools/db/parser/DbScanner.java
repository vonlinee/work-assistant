package org.assistant.tools.db.parser;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class DbScanner {

    public DbSchema scan(String url, String username, String password) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            return scan(conn);
        }
    }

    public DbSchema scan(Connection connection) throws SQLException {
        DbSchema schema = new DbSchema();
        schema.setCatalog(connection.getCatalog());
        schema.setSchema(connection.getSchema());

        DatabaseMetaData metaData = connection.getMetaData();

        // 1. Get Tables
        try (ResultSet tablesRs = metaData.getTables(connection.getCatalog(), connection.getSchema(), "%",
                new String[] { "TABLE", "VIEW" })) {
            while (tablesRs.next()) {
                String tableName = tablesRs.getString("TABLE_NAME");
                String remarks = tablesRs.getString("REMARKS");

                TableInfo tableInfo = new TableInfo();
                tableInfo.setName(tableName);
                tableInfo.setRemarks(remarks);
                schema.addTable(tableInfo);

                // 2. Get Primary Keys
                Set<String> primaryKeys = new HashSet<>();
                try (ResultSet pkRs = metaData.getPrimaryKeys(connection.getCatalog(), connection.getSchema(),
                        tableName)) {
                    while (pkRs.next()) {
                        primaryKeys.add(pkRs.getString("COLUMN_NAME"));
                    }
                }

                // 3. Get Columns
                try (ResultSet columnsRs = metaData.getColumns(connection.getCatalog(), connection.getSchema(),
                        tableName, "%")) {
                    while (columnsRs.next()) {
                        ColumnInfo columnInfo = new ColumnInfo();
                        String columnName = columnsRs.getString("COLUMN_NAME");
                        columnInfo.setName(columnName);
                        columnInfo.setDataType(columnsRs.getInt("DATA_TYPE"));
                        columnInfo.setTypeName(columnsRs.getString("TYPE_NAME"));
                        columnInfo.setSize(columnsRs.getInt("COLUMN_SIZE"));
                        columnInfo.setDecimalDigits(columnsRs.getInt("DECIMAL_DIGITS"));
                        columnInfo.setNullable(columnsRs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                        columnInfo.setRemarks(columnsRs.getString("REMARKS"));
                        columnInfo.setDefaultValue(columnsRs.getString("COLUMN_DEF"));

                        try {
                            String isAutoIncrement = columnsRs.getString("IS_AUTOINCREMENT");
                            columnInfo.setAutoIncrement("YES".equalsIgnoreCase(isAutoIncrement));
                        } catch (SQLException e) {
                            // Driver might not support IS_AUTOINCREMENT
                            columnInfo.setAutoIncrement(false);
                        }

                        columnInfo.setPrimaryKey(primaryKeys.contains(columnName));
                        tableInfo.addColumn(columnInfo);
                    }
                }
            }
        }
        return schema;
    }
}
