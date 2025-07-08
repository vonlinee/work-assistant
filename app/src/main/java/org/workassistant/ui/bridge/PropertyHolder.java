package org.workassistant.ui.bridge;

import java.util.Properties;

public abstract class PropertyHolder {
    private final Properties properties;

    protected PropertyHolder() {
        super();
        properties = new Properties();
    }

    public void addProperty(String name, String value) {
        properties.setProperty(name, value);
    }

    public String getProperty(String name) {
        return properties.getProperty(name);
    }

    public String getProperty(String name, String defaultValue) {
        return properties.getProperty(name, defaultValue);
    }

    public boolean containsKey(String name) {
        return properties.containsKey(name);
    }

    public final Properties getProperties() {
        return properties;
    }
}
