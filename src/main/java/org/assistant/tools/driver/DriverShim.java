package org.assistant.tools.driver;

import java.sql.*;
import java.util.logging.Logger;

/**
 * A shim Driver that wraps a Driver loaded by a custom URLClassLoader.
 *
 * DriverManager performs classloader checks: drivers loaded by a child
 * classloader are silently ignored when DriverManager.getConnection() is
 * called from the bootstrap classloader. Wrapping them in a DriverShim
 * (loaded by the system classloader) bypasses this restriction.
 */
public class DriverShim implements Driver {

    private final Driver wrapped;

    public DriverShim(Driver wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Connection connect(String url, java.util.Properties info) throws SQLException {
        return wrapped.connect(url, info);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return wrapped.acceptsURL(url);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, java.util.Properties info) throws SQLException {
        return wrapped.getPropertyInfo(url, info);
    }

    @Override
    public int getMajorVersion() {
        return wrapped.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return wrapped.getMinorVersion();
    }

    @Override
    public boolean jdbcCompliant() {
        return wrapped.jdbcCompliant();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return wrapped.getParentLogger();
    }

    public Driver getWrapped() {
        return wrapped;
    }
}
