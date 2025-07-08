package org.workassistant.ui.model;

import org.workassistant.ui.controller.BuiltinDriverType;
import org.workassistant.util.DBUtils;
import lombok.Data;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

@Data
public class ConnectionConfig {

    private Long id;
    /**
     * 连接名称: 唯一名称，可作为Map Key
     */
    private String connectionName;
    private String dbType;
    private BuiltinDriverType driverInfo;
    private String host;
    private String port;
    /**
     * 数据库名称
     */
    private String schema;
    private String dbName;
    private String username;
    private String password;
    private String driverClassName;
    private String encoding;

    private Properties properties;

    public String getConnectionUrl() {
        BuiltinDriverType driver = BuiltinDriverType.findByDriverClassName(driverClassName);
        String databaseName = schema;
        if (databaseName == null) {
            databaseName = "";
        }
        if (driver == null) {
            return null;
        }
        return driver.getConnectionUrl(host, port, databaseName, null);
    }

    public String getConnectionUrl(String databaseName) {
        BuiltinDriverType driver = BuiltinDriverType.findByDriverClassName(driverClassName);
        assert driver != null;
        return driver.getConnectionUrl(host, port, databaseName, properties);
    }

    public String getConnectionUrl(String databaseName, Properties properties) {
        BuiltinDriverType driver = BuiltinDriverType.findByDriverClassName(driverClassName);
        assert driver != null;
        return driver.getConnectionUrl(host, port, databaseName, properties);
    }

    public Connection getConnection(String databaseName, Properties properties) throws SQLException {
        String connectionUrl = getConnectionUrl(databaseName, properties);
        if (properties == null) {
            properties = new Properties();
            properties.put("user", username);
            properties.put("password", password);
            properties.put("serverTimezone", "UTC");
            properties.put("useUnicode", "true");
            properties.put("useSSL", "false");
            properties.put("characterEncoding", encoding);
        }
        return DBUtils.getConnection(connectionUrl, properties);
    }

    public Connection getConnection(String databaseName) throws SQLException {
        String connectionUrl = getConnectionUrl(databaseName);
        Properties properties = new Properties();
        properties.put("user", username);
        properties.put("password", password);
        properties.put("serverTimezone", "UTC");
        properties.put("useUnicode", "true");
        properties.put("useSSL", "false");
        properties.put("characterEncoding", encoding);
        return DBUtils.getConnection(connectionUrl, properties);
    }

    /**
     * 获取数据库连接
     *
     * @return 数据库连接实例
     * @throws SQLException 获取连接失败
     */
    public Connection getConnection() throws SQLException {
        String connectionUrl = getConnectionUrl();
        Properties properties = new Properties();
        properties.put("user", username);
        properties.put("password", password);
        properties.put("serverTimezone", "UTC");
        properties.put("useUnicode", "true");
        properties.put("useSSL", "false");
        properties.put("characterEncoding", encoding);
        return DBUtils.getConnection(connectionUrl, properties);
    }

    /**
     * 获取连接名称
     *
     * @return 连接名称，如果原本为空，则填充默认值：host_port，不取数据库名
     */
    public String getConnectionName() {
        if (this.connectionName == null || connectionName.isEmpty()) {
            this.connectionName = host + "_" + port;
        }
        return this.connectionName;
    }

    private String uniqueKey;

    public String getUniqueKey() {
        if (uniqueKey == null) {
            uniqueKey = connectionName;
        }
        return uniqueKey;
    }

    public BuiltinDriverType getDriver() {
        if (this.driverInfo == null) {
            this.driverInfo = BuiltinDriverType.findByDriverClassName(driverClassName);
        }
        return driverInfo;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConnectionConfig) {
            return Objects.equals(this.getConnectionName(), ((ConnectionConfig) obj).getConnectionName());
        }
        return false;
    }
}
