package org.assistant.tools.driver;

import java.util.UUID;

/**
 * Model representing a registered JDBC driver JAR.
 */
public class JdbcDriverInfo {

    private String id;
    private String name; // display name, e.g. "MySQL Connector J 8.0"
    private String driverClass; // e.g. "com.mysql.cj.jdbc.Driver"
    private String jarPath; // absolute path to the .jar file
    private boolean loaded; // transient: whether currently loaded in this session

    public JdbcDriverInfo() {
        this.id = UUID.randomUUID().toString();
    }

    public JdbcDriverInfo(String name, String driverClass, String jarPath) {
        this();
        this.name = name;
        this.driverClass = driverClass;
        this.jarPath = jarPath;
    }

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

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    @Override
    public String toString() {
        return name != null ? name : driverClass;
    }
}
