package io.devpl.sdk.util;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * 运行时系统
 */
public class SystemUtils {

    private static ClassLoader loader = Thread.currentThread().getContextClassLoader();
    private static String classPath = "";

    static {
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        try {
            URL url = loader.getResource("");
            // get class path
            if (url != null) {
                classPath = url.getPath();
                classPath = URLDecoder.decode(classPath, StandardCharsets.UTF_8);
            }
            // 如果是jar包内的，则返回当前路径
            if (StringUtils.isNullOrEmpty(classPath) || classPath.contains(".jar!")) {
                classPath = System.getProperty("user.dir");
            }
        } catch (Throwable ex) {
            classPath = System.getProperty("user.dir");
        }
    }

    public static ClassLoader getLoader() {
        return loader;
    }

    public static String getClassPath() {
        return classPath;
    }

    /**
     * 是否存在类
     *
     * @param className
     * @return
     */
    public static boolean isClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException ex) {
            // ignore expected exception
            return false;
        } catch (LinkageError ex) {
            // unexpected error, need to let the user know the actual error
            return false;
        }
    }

    public static ClassLoader getThreadClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public static ClassLoader getThreadClassLoader(Thread thread) {
        Assert.notNull(thread, "thread must not be null!");
        return thread.getContextClassLoader();
    }
}
