package org.assistant.tools.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * SQLite-backed persistence store for DataSourceConfig objects.
 * Database file is stored in ~/.work-assistant/datasources.db
 */
public class DataSourceConfigStore {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfigStore.class);
    private static final DataSourceConfigStore INSTANCE = new DataSourceConfigStore();

    private final String dbPath;

    private DataSourceConfigStore() {
        File dir = new File(System.getProperty("user.home"), ".work-assistant");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        dbPath = new File(dir, "datasources.db").getAbsolutePath();
        initSchema();
    }

    public static DataSourceConfigStore getInstance() {
        return INSTANCE;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    private void initSchema() {
        String sql = """
                CREATE TABLE IF NOT EXISTS datasource_config (
                    id       TEXT PRIMARY KEY,
                    name     TEXT NOT NULL,
                    type     TEXT,
                    host     TEXT,
                    port     TEXT,
                    database TEXT,
                    username TEXT,
                    password TEXT,
                    jdbc_url TEXT,
                    remark   TEXT
                )
                """;
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            log.error("Failed to initialize datasource schema", e);
        }
    }

    public List<DataSourceConfig> findAll() {
        List<DataSourceConfig> list = new ArrayList<>();
        String sql = "SELECT * FROM datasource_config ORDER BY name";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to load datasource configs", e);
        }
        return list;
    }

    public void save(DataSourceConfig config) {
        if (config.getId() == null || config.getId().isBlank()) {
            config.setId(UUID.randomUUID().toString());
        }
        String sql = """
                INSERT INTO datasource_config
                    (id, name, type, host, port, database, username, password, jdbc_url, remark)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            bindParams(ps, config);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to save datasource config", e);
            throw new RuntimeException("Save failed: " + e.getMessage(), e);
        }
    }

    public void update(DataSourceConfig config) {
        String sql = """
                UPDATE datasource_config
                SET name=?, type=?, host=?, port=?, database=?, username=?, password=?, jdbc_url=?, remark=?
                WHERE id=?
                """;
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, config.getName());
            ps.setString(2, config.getType());
            ps.setString(3, config.getHost());
            ps.setString(4, config.getPort());
            ps.setString(5, config.getDatabase());
            ps.setString(6, config.getUsername());
            ps.setString(7, config.getPassword());
            ps.setString(8, config.getJdbcUrl());
            ps.setString(9, config.getRemark());
            ps.setString(10, config.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to update datasource config", e);
            throw new RuntimeException("Update failed: " + e.getMessage(), e);
        }
    }

    public void delete(String id) {
        String sql = "DELETE FROM datasource_config WHERE id=?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to delete datasource config", e);
            throw new RuntimeException("Delete failed: " + e.getMessage(), e);
        }
    }

    private DataSourceConfig mapRow(ResultSet rs) throws SQLException {
        DataSourceConfig c = new DataSourceConfig();
        c.setId(rs.getString("id"));
        c.setName(rs.getString("name"));
        c.setType(rs.getString("type"));
        c.setHost(rs.getString("host"));
        c.setPort(rs.getString("port"));
        c.setDatabase(rs.getString("database"));
        c.setUsername(rs.getString("username"));
        c.setPassword(rs.getString("password"));
        c.setJdbcUrl(rs.getString("jdbc_url"));
        c.setRemark(rs.getString("remark"));
        return c;
    }

    private void bindParams(PreparedStatement ps, DataSourceConfig config) throws SQLException {
        ps.setString(1, config.getId());
        ps.setString(2, config.getName());
        ps.setString(3, config.getType());
        ps.setString(4, config.getHost());
        ps.setString(5, config.getPort());
        ps.setString(6, config.getDatabase());
        ps.setString(7, config.getUsername());
        ps.setString(8, config.getPassword());
        ps.setString(9, config.getJdbcUrl());
        ps.setString(10, config.getRemark());
    }
}
