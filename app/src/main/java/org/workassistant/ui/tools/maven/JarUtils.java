package org.workassistant.ui.tools.maven;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarUtils {

    public static boolean containsModuleInfo(File file) {
        try (JarFile jarFile = new JarFile(file)) {
            JarEntry entry = jarFile.getJarEntry("module-info.class");
            return entry != null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean containsModuleInfo(String jarFilePath) {
        try (JarFile jarFile = new JarFile(jarFilePath)) {
            JarEntry entry = jarFile.getJarEntry("module-info.class");
            return entry != null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
