package org.assistant.tools.driver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * SQLite-backed persistence for registered JDBC driver JARs.
 * Shares the same ~/.work-assistant/datasources.db database.
 */
public class JdbcDriverStore {

    private static final Logger log = LoggerFactory.getLogger(JdbcDriverStore.class);
    private static final JdbcDriverStore INSTANCE = new JdbcDriverStore();

    private final String dbPath;

    private JdbcDriverStore() {
        File dir = new File(System.getProperty("user.home"), ".work-assistant");
        if (!dir.exists())
            dir.mkdirs();
        dbPath = new File(dir, "datasources.db").getAbsolutePath();
        initSchema();
    }

    public static JdbcDriverStore getInstance() {
        return INSTANCE;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    private void initSchema() {
        String sql = """
                CREATE TABLE IF NOT EXISTS jdbc_driver (
                    id           TEXT PRIMARY KEY,
                    name         TEXT NOT NULL,
                    driver_class TEXT NOT NULL,
                    jar_path     TEXT NOT NULL
                )
                """;
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            log.error("Failed to initialize jdbc_driver schema", e);
        }
    }

    public List<JdbcDriverInfo> findAll() {
        List<JdbcDriverInfo> list = new ArrayList<>();
        String sql = "SELECT * FROM jdbc_driver ORDER BY name";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to load jdbc drivers", e);
        }
        return list;
    }

    public void save(JdbcDriverInfo info) {
        if (info.getId() == null || info.getId().isBlank()) {
            info.setId(UUID.randomUUID().toString());
        }
        String sql = "INSERT INTO jdbc_driver (id, name, driver_class, jar_path) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, info.getId());
            ps.setString(2, info.getName());
            ps.setString(3, info.getDriverClass());
            ps.setString(4, info.getJarPath());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to save jdbc driver", e);
            throw new RuntimeException("Save failed: " + e.getMessage(), e);
        }
    }

    public void update(JdbcDriverInfo info) {
        String sql = "UPDATE jdbc_driver SET name=?, driver_class=?, jar_path=? WHERE id=?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, info.getName());
            ps.setString(2, info.getDriverClass());
            ps.setString(3, info.getJarPath());
            ps.setString(4, info.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to update jdbc driver", e);
            throw new RuntimeException("Update failed: " + e.getMessage(), e);
        }
    }

    public void delete(String id) {
        String sql = "DELETE FROM jdbc_driver WHERE id=?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to delete jdbc driver", e);
            throw new RuntimeException("Delete failed: " + e.getMessage(), e);
        }
    }

    private JdbcDriverInfo mapRow(ResultSet rs) throws SQLException {
        JdbcDriverInfo d = new JdbcDriverInfo();
        d.setId(rs.getString("id"));
        d.setName(rs.getString("name"));
        d.setDriverClass(rs.getString("driver_class"));
        d.setJarPath(rs.getString("jar_path"));
        return d;
    }
}
