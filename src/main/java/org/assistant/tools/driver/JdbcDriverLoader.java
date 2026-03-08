package org.assistant.tools.driver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton runtime loader for JDBC driver JARs.
 *
 * Drivers are loaded via a dedicated URLClassLoader per JAR, then wrapped
 * in a DriverShim and registered with java.sql.DriverManager so that all
 * subsequent DriverManager.getConnection() calls can discover them.
 */
public class JdbcDriverLoader {

    private static final Logger log = LoggerFactory.getLogger(JdbcDriverLoader.class);
    private static final JdbcDriverLoader INSTANCE = new JdbcDriverLoader();

    /** id → shim currently registered with DriverManager */
    private final Map<String, DriverShim> registeredShims = new ConcurrentHashMap<>();
    /** id → classloader that loaded the driver */
    private final Map<String, URLClassLoader> classLoaders = new ConcurrentHashMap<>();

    private JdbcDriverLoader() {
    }

    public static JdbcDriverLoader getInstance() {
        return INSTANCE;
    }

    /**
     * Loads a driver JAR and registers its driver class with DriverManager.
     *
     * @param info the driver metadata (jarPath + driverClass are required)
     * @throws Exception if the JAR cannot be read or the driver class is not found
     */
    public synchronized void load(JdbcDriverInfo info) throws Exception {
        String id = info.getId();
        if (registeredShims.containsKey(id)) {
            log.info("Driver {} is already loaded, skipping.", info.getName());
            info.setLoaded(true);
            return;
        }

        File jar = new File(info.getJarPath());
        if (!jar.exists()) {
            throw new IllegalArgumentException("JAR file not found: " + info.getJarPath());
        }

        URLClassLoader loader = new URLClassLoader(
                new URL[] { jar.toURI().toURL() },
                getClass().getClassLoader());

        Class<?> driverClass = loader.loadClass(info.getDriverClass());
        Driver driver = (Driver) driverClass.getDeclaredConstructor().newInstance();

        DriverShim shim = new DriverShim(driver);
        DriverManager.registerDriver(shim);

        registeredShims.put(id, shim);
        classLoaders.put(id, loader);
        info.setLoaded(true);
        log.info("Loaded JDBC driver: {} from {}", info.getDriverClass(), info.getJarPath());
    }

    /**
     * Deregisters and unloads a driver.
     *
     * @param info the driver to unload
     */
    public synchronized void unload(JdbcDriverInfo info) {
        String id = info.getId();
        DriverShim shim = registeredShims.remove(id);
        if (shim != null) {
            try {
                DriverManager.deregisterDriver(shim);
            } catch (SQLException e) {
                log.warn("Failed to deregister driver {}: {}", info.getName(), e.getMessage());
            }
        }
        URLClassLoader loader = classLoaders.remove(id);
        if (loader != null) {
            try {
                loader.close();
            } catch (Exception e) {
                log.warn("Failed to close classloader for {}: {}", info.getName(), e.getMessage());
            }
        }
        info.setLoaded(false);
        log.info("Unloaded JDBC driver: {}", info.getName());
    }

    /**
     * Unloads and reloads a driver (useful after replacing the JAR file).
     */
    public synchronized void reload(JdbcDriverInfo info) throws Exception {
        unload(info);
        load(info);
    }

    /**
     * Loads all drivers on startup, silently skipping invalid/missing JARs.
     */
    public void loadAll(List<JdbcDriverInfo> drivers) {
        for (JdbcDriverInfo d : drivers) {
            try {
                load(d);
            } catch (Exception e) {
                log.warn("Failed to auto-load driver {}: {}", d.getName(), e.getMessage());
                d.setLoaded(false);
            }
        }
    }

    public boolean isLoaded(String id) {
        return registeredShims.containsKey(id);
    }

    /** Returns all driver IDs currently registered. */
    public Set<String> getLoadedIds() {
        return Collections.unmodifiableSet(registeredShims.keySet());
    }
}
