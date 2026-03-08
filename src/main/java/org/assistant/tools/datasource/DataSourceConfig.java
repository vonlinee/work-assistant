package org.assistant.tools.datasource;

import java.util.UUID;

/**
 * Model class representing a saved database connection configuration.
 */
public class DataSourceConfig {

    private String id;
    private String name;
    private String type; // MySQL, PostgreSQL, SQLite, Oracle, SQL Server
    private String host;
    private String port;
    private String database;
    private String username;
    private String password;
    private String jdbcUrl; // optional custom override
    private String remark;
    private String status; // transient: test connection result

    public DataSourceConfig() {
        this.id = UUID.randomUUID().toString();
    }

    // ---- Getters & Setters ----

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the effective JDBC URL: custom override if set, otherwise
     * auto-generated.
     */
    public String getEffectiveJdbcUrl() {
        if (jdbcUrl != null && !jdbcUrl.isBlank()) {
            return jdbcUrl;
        }
        return buildJdbcUrl(type, host, port, database);
    }

    /**
     * Builds a JDBC URL from the given components based on driver type.
     */
    public static String buildJdbcUrl(String type, String host, String port, String database) {
        if (type == null)
            return "";
        return switch (type) {
            case "MySQL" -> "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC";
            case "PostgreSQL" -> "jdbc:postgresql://" + host + ":" + port + "/" + database;
            case "SQLite" -> "jdbc:sqlite:" + database;
            case "Oracle" -> "jdbc:oracle:thin:@" + host + ":" + port + ":" + database;
            case "SQL Server" -> "jdbc:sqlserver://" + host + ":" + port + ";databaseName=" + database;
            default -> "jdbc:" + type.toLowerCase() + "://" + host + ":" + port + "/" + database;
        };
    }

    /**
     * Returns the default port for a given database type.
     */
    public static String defaultPort(String type) {
        if (type == null)
            return "";
        return switch (type) {
            case "MySQL" -> "3306";
            case "PostgreSQL" -> "5432";
            case "Oracle" -> "1521";
            case "SQL Server" -> "1433";
            default -> "";
        };
    }
}
