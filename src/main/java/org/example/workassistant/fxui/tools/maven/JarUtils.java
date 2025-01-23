package org.example.workassistant.fxui.tools.maven;

import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarUtils {

    private static boolean containsModuleInfo(String jarFilePath) {
        try (JarFile jarFile = new JarFile(jarFilePath)) {
            JarEntry entry = jarFile.getJarEntry("module-info.class");
            return entry != null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
