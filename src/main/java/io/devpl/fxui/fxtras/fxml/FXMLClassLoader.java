package io.devpl.fxui.fxtras.fxml;

import io.devpl.fxui.utils.ResourceLoader;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 优化FXML加载速度
 * 参考：
 * <a href="https://stackoverflow.com/questions/11734885/javafx2-very-poor-performance-when-adding-custom-made-fxmlpanels-to-gridpane">...</a>
 */
public class FXMLClassLoader extends ClassLoader {
    private final Map<String, Class<?>> classes = new HashMap<>();
    private final ClassLoader parent;

    public FXMLClassLoader(ClassLoader parent) {
        if (parent == null) {
            parent = ClassLoader.getSystemClassLoader();
        }
        this.parent = parent;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> c = findClass(name);
        if (c == null) {
            throw new ClassNotFoundException(name);
        }
        return c;
    }

    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        if (classes.containsKey(className)) {
            return classes.get(className);
        } else {
            try {
                Class<?> result = parent.loadClass(className);
                classes.put(className, result);
                return result;
            } catch (ClassNotFoundException ignore) {
                classes.put(className, null);
                return null;
            }
        }
    }

    @Override
    public URL getResource(String name) {
        if (name.endsWith("fxml")) {
            // FXMLLoader如果未设置ClassLoader，使用AppClassLoader进行加载FXML文件
            // ClassLoader#getResource(name) 返回为null
            return ResourceLoader.load(name);
        }
        return parent.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return parent.getResources(name);
    }

    @Override
    public String toString() {
        return parent.toString();
    }

    @Override
    public void setDefaultAssertionStatus(boolean enabled) {
        parent.setDefaultAssertionStatus(enabled);
    }

    @Override
    public void setPackageAssertionStatus(String packageName, boolean enabled) {
        parent.setPackageAssertionStatus(packageName, enabled);
    }

    @Override
    public void setClassAssertionStatus(String className, boolean enabled) {
        parent.setClassAssertionStatus(className, enabled);
    }

    @Override
    public void clearAssertionStatus() {
        parent.clearAssertionStatus();
    }
}
